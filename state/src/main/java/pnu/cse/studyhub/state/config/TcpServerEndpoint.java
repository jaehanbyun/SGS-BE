package pnu.cse.studyhub.state.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import pnu.cse.studyhub.state.service.MessageService;

@MessageEndpoint
@RequiredArgsConstructor
public class TcpServerEndpoint {
    private final MessageService messageService;

    @ServiceActivator(inputChannel = "inboundChannel")
    public String processMessage(String message) {
        return messageService.processMessage(message);
    }
}
