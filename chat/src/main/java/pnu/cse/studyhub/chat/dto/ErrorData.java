package pnu.cse.studyhub.chat.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
public class ErrorData {
    String errorCode;
    LocalDateTime timestamp;
    String message;
}
