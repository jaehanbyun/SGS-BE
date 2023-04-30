package pnu.cse.studyhub.chat.config.tcp;

import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.stereotype.Component;

@Component
@MessagingGateway(defaultRequestChannel = "outboundChannel")
public interface TCPClientGateway {
    String send(String message);
}
