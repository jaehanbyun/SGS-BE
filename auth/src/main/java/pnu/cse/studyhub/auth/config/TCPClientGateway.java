package pnu.cse.studyhub.auth.config;

import org.springframework.stereotype.Component;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.stereotype.Component;

@Component
@MessagingGateway(defaultRequestChannel = "outboundChannel")
public interface TCPClientGateway {
    String send(String message);

    String sendAndReceive(String message);
}