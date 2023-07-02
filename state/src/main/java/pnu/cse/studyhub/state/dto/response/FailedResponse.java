package pnu.cse.studyhub.state.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@Builder
public class FailedResponse {
    private final String result;
    private final String message;
    private final String status;
    private final String code;
    private final String description;
}