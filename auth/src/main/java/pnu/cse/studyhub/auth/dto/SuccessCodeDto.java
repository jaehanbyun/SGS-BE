package pnu.cse.studyhub.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SuccessCodeDto {

    protected Boolean isSuccess;
    protected String code;
    protected String message;
}
