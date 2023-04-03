package pnu.cse.studyhub.chat.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import pnu.cse.studyhub.chat.dto.response.FailedResponse;


@RestController
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ChatNotFoundException.class)
    public ResponseEntity<FailedResponse> handleChatNotFoundException(Exception e) {
        FailedResponse failedResponse = new FailedResponse<>("Failed", e.getMessage(), ErrorCode.CHAT_NOT_FOUND.toErrorData());
        return ResponseEntity.status(ErrorCode.CHAT_NOT_FOUND.getStatus()).body(failedResponse);
    }

}
