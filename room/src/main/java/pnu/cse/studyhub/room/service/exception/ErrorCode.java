package pnu.cse.studyhub.room.service.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Room-000", "Internal Room server error"),
    NO_CONTENT(HttpStatus.NO_CONTENT,"Room-001","Study room does not exist"),
    ROOM_NOT_FOUND(HttpStatus.NOT_FOUND,"Room-003","Study room not founded"),
    User_NOT_FOUND(HttpStatus.NOT_FOUND,"Room-004","User not founded"),
    INVALID_PERMISSION(HttpStatus.FORBIDDEN,"Room-005","Permission is invalid"),
    MAX_USER(HttpStatus.CONFLICT,"Room-006","The room is full of users."),
    SAME_DATA(HttpStatus.CONFLICT,"Room-007","This information is the same as the current room information."),
    INCORRECT_ROOMCODE(HttpStatus.UNAUTHORIZED,"Room-008","This RoomCode is incorrect"),
    ALREADY_JOIN(HttpStatus.CONFLICT,"Room-009","User has already joined the study group.");


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