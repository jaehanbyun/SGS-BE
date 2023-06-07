package pnu.cse.studyhub.room.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class TcpMessageService {
    private final TcpClientGateway tcpClientGateway;

    public String sendMessage(String message) {
        String timestamp = LocalDateTime.now().toString();
        log.info("Sending message: {}", message);
        String response = tcpClientGateway.send(message);
        log.info("Received response: {}", response);

        return response;
    }


}