package pnu.cse.studyhub.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ResponseStudyTimeDto {
    protected String result;
    protected String message;
    protected StudyTimeDto data;
}
