package pnu.cse.studyhub.chat.exception;


import org.springframework.http.HttpStatus;
import pnu.cse.studyhub.chat.dto.ErrorResponse;

import java.time.LocalDateTime;

public enum ErrorCode {
    //SERVER
    USER_NOT_FOUND(HttpStatus.NOT_FOUND.toString(),"CHAT-001", "채팅이 존재하지 않는 경우"),
    MESSAGE_NOT_DELIVERED(HttpStatus.INTERNAL_SERVER_ERROR.toString(), "CHAT-002", "메시지 전송 실패"),
    INVALID_MESSAGE_FORMAT(HttpStatus.BAD_REQUEST.toString(), "CHAT-003", "메시지 형식이 잘못된 경우"),

    //TCP
    TCP_CONNECTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR.toString(), "CHAT-011", "TCP 연결 실패"),
    TCP_BIND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR.toString(), "CHAT-012", "TCP 바인딩 실패"),
    TCP_TIMEOUT(HttpStatus.REQUEST_TIMEOUT.toString(), "CHAT-013", "TCP 연결 시간 초과"),
    //ETC
    UNKNOWN_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.toString(), "CHAT-021", "알 수 없는 서버 오류"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED.toString(), "CHAT-022", "토큰이 유효하지 않은 경우");

    private final String status;

    private final String code;
    private final String message;

    ErrorCode(String status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
