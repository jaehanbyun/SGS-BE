package pnu.cse.studyhub.signaling.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pnu.cse.studyhub.signaling.config.tcp.TCPClientGateway;
import pnu.cse.studyhub.signaling.dao.request.tcp.TCPMessageRequest;


@Service
@Slf4j
@RequiredArgsConstructor
public class TCPMessageService {
    private final TCPClientGateway tcpClientGateway;

    public String processMessage(String message) {
        log.info("Received message: {}", message);
        ObjectMapper mapper = new ObjectMapper();
        try{
            TCPMessageRequest response = mapper.readValue(message, TCPMessageRequest.class);
            String responseMessage = "";
            switch (response.getServer()) {
                case "state":

                default:
                    break;
            }
            //log.warn("responseMessage : " + responseMessage);
            return responseMessage;
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}