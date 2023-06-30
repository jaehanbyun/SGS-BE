package pnu.cse.studyhub.chat.exception;

import org.springframework.http.HttpStatus;
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
        FailedResponse failedResponse = new FailedResponse<>(
                "Failed",
                ErrorCode.INVALID_MESSAGE_FORMAT.getMessage(),
                ErrorCode.INVALID_MESSAGE_FORMAT.getStatus(),
                ErrorCode.INVALID_MESSAGE_FORMAT.getCode(),
                e.getMessage());
        return ResponseEntity.status(HttpStatus.valueOf(ErrorCode.INVALID_MESSAGE_FORMAT.getStatus())).body(failedResponse);
    }
    @ExceptionHandler(FileConversionException.class)
    public ResponseEntity<FailedResponse> handleFileConversionException(Exception e) {
        FailedResponse failedResponse = new FailedResponse<>(
                "Failed",
                ErrorCode.FILE_CONVERSION_ERROR.getMessage(),
                ErrorCode.FILE_CONVERSION_ERROR.getStatus(),
                ErrorCode.FILE_CONVERSION_ERROR.getCode(),
                e.getMessage());
        return ResponseEntity.status(HttpStatus.valueOf(ErrorCode.FILE_CONVERSION_ERROR.getStatus())).body(failedResponse);
    }
}
