package pnu.cse.studyhub.auth.config;

import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;

@RequiredArgsConstructor
@Component
public class JwtTokenProvider {
    private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

    private final static String EMAIL = "email";
    private final static String ID = "id";

    @Value("${token.secret}")
    private String secretKey;

    // 1시간
    private long tokenTime =  60 * 60 * 1000L;

    // 1시간 * 24
    private long refreshTime = 24 * 60 * 60 * 1000L;

    public String createToken(String email, String userid) {
        Claims claims = Jwts.claims();
        claims.put(ID,userid);
        claims.put(EMAIL,email);

//        LocalDate date = LocalDate.now();
//        Date now = java.sql.Date.valueOf(date);
        LocalDateTime localDateTime = LocalDateTime.now(); // Or any other LocalDateTime

        Date now = Date.from(localDateTime.atZone(ZoneId.of("Asia/Seoul")).toInstant());
//        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
        log.error("date check " + now);
        log.error("date check " + now.getTime());

        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime()+tokenTime))
                .signWith(SignatureAlgorithm.HS256,secretKey)
                .compact();
    }

    public String CreateRefreshToken(String email, String userid) {
        Claims claims = Jwts.claims();
        claims.put(ID,userid);
        claims.put(EMAIL,email);

        LocalDateTime localDateTime = LocalDateTime.now();
        Date now = Date.from(localDateTime.atZone(ZoneId.of("Asia/Seoul")).toInstant());

        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + refreshTime))
                .signWith(SignatureAlgorithm.HS256,secretKey)
                .compact();
    }

    public String getUserid(String token){
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody().get("id").toString();
    }

    public String resolveToken(HttpServletRequest req){
        return req.getHeader("AUTHORIZATION");
    }

    public boolean validateToken(String jwtToken) {
        try {
            if (jwtToken.isEmpty()) throw new JwtException("empty jwtToken");
            Jws<Claims> claimsJws = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(jwtToken);
            return !claimsJws.getBody().getExpiration().before(new Date());
        } catch (JwtException e) {
//            if (jwtToken.isEmpty()) {
//                req.setAttribute("exception", "토큰이 존재하지 않습니다.");
//            }
//            else req.setAttribute("exception", "올바르지 못한 토큰입니다.");
            return false;
        }
    }
}
