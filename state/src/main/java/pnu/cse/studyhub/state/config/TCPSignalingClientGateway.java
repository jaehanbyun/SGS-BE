package pnu.cse.studyhub.state.config;

import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.stereotype.Component;

@Component
@MessagingGateway(defaultRequestChannel = "signalingOutboundChannel")
public interface TCPSignalingClientGateway {
    String send(String message);
}