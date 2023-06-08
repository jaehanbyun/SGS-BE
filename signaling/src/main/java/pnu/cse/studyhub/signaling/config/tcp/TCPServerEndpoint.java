package pnu.cse.studyhub.signaling.config.tcp;

import lombok.RequiredArgsConstructor;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import pnu.cse.studyhub.signaling.service.TCPMessageService;

@MessageEndpoint
@RequiredArgsConstructor
public class TCPServerEndpoint {
    private final TCPMessageService messageService;

    @ServiceActivator(inputChannel = "inboundChannel")
    public String processMessage(String message) {
        return messageService.processMessage(message);
    }
}
