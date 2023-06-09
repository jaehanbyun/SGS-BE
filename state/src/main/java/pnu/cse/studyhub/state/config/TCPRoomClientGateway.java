package pnu.cse.studyhub.state.config;

import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.stereotype.Component;

@Component
@MessagingGateway(defaultRequestChannel = "roomOutboundChannel")
public interface TCPRoomClientGateway {
    String send(String message);
}