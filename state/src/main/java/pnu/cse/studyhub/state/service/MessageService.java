package pnu.cse.studyhub.state.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pnu.cse.studyhub.state.config.TCPClientGateway;
import pnu.cse.studyhub.state.dto.request.TCPChatRequest;
import pnu.cse.studyhub.state.dto.request.TCPMessageRequest;
import pnu.cse.studyhub.state.dto.request.TCPSignalingRequest;
import pnu.cse.studyhub.state.dto.response.TCPRoomResponse;
import pnu.cse.studyhub.state.dto.response.TCPSignalingResponse;
import pnu.cse.studyhub.state.repository.entity.RealTimeData;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageService {

    private final RedisService redisService;
    private final TCPClientGateway tcpClientGateway;

    public String processMessage(String message) {
        log.info("Received message: {}", message);
        ObjectMapper mapper = new ObjectMapper();
        try{
            TCPMessageRequest response = mapper.readValue(message, TCPMessageRequest.class);
//            String responseMessage = String.format("Message \"%s\" is processed", response.toString());
            String responseMessage = "";
            switch (response.getServer()) {
                case "chat":
                    TCPChatRequest chatRequest = (TCPChatRequest) response;
                    log.warn(chatRequest.toString());
                    if (chatRequest.getType().matches("SUBSCRIBE")) {
                        RealTimeData realTimeData =  redisService.getData(chatRequest.getUserId());
                        if (realTimeData == null) {
                            realTimeData = new RealTimeData();
                            realTimeData.setUserId(chatRequest.getUserId());
                            realTimeData.setRoomId(chatRequest.getRoomId());
                            realTimeData.setSessionId(chatRequest.getSession());
                            RealTimeData chatSubscribeRealTimeData =  redisService.setData(realTimeData);
                            responseMessage = mapper.writeValueAsString(chatSubscribeRealTimeData);
                        } else {
                            realTimeData.setRoomId(chatRequest.getRoomId());
                            realTimeData.setSessionId(chatRequest.getSession());
                            RealTimeData chatSubscribeRealTimeData = redisService.setData(realTimeData);
                            responseMessage = mapper.writeValueAsString(chatSubscribeRealTimeData);
                        }
                    } else if (chatRequest.getType().matches("DISCONNECT")) {
                        RealTimeData realTimeData = redisService.getData(chatRequest.getUserId());
                        if (realTimeData != null) {
                            realTimeData.setRoomId(chatRequest.getRoomId());
                            realTimeData.setSessionId(null);
                            RealTimeData chatDisconnectRealTimeData = redisService.setData(realTimeData);
                            responseMessage = mapper.writeValueAsString(chatDisconnectRealTimeData);
                        } else {
                            //예외처리
                        }

                    } else {
                        // 에러처리
                    }


                    break;
                case "signaling":
                    TCPSignalingRequest signalingRequest = (TCPSignalingRequest) response;
                    log.warn(signalingRequest.toString());
                    // Signaling 서버에 StudyTime 전달.
                    if (signalingRequest.getType().matches("STUDY_TIME_FROM_TCP")) {
                        try {
                            RealTimeData realTimeData =  redisService.getData(signalingRequest.getUserId());
                            if (realTimeData != null) {
                                responseMessage = sendStudyTimeMessage(realTimeData);
//                                String roomInResult = sendRoomInOutMessage(realTimeData);
//                                log.info(roomInResult);
                            } else {
                                //예외처리
                            }
                        }catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    // Signaling 서버로부터 StudyTime 저장.
                    } else if (signalingRequest.getType().matches("STUDY_TIME_TO_TCP")) {
                        try {
                            RealTimeData realTimeData =  redisService.getData(signalingRequest.getUserId());
                            if (realTimeData != null) {
                                realTimeData.setStudyTime(signalingRequest.getStudyTime());
                                RealTimeData signalingSetRealTimeData = redisService.setData(realTimeData);
                                responseMessage = sendStudyTimeMessage(signalingSetRealTimeData);
                                String roomOutResult = sendRoomInOutMessage(realTimeData);
                                log.info("roomOutResult : " + roomOutResult);

                            } else {
                                //예외처리
                            }
                        }catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        // 에러처리
                    }
                    break;
            }
            log.warn("responseMessage : " + responseMessage);
            return responseMessage;
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
    public String sendRoomInOutMessage(RealTimeData rtData) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        TCPRoomResponse tcpRoomResponse = TCPRoomResponse.builder()
                .server("state")
                .userId(rtData.getUserId())
                .roomId(rtData.getRoomId())
                .build();

        String tcpRoomResponseMessage = mapper.writeValueAsString(tcpRoomResponse);
        return tcpRoomResponseMessage;
    }
    public String sendStudyTimeMessage(RealTimeData rtData) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        TCPSignalingResponse tcpSignalingResponse = TCPSignalingResponse.builder()
                .userId(rtData.getUserId())
                .studyTime(rtData.getStudyTime())
                .build();

        String tcpSignalingResponseMessage = mapper.writeValueAsString(tcpSignalingResponse);
        return tcpSignalingResponseMessage;
    }
}
