package pnu.cse.studyhub.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpHeaders;

@Getter
@Setter
@NoArgsConstructor
public class ResponseDataDto<T> {

    protected String result;
    protected String message;
    protected SignInResponseDto data;
}
