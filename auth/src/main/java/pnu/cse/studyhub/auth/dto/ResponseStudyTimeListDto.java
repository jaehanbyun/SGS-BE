package pnu.cse.studyhub.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ResponseStudyTimeListDto {

    protected String result;
    protected String message;
    protected List<StudyTimeDto> data;
}
