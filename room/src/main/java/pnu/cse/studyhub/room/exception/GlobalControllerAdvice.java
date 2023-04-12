package pnu.cse.studyhub.room.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pnu.cse.studyhub.room.dto.response.Response;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalControllerAdvice {

        @ExceptionHandler(ApplicationException.class)
        public ResponseEntity<?> applicationHandler(ApplicationException e){
            log.error("Error occurs {}", e.toString());
            String message = e.getMessage();
            Map<String,Object> data = new HashMap<>();
            data.put("errorCode", e.getErrorCode().getErrorCode());
            data.put("description", e.getErrorCode().getDescription());
            data.put("timestamp", e.getTimestamp());

            return ResponseEntity.status(e.getErrorCode().getStatus())
                    .body(Response.error(message,data));
        }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> applicationHandler(RuntimeException e){
        log.error("Error occurs {}", e.toString());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Response.error("Internal Server Error",ErrorCode.INTERNAL_SERVER_ERROR.name()));

    }


}
