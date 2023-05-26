package pnu.cse.studyhub.signaling.config.tcp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class TCPMessageService {
    private final TCPClientGateway tcpClientGateway;

    public String sendMessage(String message) {
        String timestamp = LocalDateTime.now().toString();
        log.info("Sending message: {}", message);
        String response = tcpClientGateway.send(message);
        log.info("Received response: {}", response);

        return response;
    }

//    public String requestStudyTime(String message) {
//        String timestamp = LocalDateTime.now().toString();
//        log.info("Sending message: {}", message);
//
//        String responseMessage = tcpClientGateway.sendAndReceive(message);
//        // 응답 메시지를 파싱하여 studyTime 변수 추출
//        return responseMessage;
//    }
}