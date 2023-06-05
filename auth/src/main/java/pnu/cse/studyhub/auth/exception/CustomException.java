package pnu.cse.studyhub.auth.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CustomException extends RuntimeException{
    CustomExceptionStatus customExceptionStatus;
    private String message;
    private String errorCode;
    private LocalDateTime timestamp;



    public CustomException(CustomExceptionStatus customExceptionStatus, String errorCode, String message){
        this.customExceptionStatus = customExceptionStatus;
        this.message = message;
        this.errorCode = errorCode;
        this.timestamp = LocalDateTime.now();
    }
}
