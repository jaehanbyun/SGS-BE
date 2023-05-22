package pnu.cse.studyhub.auth.dto;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountDto {
    private String userid;

    private String email;

    private String password;

}
