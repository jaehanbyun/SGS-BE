package pnu.cse.studyhub.auth.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pnu.cse.studyhub.auth.dto.AccountDto;

import javax.persistence.*;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserAccount {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long idx;


    @Column(nullable = false, unique = true)
    private String userid;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    public static UserAccount createAccount(AccountDto dto) {

        return UserAccount.builder()
                .userid(dto.getId())
                .email(dto.getEmail())
                .password(dto.getPassword())
                .build();
    }
}
