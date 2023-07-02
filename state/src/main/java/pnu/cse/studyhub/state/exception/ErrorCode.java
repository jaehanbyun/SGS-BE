package pnu.cse.studyhub.state.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    //TCP
    TCP_CONNECTION_FAILED("TCP-ERROR", "STATE-001", "TCP 연결 실패"),
    TCP_INVALID_ADDRESS("TCP-ERROR", "STATE-002", "TCP 바인딩 실패"),
    TCP_TIMEOUT("TCP-ERROR", "STATE-003", "TCP 연결 시간 초과"),
    TCP_HOST_UNREACHABLE("TCP-ERROR", "STATE-004", "호스트의 IP주소를 확인할 수 없음"),
    // 현재 스레드가 대기, 작업 또는 휴면 상태이고 스레드가 Object.wait() 메서드 또는 그와 유사한 동작을 호출했는디 스레드를 다시 중단시킨 경우
    TCP_CONNECTION_INTERRUPTED("TCP-ERROR", "STATE-005", "TCP 연결 인터럽트 발생"),
    TCP_CONNECTION_RESET("TCP-ERROR", "STATE-006", "피어가 TCP 연결을 재설정한 경우"),
    TCP_INVALID_HOST("TCP-ERROR", "STATE-007", "호스트가 올바르지 않음"),
    TCP_IO_ERROR("TCP-ERROR", "STATE-008", "TCP I/O 처리 에러 발생"),


    //ETC
    UNKNOWN_SERVER_ERROR("TCP-ERROR", "STATE-011", "알 수 없는 서버 오류");

    private final String status;

    private final String code;
    private final String message;

    ErrorCode(String status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
