package pnu.cse.studyhub.auth.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CustomException extends RuntimeException{
    CustomExceptionStatus customExceptionStatus;
    private String message;
    private LocalDateTime timestamp;



    public CustomException(CustomExceptionStatus customExceptionStatus,String message){
        this.customExceptionStatus = customExceptionStatus;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }
}
