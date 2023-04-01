package pnu.cse.studyhub.room.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    EXCEPTION_TEST(HttpStatus.UNAUTHORIZED,"Room-002", "exception test"),
    NO_CONTENT(HttpStatus.NO_CONTENT,"Room-001","Study room does not exist"),




    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Room-001", "Internal server error");




    private HttpStatus status;
    private String errorCode;
    private String description;


}

//{
//        "result": "FAIL",
//        "message": "login failed",
//        "data": {
//            "status": 403,
//            "errorCode": "AUTH-005",
//            "description": "탈퇴한 회원이 요청한 경우",
//            "timestamp" : ~~
//        }
//}