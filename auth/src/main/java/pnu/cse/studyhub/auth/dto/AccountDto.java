package pnu.cse.studyhub.auth.dto;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountDto {
    private String id;

    private String email;

    private String password;

}
