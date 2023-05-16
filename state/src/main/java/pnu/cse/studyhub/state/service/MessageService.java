package pnu.cse.studyhub.state.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pnu.cse.studyhub.state.dto.request.TCPChatRequest;
import pnu.cse.studyhub.state.dto.request.TCPMessageRequest;
import pnu.cse.studyhub.state.dto.request.TCPSignalingRequest;
import pnu.cse.studyhub.state.repository.entity.RealTimeData;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageService {

    private final RedisService redisService;

    public String processMessage(String message) {
        log.info("Received message: {}", message);
        ObjectMapper mapper = new ObjectMapper();
        try{
            TCPMessageRequest response = mapper.readValue(message, TCPMessageRequest.class);
            String responseMessage = String.format("Message \"%s\" is processed", response.toString());
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
                            RealTimeData realTimeData1 =  redisService.setData(realTimeData);
                        } else {
                            realTimeData.setRoomId(chatRequest.getRoomId());
                            realTimeData.setSessionId(chatRequest.getSession());
                            RealTimeData realTimeData2 = redisService.setData(realTimeData);
                        }

                    } else if (chatRequest.getType().matches("DISCONNECT")) {
                        RealTimeData realTimeData = redisService.getData(chatRequest.getUserId());
                        if (realTimeData != null) {
                            realTimeData.setRoomId(chatRequest.getRoomId());
                            realTimeData.setSessionId(null);
                            redisService.setData(realTimeData);
                        }
                    } else {
                        // 에러처리
                    }
                    break;
                case "signaling":
                    TCPSignalingRequest signalingRequest = (TCPSignalingRequest) response;
                    log.warn(signalingRequest.toString());
                    if (signalingRequest.getAction().matches("TIMER_ON")) {
                        try {
                            RealTimeData realTimeData =  redisService.getData(signalingRequest.getUserId());
                            LocalDateTime currentTime = LocalDateTime.now();
                            realTimeData.setStudyStartTime(currentTime);
                            RealTimeData savedData = redisService.setData(realTimeData);
                        }catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    } else if (signalingRequest.getAction().matches("TIMER_OFF")) {
                        try {
                            RealTimeData realTimeData =  redisService.getData(signalingRequest.getUserId());
                            LocalDateTime currentTime = LocalDateTime.now();
                            LocalDateTime studyStartTime = realTimeData.getStudyStartTime();
                            Duration totalStudyTime = Duration.between(studyStartTime, currentTime);
                            realTimeData.setStudyStartTime(null);
                            realTimeData.setRecordTime(totalStudyTime);
                            RealTimeData savedData = redisService.setData(realTimeData);
                        }catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        // 에러처리
                    }
                    break;
            }
            return responseMessage;
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
