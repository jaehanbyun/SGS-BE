package pnu.cse.studyhub.chat.config.tcp;

import io.jsonwebtoken.Jwt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pnu.cse.studyhub.chat.dto.request.TCPSocketSessionRequest;
import pnu.cse.studyhub.chat.util.ByteArrayToStringConverter;


@Service
@RequiredArgsConstructor
@Slf4j
public class TCPMessageService {
    private final TCPClientGateway tcpClientGateway;
    private final ByteArrayToStringConverter converter;

    //subscribe
    public void subscribeRoom(Long roomId, String userId, String sessionId) {
        TCPSocketSessionRequest subscribeRequest = TCPSocketSessionRequest.builder()
                .type("SUBSCRIBE")
                .userId(userId)
                .server("chat")
                .roomId(roomId)
                .session(sessionId)
                .build();
        String message = subscribeRequest.toString();
        log.debug("Sending message: {}", message);
        String byteResponse = tcpClientGateway.send(message);
        String strResponse = converter.convert(byteResponse);
        log.debug("Received response: {}", strResponse);
    }
    //unsubscribe || disconnect
    public void unsubscribeRoom(String userId, String sessionId) {
        TCPSocketSessionRequest unsubscribeRequest = TCPSocketSessionRequest.builder()
                .type("UNSUBSCRIBE")
                .userId(userId)
                .server("chat")
                .roomId(null)
                .session(sessionId)
                .build();
        String message = unsubscribeRequest.toString();
        log.debug("Sending message: {}", message);
        String byteResponse = tcpClientGateway.send(message);
        String strResponse = converter.convert(byteResponse);
        log.debug("Received response: {}", strResponse);
    }

}
