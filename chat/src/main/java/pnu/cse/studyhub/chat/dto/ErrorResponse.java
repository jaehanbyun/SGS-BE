package pnu.cse.studyhub.chat.dto;

import lombok.Builder;
import lombok.Data;
import pnu.cse.studyhub.chat.exception.ErrorCode;

import java.time.LocalDateTime;

@Builder
@Data
public class ErrorResponse {
    String result;
    String status;
    String code;
    String message;
    String description;
}
