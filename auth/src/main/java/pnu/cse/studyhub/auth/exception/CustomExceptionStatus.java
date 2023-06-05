package pnu.cse.studyhub.auth.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CustomExceptionStatus {


    /**
     *
     */
    USERID_NOT_FOUND("Userid Not Founded"),
    WRONG_PASSWORD("Wrong Password"),
    DUPLICATED_EMAIL("Duplicated Email Address"),
    EMPTY_EMAIL("Empty Email"),
    INVALID_EMAIL("Invalid Email"),
    DUPLICATED_USERID("Duplicated Userid"),
    ACCOUNT_NOT_FOUND( "Account Not Found"),
    EMAIL_NOT_FOUND( "Email Not Found"),
    INVALID_PARAM( "Invalid Type Parameter"),
    WRONG_ID("Wrong ID");



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
