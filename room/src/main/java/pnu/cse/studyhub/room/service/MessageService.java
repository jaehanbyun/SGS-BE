package pnu.cse.studyhub.room.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pnu.cse.studyhub.room.dto.request.tcp.TCPMessageRequest;
import pnu.cse.studyhub.room.dto.request.tcp.TCPStateRequest;
import pnu.cse.studyhub.room.model.UserRoomId;
import pnu.cse.studyhub.room.model.entity.OpenRoomEntity;
import pnu.cse.studyhub.room.model.entity.OpenUserRoomEntity;
import pnu.cse.studyhub.room.repository.OpenRoomRepository;
import pnu.cse.studyhub.room.repository.UserRoomRepository;
import pnu.cse.studyhub.room.service.exception.ApplicationException;
import pnu.cse.studyhub.room.service.exception.ErrorCode;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageService {

    private final RoomService roomService;

    public String processMessage(String message) {
        log.info("Received message: {}", message);
        ObjectMapper mapper = new ObjectMapper();
        try{
            TCPMessageRequest response = mapper.readValue(message, TCPMessageRequest.class);
//            String responseMessage = String.format("Message \"%s\" is processed", response.toString());
            String responseMessage = "";
            switch (response.getServer()) {
                case "state":
                    TCPStateRequest stateRequest = (TCPStateRequest) response;
                    log.warn(stateRequest.toString());
                    responseMessage = roomService.stateTCP(stateRequest.getRoomId(),stateRequest.getUserId());

                    break;
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