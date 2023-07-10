package pnu.cse.studyhub.gateway.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import pnu.cse.studyhub.gateway.dto.ErrorResponseDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@Order(-1) // 내부 bean 보다 우선 순위를 높여 해당 빈이 동작하게 설정
@Slf4j
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {
//    @Value("${EC2_IP}")
//    String authUrl;
    
    private final WebClient webClient;

    public GlobalExceptionHandler(WebClient.Builder webClientBuilder) {
        webClient = webClientBuilder.build();
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        log.error("In Exception Handler = {}", ex.toString());

        ErrorCode errorCode;
        ErrorResponseDto errorResponseDto;
        if (ex.getClass() == MalformedJwtException.class || ex.getClass() == SignatureException.class
                || ex.getClass() == UnsupportedJwtException.class ) {
            errorCode = ErrorCode.INVALID_TOKEN;
            errorResponseDto = errorCode.toErrorResponseDto("유효하지 않은 토큰");
        }
        else if (ex.getClass() == ExpiredJwtException.class){
            String refreshToken = exchange.getRequest().getHeaders().getValuesAsList("refreshToken").get(0);
            String tmp = refreshToken.replace("refreshToken=", "");
            refreshToken = "";
            for (int i=0;i<tmp.length();i++) {
                if (tmp.charAt(i) == ';') break;
                refreshToken += tmp.charAt(i);
            }
            log.error("check refresh token "+refreshToken);
            URI uri = UriComponentsBuilder
                    .fromUriString("http://localhost:8080")
                    .path("/auth/refreshToken")
                    .build()
                    .toUri();
            exchange.getRequest()
                    .mutate()
                    .uri(uri)
                    .header("refresh_token", refreshToken)
                    .build();

            ServerHttpResponse response = exchange.getResponse();

            String finalRefreshToken = refreshToken;
            return webClient
                    .method(HttpMethod.POST)
                    .uri(uri)
                    .headers(headers -> headers.add("refresh_token", finalRefreshToken))
                    .exchange()
                    .flatMap(clientResponse -> response.writeWith(clientResponse.bodyToMono(DataBuffer.class)));


//            HttpClient client = HttpClientBuilder.create().build(); // HttpClient 생성
//            HttpPost getRequest = new HttpGet(requestURL); //GET 메소드 URL 생성
//            getRequest.addHeader("x-api-key", RestTestCommon.API_KEY); //KEY 입력
//
//            HttpResponse response = client.execute(getRequest);
//
//            //Response 출력
//            if (response.getStatusLine().getStatusCode() == 200) {
//                ResponseHandler<String> handler = new BasicResponseHandler();
//                String body = handler.handleResponse(response);
//                System.out.println(body);
//            } else {
//                System.out.println("response is error : " + response.getStatusLine().getStatusCode());
//            }

//            errorCode = ErrorCode.EXPIRED_TOKEN;
//            errorResponseDto = errorCode.toErrorResponseDto("만료된 토큰");
        }
        else if (ex.getClass() == JwtTokenMissingException.class) {
            errorCode = ErrorCode.MISSING_TOKEN;
            errorResponseDto = errorCode.toErrorResponseDto("토큰이 전달되지 않음");
        } else {
            errorResponseDto = null;
            ex.printStackTrace();
        }
        ObjectMapper mapper = new ObjectMapper();
        String result = null;

        try {
            result = mapper.writeValueAsString(errorResponseDto);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        byte[] bytes = result.getBytes(StandardCharsets.UTF_8);
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().add("Content-Type","application/json");
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        return exchange.getResponse().writeWith(Flux.just(buffer));

    }
}