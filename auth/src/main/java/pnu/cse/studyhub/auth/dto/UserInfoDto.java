package pnu.cse.studyhub.auth.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInfoDto {

    private String id;

    private String month;

    private String day;
    private String studyTime;
}
