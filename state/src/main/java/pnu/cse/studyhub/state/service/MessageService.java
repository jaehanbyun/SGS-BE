package pnu.cse.studyhub.state.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pnu.cse.studyhub.state.config.TCPClientGateway;
import pnu.cse.studyhub.state.dto.UserStudyTime;
import pnu.cse.studyhub.state.dto.request.TCPAuthRequest;
import pnu.cse.studyhub.state.dto.request.TCPChatRequest;
import pnu.cse.studyhub.state.dto.request.TCPMessageRequest;
import pnu.cse.studyhub.state.dto.request.TCPSignalingRequest;
import pnu.cse.studyhub.state.dto.response.TCPAuthResponse;
import pnu.cse.studyhub.state.dto.request.TCPRoomRequest;
import pnu.cse.studyhub.state.dto.response.TCPSignalingResponse;
import pnu.cse.studyhub.state.repository.entity.RealTimeData;
import pnu.cse.studyhub.state.util.JsonConverter;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageService {

    private final RedisService redisService;
    private final TCPClientGateway tcpClientGateway;
    private final JsonConverter jsonConverter;

    public String processMessage(String message) {
        log.info("Received message: {}", message);
        ObjectMapper mapper = new ObjectMapper();
        try{
            TCPMessageRequest response = jsonConverter.convertFromJson(message, TCPMessageRequest.class);
            String responseMessage = "";
            switch (response.getServer()) {
                case "chat":
                    TCPChatRequest chatRequest = (TCPChatRequest) response;
                    if (chatRequest.getType().matches("SUBSCRIBE")) {
                        RealTimeData realTimeData =  redisService.findRealTimeData(chatRequest.getUserId());
                        if (realTimeData != null) { // 오늘 접속 이력이 있는 경우
                            realTimeData.setRoomId(chatRequest.getRoomId());
                            realTimeData.setSessionId(chatRequest.getSession());
                            RealTimeData chatSubscribeRealTimeData = redisService.saveRealTimeDataAndSession(realTimeData);

                            responseMessage = jsonConverter.convertToJson(chatSubscribeRealTimeData);
                        } else { // 오늘 접속 이력이 없는 경우
                            realTimeData = makeRealTimeData(chatRequest);
                            RealTimeData chatSubscribeRealTimeData = redisService.saveRealTimeDataAndSession(realTimeData);

                            responseMessage = jsonConverter.convertToJson(chatSubscribeRealTimeData);
                        }
                    } else if (chatRequest.getType().matches("DISCONNECT|UNSUBSCRIBE")) {
                        String userId = redisService.findUserIdBySessionId(chatRequest.getSession());

                        if (userId  != null) {
                            redisService.deleteRealTimeDataAndSession(userId, chatRequest.getSession());
                            responseMessage = mapper.writeValueAsString(redisService.findRealTimeData(userId));
                        } else {
                            // 존재하지 않는 접속 이력에 대한 삭제 동작 , 예외처리
                        }

                    } else {
                        // 구독, 구독해제, 연결해제를 제외한 나머지 소켓 동작
                    };
                    break;
                case "signaling":
                    TCPSignalingRequest signalingRequest = (TCPSignalingRequest) response;
                    // Signaling <- state, StudyTime 조회, 방 들어옴.
                    if (signalingRequest.getType().matches("STUDY_TIME_FROM_TCP")) {
                        RealTimeData realTimeData =  redisService.findRealTimeData(signalingRequest.getUserId());
                        if (realTimeData != null) { // 공부 이력이 있는 경우
                            responseMessage = sendSignalingServerStudyTimeMessage(realTimeData);
                        } else { // 공부 이력이 없는 경우
                            realTimeData = makeRealTimeData(signalingRequest);

                            responseMessage = sendSignalingServerStudyTimeMessage(realTimeData);
                        }
                    // Signaling -> state,  StudyTime 저장, 방 나감.
                    } else if (signalingRequest.getType().matches("STUDY_TIME_TO_TCP")) {
                        RealTimeData realTimeData =  redisService.findRealTimeData(signalingRequest.getUserId());
                        String roomOutResult = "";
                        if (realTimeData != null) { // 공부 이력이 있는 경우
                            // 공부 시간 저장
                            realTimeData.setStudyTime(signalingRequest.getStudyTime());
                            RealTimeData signalingSetRealTimeData = redisService.saveRealTimeData(realTimeData);
                            responseMessage = sendSignalingServerStudyTimeMessage(signalingSetRealTimeData);
                            // Room Out 알림
                            roomOutResult = sendRoomServerRoomOutMessage(realTimeData);
                        } else {
                            // 공부 이력이 없는 경우
                            realTimeData = makeRealTimeData(signalingRequest);
                            responseMessage = sendSignalingServerStudyTimeMessage(realTimeData);
                            roomOutResult = sendRoomServerRoomOutMessage(realTimeData);
                        }
                        tcpClientGateway.send(roomOutResult);
                    } else {
                        // 에러처리
                    }
                    break;
                case "auth":
                    TCPAuthRequest authRequest = (TCPAuthRequest) response;
                    log.warn(authRequest.toString());
                    // 유저 서버에 공부 시간 전달
                    if (authRequest.getType().matches("USER_STUDY_TIME")) {
                        try {
                            RealTimeData realTimeData =  redisService.findRealTimeData(authRequest.getUserId());
                            if (realTimeData != null) {
                                responseMessage = sendAuthServerStudyTimeMessage(realTimeData);
                            } else {
                                //예외처리
                            }
                        }catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                    break;
            }
            log.debug("responseMessage : " + responseMessage);
            return responseMessage;
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
    public String sendRoomServerRoomOutMessage(RealTimeData rtData) {
        TCPRoomRequest tcpRoomRequest = TCPRoomRequest.builder()
                .server("state")
                .userId(rtData.getUserId())
                .roomId(rtData.getRoomId())
                .build();

        String tcpRoomOutResponseMessage = jsonConverter.convertToJson(tcpRoomRequest);
        return tcpRoomOutResponseMessage;
    }
    public String sendSignalingServerStudyTimeMessage(RealTimeData rtData) {
        TCPSignalingResponse tcpSignalingResponse = TCPSignalingResponse.builder()
                .userId(rtData.getUserId())
                .studyTime(rtData.getStudyTime())
                .build();

        String tcpSignalingResponseMessage = jsonConverter.convertToJson(tcpSignalingResponse);
        return tcpSignalingResponseMessage;
    }
    public String sendAuthServerStudyTimeMessage(RealTimeData realTimeData){
        TCPAuthResponse tcpAuthResponse = TCPAuthResponse.builder()
                .userId(realTimeData.getUserId())
                .studyTime(realTimeData.getStudyTime())
                .build();

        String tcpAuthResponseMessage = jsonConverter.convertToJson(tcpAuthResponse);
        return tcpAuthResponseMessage;
    }
    public RealTimeData makeRealTimeData(TCPChatRequest chatRequest) {
        RealTimeData realTimeData = new RealTimeData();
        realTimeData.setUserId(chatRequest.getUserId());
        realTimeData.setRoomId(chatRequest.getRoomId());
        realTimeData.setSessionId(chatRequest.getSession());
        return realTimeData;
    }
    public RealTimeData makeRealTimeData(TCPSignalingRequest signalingRequest) {
        RealTimeData realTimeData = new RealTimeData();
        realTimeData.setUserId(signalingRequest.getUserId());
        realTimeData.setStudyTime(signalingRequest.getStudyTime());
        return realTimeData;
    }
}
