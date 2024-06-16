package pnu.cse.studyhub.room.config;

import lombok.RequiredArgsConstructor;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import pnu.cse.studyhub.room.service.MessageService;

@MessageEndpoint
@RequiredArgsConstructor
public class TcpServerEndpoint {
    private final MessageService messageService;

    @ServiceActivator(inputChannel = "inboundChannel")
    public String processMessage(String message) {
        return messageService.processMessage(message);
    }
}