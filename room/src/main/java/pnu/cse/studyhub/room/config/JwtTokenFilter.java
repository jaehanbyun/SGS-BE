package pnu.cse.studyhub.room.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import pnu.cse.studyhub.room.util.JwtTokenUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtTokenFilter extends OncePerRequestFilter {

    @Value("${jwt.secret-key}")
    private String key;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException,ServletException {

        final String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        try {
            final String token = header.split(" ")[1].trim();
            String userId = JwtTokenUtils.getUserId(token,key);
            request.setAttribute("userId", userId);

            filterChain.doFilter(request,response);
        }catch (RuntimeException e){
            log.error("Error occurs while validation. {}", e.toString());
            filterChain.doFilter(request,response);
        }

        // TODO : Test용
        //request.setAttribute("userId", "test");
        //filterChain.doFilter(request,response);
    }
}
