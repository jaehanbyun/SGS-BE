package pnu.cse.studyhub.auth.dto;

import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SignInResponseDto {
    private String id;

    private String email;

    private String accessToken;

    private String refreshToken;

}
