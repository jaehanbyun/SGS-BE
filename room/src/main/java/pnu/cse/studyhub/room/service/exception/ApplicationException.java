package pnu.cse.studyhub.room.service.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class ApplicationException extends RuntimeException {

    private ErrorCode errorCode;
    private String message;
    private LocalDateTime timestamp;



    public ApplicationException(ErrorCode errorCode,String message){
        this.errorCode = errorCode;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

}

