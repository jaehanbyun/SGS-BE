package pnu.cse.studyhub.chat.tcp;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pnu.cse.studyhub.chat.config.tcp.TCPMessageService;

@Slf4j
public class TcpTest {

    @Autowired
    private TCPMessageService tcpMessageService;

    @Test
    @DisplayName("채팅 저장 테스트")
    void saveChat() {
        tcpMessageService.sendMessage("test");

    }
}
