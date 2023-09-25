package pnu.cse.studyhub.auth.dto;

import lombok.*;

import javax.servlet.http.Cookie;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SignInResponseDto {
    private String id;

    private String email;

    private String accessToken;

}
