package pnu.cse.studyhub.signaling.config.tcp;

import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.stereotype.Component;

@Component
@MessagingGateway(defaultRequestChannel = "outboundChannel")
public interface TCPClientGateway {
    String send(String message);
}