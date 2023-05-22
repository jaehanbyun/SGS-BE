package pnu.cse.studyhub.auth.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CustomExceptionStatus {


    /**
     *
     */
    USERID_NOT_FOUND("AUTH-002", "Userid Not Founded"),
    WRONG_PASSWORD("AUTH-002", "Wrong Password"),
    DUPLICATED_EMAIL("AUTH-003", "Duplicated Email Address"),
    EMPTY_EMAIL("AUTH-003", "Empty Email"),
    INVALID_EMAIL("AUTH-003", "Invalid Email"),
    DUPLICATED_USERID("AUTH-004", "Duplicated Userid"),
    ACCOUNT_NOT_FOUND("AUTH-006", "Account Not Found");



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
