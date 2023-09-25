package pnu.cse.studyhub.room.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

public class JwtTokenUtils {

//    public static String getUserId(String token, String key){
//        System.out.println("겟유저아이이디");
//        return extractClaims(token,key).get("id",String.class);
//    }
//
//    private static Claims extractClaims(String token, String key){
//        System.out.println("익스트랙트클레임");
//        return Jwts.parserBuilder().setSigningKey(getKey(key))
//                .build().parseClaimsJws(token).getBody();
//    }
//
//    private static Key getKey(String key){
//        System.out.println("겟키");
//        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
//        return Keys.hmacShaKeyFor(keyBytes);
//    }

    public static String getUserId(String token,String key){
        return Jwts.parser().setSigningKey(key).
                parseClaimsJws(token).getBody().get("id",String.class);
    }


}
