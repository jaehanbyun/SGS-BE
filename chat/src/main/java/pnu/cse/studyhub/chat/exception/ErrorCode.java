package pnu.cse.studyhub.chat.exception;


import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    //SERVER
    USER_NOT_FOUND(HttpStatus.NOT_FOUND.toString(),"CHAT-001", "해당 유저가 존재하지 않음"),
    MESSAGE_NOT_DELIVERED(HttpStatus.INTERNAL_SERVER_ERROR.toString(), "CHAT-002", "메시지 전송 실패"),
    INVALID_MESSAGE_FORMAT(HttpStatus.BAD_REQUEST.toString(), "CHAT-003", "메시지 형식이 잘못됨"),
    //SOCKET
    KAFKA_CONNECTION_FAILED("INTERNAL_SOCKET_ERROR", "CHAT-011", "소켓-카프카 연결 실패"),
    KAFKA_INTERRUPTTED("INTERNAL_SOCKET_ERROR", "CHAT-012", "카프카 인터럽트 발생"),
    KAFKA_SERIALIZE_FAILED("INTERNAL_SOCKET_ERROR", "CHAT-013", "카프카 직렬화 실패"),
    KAFKA_TIMEOUT("INTERNAL_SOCKET_ERROR", "CHAT-014", "카프카 연결 시간 초과"),
    KAFKA_UNKNOWN_ERROR("INTERNAL_SOCKET_ERROR", "CHAT-015", "알 수 없는 카프카 오류"),
    //TCP
    TCP_CONNECTION_FAILED("TCP-ERROR", "CHAT-021", "TCP 연결 실패"),
    TCP_BIND_FAILED("TCP-ERROR", "CHAT-022", "TCP 바인딩 실패"),
    TCP_TIMEOUT("TCP-ERROR", "CHAT-023", "TCP 연결 시간 초과"),
    //ETC
    UNKNOWN_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.toString(), "CHAT-031", "알 수 없는 서버 오류"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED.toString(), "CHAT-032", "토큰이 유효하지 않은 경우"),
    FILE_CONVERSION_ERROR(HttpStatus.BAD_REQUEST.toString(), "CHAT-033", "파일 변환 실패"),;

    private final String status;

    private final String code;
    private final String message;

    ErrorCode(String status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
