package pnu.cse.studyhub.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StudyTimeDto {
    protected String userid;
    protected String date;
    protected String studyTime;
}
