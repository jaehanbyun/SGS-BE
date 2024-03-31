package pnu.cse.studyhub.auth.config;

import lombok.RequiredArgsConstructor;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import pnu.cse.studyhub.auth.service.MessageService;

@MessageEndpoint
@RequiredArgsConstructor
public class TCPServerEndpoint {
    private final MessageService messageService;

    @ServiceActivator(inputChannel = "inboundChannel")
    public String processMessage(String message) {
        return messageService.processMessage(message);
    }
}