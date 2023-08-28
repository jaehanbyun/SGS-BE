package pnu.cse.studyhub.chat.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import pnu.cse.studyhub.chat.exception.ErrorCode;

@Data
@RequiredArgsConstructor
@Builder
public class FailedResponse<D> {
   private final String result;
   private final String message;
   private final String status;
   private final String code;
   private final String description;
}
