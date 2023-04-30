package pnu.cse.studyhub.chat.exception;


import org.springframework.http.HttpStatus;
import pnu.cse.studyhub.chat.dto.ErrorData;

import java.time.LocalDateTime;

public enum ErrorCode {
    CHAT_NOT_FOUND(HttpStatus.NOT_FOUND,"CHAT-001", "채팅이 존재하지 않는 경우");

    private final HttpStatus status;
    private final String code;
    private final String description;

    ErrorCode(HttpStatus status, String code, String description) {
        this.status = status;
        this.code = code;
        this.description = description;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public ErrorData toErrorData() {
        return ErrorData
                .builder()
                .errorCode(this.code)
                .timestamp(LocalDateTime.now())
                .message(description)
                .build();
    }
}
