package pnu.cse.studyhub.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StudyTimeDto {
    private String userid;

    private String date;

    private String studyTimeStr;

    private Integer studyTimeInt;
}
