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
    ROOM_NOT_FOUND(HttpStatus.NOT_FOUND,"Room-003","Study room not founded"),
    User_NOT_FOUND(HttpStatus.NOT_FOUND,"Room-004","User not founded"),
    INVALID_PERMISSION(HttpStatus.UNAUTHORIZED,"Room-005","Permission is invalid"),
    MAX_USER(HttpStatus.CONFLICT,"Room-006","The room is full of users."),


    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Room-000", "Internal server error");




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