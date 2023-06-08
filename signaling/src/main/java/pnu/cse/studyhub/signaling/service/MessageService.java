package pnu.cse.studyhub.signaling.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pnu.cse.studyhub.signaling.dao.request.tcp.TCPAlertRequest;
import pnu.cse.studyhub.signaling.dao.request.tcp.TCPMessageRequest;
import pnu.cse.studyhub.signaling.dao.request.tcp.TCPTypeRequest;
import pnu.cse.studyhub.signaling.util.Room;
import pnu.cse.studyhub.signaling.util.RoomManager;
import pnu.cse.studyhub.signaling.util.UserRegistry;
import pnu.cse.studyhub.signaling.util.UserSession;

import java.io.IOException;

import static pnu.cse.studyhub.signaling.util.Message.userAlertMessage;
import static pnu.cse.studyhub.signaling.util.Message.userTypeMessage;


@Service
@Slf4j
@RequiredArgsConstructor
public class MessageService {

    private final UserRegistry userRegistry;
    private final RoomManager roomManager;

    public String processMessage(String message) {
        log.info("Received message: {}", message);
        ObjectMapper mapper = new ObjectMapper();
        try{
            TCPMessageRequest response = mapper.readValue(message, TCPMessageRequest.class);
            String responseMessage = "";
            String userId = null;
            switch (response.getType()) {
                case "ALERT":
                    TCPAlertRequest alertRequest = (TCPAlertRequest) response;
                    userId = alertRequest.getUserId();

                    UserSession alertUser = userRegistry.getByUserId(userId);
                    alertUser.sendMessage(userAlertMessage(userId,alertRequest.getAlertCount()));
                    responseMessage = "SUCCESS";
                    break;

                case "KICK_OUT":
                case "KICK_OUT_BY_ALERT":

                    TCPTypeRequest kickOutRequest = (TCPTypeRequest) response;
                    userId = kickOutRequest.getUserId();

                    UserSession User = userRegistry.getByUserId(userId);
                    User.sendMessage(userTypeMessage(response.getType(),userId));
                    responseMessage = "SUCCESS";
                    break;

                case "DELEGATE":
                    TCPTypeRequest delegateRequest = (TCPTypeRequest) response;

                    final Room room = roomManager.onlyGetRoom(delegateRequest.getRoomId());
                    room.delegateOwner(response.getType(), delegateRequest.getUserId());
                    responseMessage = "SUCCESS";
                    break;

                default:
                    break;
            }
            return responseMessage;
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}