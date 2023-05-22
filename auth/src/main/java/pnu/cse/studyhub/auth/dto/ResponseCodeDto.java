package pnu.cse.studyhub.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ResponseCodeDto<T> {
    protected String success;
    protected String message;
    protected SuccessCodeDto successCode;

}
