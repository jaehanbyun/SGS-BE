package pnu.cse.studyhub.chat.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

@Data
@RequiredArgsConstructor
@Builder
public class ErrorResponse {
//    private final int errorCode;
//    private final String message;
   private final int status;
   private final String message;
   private final String errorCode;
   private final LocalDate timestamp;
}
