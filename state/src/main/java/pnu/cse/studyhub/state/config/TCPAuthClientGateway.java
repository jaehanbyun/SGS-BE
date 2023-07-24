package pnu.cse.studyhub.state.config;

import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.stereotype.Component;

@Component
@MessagingGateway(defaultRequestChannel = "authOutboundChannel")
public interface TCPAuthClientGateway {
    String send(String message);
}