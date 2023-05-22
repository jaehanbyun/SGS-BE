package pnu.cse.studyhub.gateway.dto;

import lombok.*;

@Data
@RequiredArgsConstructor
@Builder
public class ErrorResponseDto  {
    private final int status;
    private final String errorCode;
    private final String description;
    private final String errorMsg;
}