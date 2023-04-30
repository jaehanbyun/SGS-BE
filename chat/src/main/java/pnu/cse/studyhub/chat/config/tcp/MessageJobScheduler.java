package pnu.cse.studyhub.chat.config.tcp;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessageJobScheduler {
    private final TCPMessageService tcpMessageService;


//    @Scheduled(fixedDelay = 10000L)
    public void sendMessage() {
        tcpMessageService.sendMessage("scheduled test message");
    }
}
