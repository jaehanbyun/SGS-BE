package pnu.cse.studyhub.state.config;

import lombok.RequiredArgsConstructor;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import pnu.cse.studyhub.state.dto.response.FailedResponse;
import pnu.cse.studyhub.state.exception.ErrorCode;
import pnu.cse.studyhub.state.service.MessageService;
import pnu.cse.studyhub.state.util.JsonConverter;

import java.io.IOException;
import java.net.BindException;
import java.net.NoRouteToHostException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.channels.ClosedByInterruptException;
import java.util.HashMap;
import java.util.Map;

@MessageEndpoint
@RequiredArgsConstructor
public class TCPServerEndpoint {
    private final MessageService messageService;
    private final JsonConverter jsonConverter;

    private static final Map<Class<? extends Exception>, ErrorCode> ERROR_MAPPING = new HashMap<>();

    static {
        ERROR_MAPPING.put(SocketException.class, ErrorCode.TCP_CONNECTION_FAILED);
        ERROR_MAPPING.put(BindException.class, ErrorCode.TCP_INVALID_ADDRESS);
        ERROR_MAPPING.put(NoRouteToHostException.class, ErrorCode.TCP_HOST_UNREACHABLE);
        ERROR_MAPPING.put(ClosedByInterruptException.class, ErrorCode.TCP_CONNECTION_INTERRUPTED);
        ERROR_MAPPING.put(SocketException.class, ErrorCode.TCP_CONNECTION_RESET);
        ERROR_MAPPING.put(UnknownHostException.class, ErrorCode.TCP_INVALID_HOST);
        ERROR_MAPPING.put(IOException.class, ErrorCode.TCP_IO_ERROR);
        // 추가로 필요한 매핑 추가
    }

    @ServiceActivator(inputChannel = "inboundChannel")
    public String processMessage(String message) {
        return messageService.processMessage(message);
    }

    @ServiceActivator(inputChannel = "errorChannel")
    public String errorHandler(Exception exception) {
        ErrorCode errorCode = ERROR_MAPPING.get(exception.getClass());
        if (errorCode == null)
            errorCode = ErrorCode.UNKNOWN_SERVER_ERROR;

        FailedResponse failedResponse = createFailedResponse(exception.getMessage(), errorCode);
        String errorResponse = jsonConverter.convertToJson(failedResponse);
        return errorResponse;
    }


    private FailedResponse createFailedResponse(String message, ErrorCode errorCode) {
        return new FailedResponse(
                "Failed",
                errorCode.getMessage(),
                errorCode.getStatus(),
                errorCode.getCode(),
                message);
    }
}
