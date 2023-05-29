package pnu.cse.studyhub.chat.config.tcp;

import io.jsonwebtoken.Jwt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class TCPMessageService {
    private final TCPClientGateway tcpClientGateway;

    public void sendMessage(String message) {
        String timestamp = LocalDateTime.now().toString();
        log.info("Sending message: {}", message);
        String response = tcpClientGateway.send(message);
        log.info("Received response: {}", response);
    }
}
