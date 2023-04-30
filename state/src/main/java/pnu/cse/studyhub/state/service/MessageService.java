package pnu.cse.studyhub.state.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pnu.cse.studyhub.state.dto.request.TCPSocketSessionRequest;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageService {

    private final RedisService redisService;

    public String processMessage(String message) {
        log.info("Received message: {}", message);
        ObjectMapper mapper = new ObjectMapper();
        try{
            TCPSocketSessionRequest response = mapper.readValue(message, TCPSocketSessionRequest.class);
            String responseMessage = String.format("Message \"%s\" is processed", response.toString());
            log.info("Sending response: {}", responseMessage);
            if (response.getType() == "CONNECT") {
                redisService.setValues(response.getUserId(), response.getSession());
            }
            else if (response.getType() == "DISCONNECT") {
                redisService.delValues(response.getUserId());
            } else {
                // 에러처리
            }
            return responseMessage;
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
