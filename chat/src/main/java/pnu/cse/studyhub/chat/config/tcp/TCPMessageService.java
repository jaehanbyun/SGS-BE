package pnu.cse.studyhub.chat.config.tcp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pnu.cse.studyhub.chat.util.ByteArrayToStringConverter;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class TCPMessageService {
    private final TCPClientGateway tcpClientGateway;
    private final ByteArrayToStringConverter converter;


    public void sendMessage(String message) {
        log.debug("Sending message: {}", message);
        String byteResponse = tcpClientGateway.send(message);
        String strResponse = converter.convert(byteResponse);
        log.debug("Received response: {}", strResponse);
    }

}
