package pnu.cse.studyhub.gateway.security;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import pnu.cse.studyhub.gateway.exception.JwtTokenMissingException;

import java.util.Base64;
import java.util.Date;

import static org.springframework.web.reactive.function.server.RequestPredicates.queryParam;

@Component
@Slf4j
public class AuthorizationHeaderFilter extends AbstractGatewayFilterFactory<AuthorizationHeaderFilter.Config> {
    Environment env;
    String secretKey;

    @Autowired
    public AuthorizationHeaderFilter(Environment env) {
        super(Config.class);
        this.env = env;
        this.secretKey = env.getProperty("token.secret_key");
    }
    public static class Config {

    }
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)){
                throw new JwtTokenMissingException("헤더 없음");
            }

            String authorizationHeader = request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
            String jwt = authorizationHeader.replace("Bearer ", "");

            log.error("jwt:"+jwt);

//            if(!isValidToken(jwt)) {
//                log.error("invalid token");
//            }
            String id = isValidToken(jwt);
//            request.mutate().parameter()
            ServerHttpRequest modifiedRequest = exchange.getRequest()
                    .mutate()
                    .header("id",id)
                    .build();


            ServerWebExchange modifiedExchange = exchange.mutate()
                    .request(modifiedRequest)
                    .build();

            return chain.filter(exchange);
        };
    }

//    public void validateJwtToken(String token) {
//        try {
//            String secretKey = env.getProperty("token.secret_key");
//            secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
//            log.error("check point for check decode  "+ secretKey);
//            Claims claims1 = Jwts.parser()
//                    .setSigningKey(env.getProperty("token.secret_key"))
//                    .parseClaimsJws(token)
//                    .getBody();
//            log.error("check for claim method 1  : "+ claims1.toString());
//            Jws<Claims> claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
//            log.error("claim : "+claims.toString());
//        } catch (SignatureException | MalformedJwtException |
//                 UnsupportedJwtException | IllegalArgumentException | ExpiredJwtException jwtException) {
//            log.error("error text : "+jwtException.toString());
//            jwtException.printStackTrace();
//            throw jwtException;
//        }
//    }

    /**
     * 토큰 유효여부 확인
     */
    public String isValidToken(String token) {
        log.info("isValidToken token = {}", token);
        if(isTokenExpired(token)) { log.error("check"); }
        String id = checkValidToken(token);

//        return (token.equals(checkToken) && !isTokenExpired(token));
        return id;
    }

    /**
     * 토큰의 Claim 디코딩
     */
    private Claims getAllClaims(String token) {
        log.info("getAllClaims token = {}", token);
        log.info("check sigin key= {}", secretKey);
//        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Claim 에서 username 가져오기
     */
    public String checkValidToken(String token) {
        Claims claims = getAllClaims(token);
        log.error("Check claims ={}",claims.toString());
        String id = (String) claims.get("id");
        return id;
//        return Jwts.builder()
//                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
//                .setClaims(claims)
//                .signWith(SignatureAlgorithm.HS256,secretKey)
//                .compact();
    }

    /**
     * 토큰 만료기한 가져오기
     */
    public Date getExpirationDate(String token) {
        Claims claims = getAllClaims(token);
        return claims.getExpiration();
    }

    /**
     * 토큰이 만료되었는지
     */
    private boolean isTokenExpired(String token) {
        return getExpirationDate(token).before(new Date());
    }

}
