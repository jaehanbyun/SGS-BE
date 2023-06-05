package pnu.cse.studyhub.auth.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pnu.cse.studyhub.auth.dto.AccountDto;
import pnu.cse.studyhub.auth.dto.UserInfoDto;

import javax.persistence.*;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long idx;

    @Column(nullable = false, unique = true)
    private String userid;

    @Column(nullable = false)
    private String date;

    @Column(nullable = false)
    private String studyTime;



    public static User createInfo(UserInfoDto dto) {

        return User.builder()
                .userid(dto.getId())
                .date(dto.getDate())
                .studyTime(dto.getStudyTime())
                .build();
    }
}
