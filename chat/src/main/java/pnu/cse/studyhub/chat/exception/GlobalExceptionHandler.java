package pnu.cse.studyhub.chat.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pnu.cse.studyhub.chat.dto.response.FailedResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(InvalidMessageFormatException.class)
    public ResponseEntity<FailedResponse> handleInvalidMessageFormatException(Exception e) {
        FailedResponse failedResponse = new FailedResponse<>("Failed", e.getMessage(), ErrorCode.INVALID_MESSAGE_FORMAT.toErrorResponse(e.getMessage()));
        return ResponseEntity.status(ErrorCode.INVALID_MESSAGE_FORMAT.getStatus()).body(failedResponse);
    }
    @ExceptionHandler(FileConversionException.class)
    public ResponseEntity<FailedResponse> handleFileConversionException(Exception e) {
        FailedResponse failedResponse = new FailedResponse<>("Failed", e.getMessage(), ErrorCode.FILE_CONVERSION_ERROR.toErrorResponse(e.getMessage()));
        return ResponseEntity.status(ErrorCode.FILE_CONVERSION_ERROR.getStatus()).body(failedResponse);
    }
}
