package pnu.cse.studyhub.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ResponseDataDto<T> {
    protected String success;
    protected String message;
    protected SignInResponseDto accountInfo;
}
