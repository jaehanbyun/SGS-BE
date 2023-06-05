package pnu.cse.studyhub.auth.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInfoDto {

    private String id;

    private String date;

    private String studyTime;
}
