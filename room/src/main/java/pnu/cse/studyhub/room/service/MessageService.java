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

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageService {

    private final OpenRoomRepository openRoomRepository;
    private final UserRoomRepository userRoomRepository;

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


                    Optional<OpenRoomEntity> openRoomEntity = openRoomRepository.findById(stateRequest.getRoomId());

                    if(openRoomEntity.isPresent()){ // 공개방이면
                        OpenUserRoomEntity openUserRoom = userRoomRepository.findById(new UserRoomId(stateRequest.getUserId(), stateRequest.getRoomId())).orElseThrow(() ->
                                new ApplicationException(ErrorCode.User_NOT_FOUND, String.format("%s not founded", stateRequest.getUserId())));

                        if(openUserRoom.isRoomOwner()){ // 방장이면 openRoomEntity 삭제
                            openRoomRepository.delete(openRoomEntity.get());
                            responseMessage = "DELETE";

                        }else{ // 일반 유저면 openRoomEntity 인원 수 감소
                            openRoomEntity.get().minusUser();
                            responseMessage = "KEEP";
                        }

                    }else{
                        responseMessage = "KEEP";
                    }
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