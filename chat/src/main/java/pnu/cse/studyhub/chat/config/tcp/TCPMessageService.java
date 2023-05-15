package pnu.cse.studyhub.chat.config.tcp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class TCPMessageService {
    private final TCPClientGateway tcpClientGateway;


    public void sendMessage(String message) {
        log.info("Sending message: {}", message);
        String response = tcpClientGateway.send(message);
        log.info("Received response: {}", response);
    }
}
