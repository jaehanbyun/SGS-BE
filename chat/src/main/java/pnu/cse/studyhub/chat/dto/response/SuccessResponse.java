package pnu.cse.studyhub.chat.dto.response;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class SuccessResponse<D> {
    private final String result;
    private final String message;
    private final D data;
}
