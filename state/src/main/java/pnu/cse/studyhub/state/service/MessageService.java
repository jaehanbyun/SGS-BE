package pnu.cse.studyhub.state.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pnu.cse.studyhub.state.config.TCPAuthClientGateway;
import pnu.cse.studyhub.state.config.TCPRoomClientGateway;
import pnu.cse.studyhub.state.config.TCPSignalingClientGateway;
import pnu.cse.studyhub.state.dto.UserDto;
import pnu.cse.studyhub.state.dto.request.receive.*;
import pnu.cse.studyhub.state.dto.request.send.TCPSignalingSendAlertRequest;
import pnu.cse.studyhub.state.dto.request.send.TCPSignalingSendRequest;
import pnu.cse.studyhub.state.dto.response.receive.TCPAuthReceiveResponse;
import pnu.cse.studyhub.state.dto.request.send.TCPRoomSendRequest;
import pnu.cse.studyhub.state.dto.response.receive.TCPSignalingReceiveResponse;
import pnu.cse.studyhub.state.repository.entity.RealTimeData;
import pnu.cse.studyhub.state.util.JsonConverter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageService {

    private final RedisService redisService;
    private final TCPRoomClientGateway tcpRoomClientGateway;
    private final TCPSignalingClientGateway tcpSignalingClientGateway;
    private final TCPAuthClientGateway tcpAuthClientGateway;
    private final JsonConverter jsonConverter;

    public String processMessage(String message) {
        log.info("Received message: {}", message);
        ObjectMapper mapper = new ObjectMapper();
        try{
            TCPMessageReceiveRequest response = jsonConverter.convertFromJson(message, TCPMessageReceiveRequest.class);
            String responseMessage = "";
            switch (response.getServer()) {
                case "chat":
                    TCPChatReceiveRequest chatRequest = (TCPChatReceiveRequest) response;
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
                            // RealTimeData, 즉 공부 시간 기록은 남기고, session 정보만 삭제
                            redisService.deleteSession(userId);
                            // redisService.deleteRealTimeDataAndSession(userId, chatRequest.getSession());
                            responseMessage = mapper.writeValueAsString(redisService.findRealTimeData(userId));
                        } else {
                            // 존재하지 않는 접속 이력에 대한 삭제 동작 , 예외처리
                        }

                    } else {
                        // 구독, 구독해제, 연결해제를 제외한 나머지 소켓 동작
                    };
                    break;
                case "signaling":
                    TCPSignalingReceiveRequest signalingRequest = new TCPSignalingReceiveRequest();
                    if (response.getType().startsWith("STUDY_TIME") && response instanceof TCPSignalingReceiveRequest) {
                        signalingRequest = (TCPSignalingReceiveRequest) response;

                        // Signaling <- state, StudyTime 조회, 방 들어옴.
                        if (signalingRequest.getType().matches("STUDY_TIME_FROM_TCP")) {
                            RealTimeData realTimeData =  redisService.findRealTimeData(signalingRequest.getUserId());
                            if (realTimeData != null) { // 공부 이력이 있는 경우
                                responseMessage = sendSignalingServerStudyTimeMessage(realTimeData);
                            } else { // 공부 이력이 없는 경우
                                realTimeData = makeRealTimeData(signalingRequest);

                                responseMessage = sendSignalingServerStudyTimeMessage(realTimeData);
                                log.debug("STUDY_TIME_FROM_TCP: {}", responseMessage);
                            }
                            // Signaling -> state,  StudyTime 저장, 방 나감.
                        } else if (signalingRequest.getType().matches("STUDY_TIME_TO_TCP")) {
                            RealTimeData realTimeData = redisService.findRealTimeData(signalingRequest.getUserId());
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
                            tcpRoomClientGateway.send(roomOutResult);
                        } else {
                            // ClassCastException
                        }
                    } else {
                        // 에러처리
                    }
                    break;
                case "signaling_scheduling":
                    TCPSignalingReceiveSchedulingRequest signalingSchedulingRequest = new TCPSignalingReceiveSchedulingRequest();
                    if (response.getType().startsWith("SCHEDULER") && response instanceof TCPSignalingReceiveSchedulingRequest) {
                        signalingSchedulingRequest = (TCPSignalingReceiveSchedulingRequest) response;
                        // 새벽 05:00에 시그널링 서버로 부터 데이터 동기화를 위해 일괄적으로 데이터를 받음.
                        if (signalingSchedulingRequest.getType().matches("SCHEDULER")) {
                            List<UserDto> userList = signalingSchedulingRequest.getUsers();
                            for (UserDto userDto : userList) {
                                RealTimeData realTimeData = redisService.findRealTimeData(userDto.getUserId());
                                // 유저가 존재하면, redis에 유저의 시간 정보를 갱신함
                                if (realTimeData != null) {
                                    realTimeData.setStudyTime(userDto.getStudyTime());
                                    redisService.saveRealTimeData(realTimeData);
                                    // 유저가 존재하지 않으면 redis에 상태 정보를 생성하여 저장
                                } else {
                                    realTimeData = makeRealTimeData(userDto);
                                    redisService.saveRealTimeData(realTimeData);
                                }
                            }
                            responseMessage = signalingSchedulingRequest.toString();
                        } else if (signalingSchedulingRequest.getType().matches("SCHEDULER_LAST")) {
                            List<UserDto> userList = signalingSchedulingRequest.getUsers();
                            for (UserDto userDto : userList) {
                                RealTimeData realTimeData = redisService.findRealTimeData(userDto.getUserId());
                                // 유저가 존재하면, redis에 유저의 시간 정보를 갱신함
                                if (realTimeData != null) {
                                    realTimeData.setStudyTime(userDto.getStudyTime());
                                    redisService.saveRealTimeData(realTimeData);
                                // 유저가 존재하지 않으면 redis에 상태 정보를 생성하여 저장
                                } else {
                                    realTimeData = makeRealTimeData(userDto);
                                    redisService.saveRealTimeData(realTimeData);
                                }
                            }
                            List<RealTimeData> allData = redisService.getAllRealTimeData();
                            processAndSendBatch(allData);
                            responseMessage = signalingSchedulingRequest.toString();
                        } else {
                            // 에러처리
                        }
                    }
                        break;
                case "auth":
                    TCPAuthReceiveRequest authRequest = (TCPAuthReceiveRequest) response;
                    log.warn(authRequest.toString());
                    // 유저 서버에 공부 시간 전달
                    if (authRequest.getType().matches("STUDY_TIME_FROM_TCP")) {
                        try {
                            RealTimeData realTimeData =  redisService.findRealTimeData(authRequest.getUserId());
                            if (realTimeData != null) {
                                responseMessage = sendAuthServerStudyTimeMessage(realTimeData);
                            } else {
                                realTimeData = new RealTimeData();
                                realTimeData.setUserId(authRequest.getUserId());
                                responseMessage = sendAuthServerStudyTimeMessage(realTimeData);
                            }

                        }catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                    break;
                case "room":
                    TCPRoomReceiveRequest roomRequest = (TCPRoomReceiveRequest) response;
                    log.debug(roomRequest.toString());
                    if (roomRequest.getType().matches("ALERT")) {
                        String signalingServerRoomRequestMessage = sendSignalingServerAlertMessage(roomRequest);
                        String resp = tcpSignalingClientGateway.send(signalingServerRoomRequestMessage);
                        log.debug("resq : " + signalingServerRoomRequestMessage);
                        log.debug("resp : " + resp);
                    }
                    if (roomRequest.getType().matches("KICK_OUT|KICK_OUT_BY_ALERT|DELEGATE")) {
                        // 내부적으로 server : "room" 요청 온 것이 server : "state" 로 바뀜
                        String signalingServerRoomRequestMessage = sendSignalingServerRoomMessage(roomRequest);
                        String resp = tcpSignalingClientGateway.send(signalingServerRoomRequestMessage);
                        log.debug("resq : " + signalingServerRoomRequestMessage);
                        log.debug("resp : " + resp);
                    } else {
                        // 잘못된 type 입력 에러 처리
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
        TCPRoomSendRequest tcpRoomSendRequest = TCPRoomSendRequest.builder()
                .server("state")
                .userId(rtData.getUserId())
                .roomId(rtData.getRoomId())
                .build();

        String tcpRoomOutResponseMessage = jsonConverter.convertToJson(tcpRoomSendRequest);
        return tcpRoomOutResponseMessage;
    }
    public String sendSignalingServerStudyTimeMessage(RealTimeData rtData) {
        TCPSignalingReceiveResponse tcpSignalingReceiveResponse = TCPSignalingReceiveResponse.builder()
                .userId(rtData.getUserId())
                .studyTime(rtData.getStudyTime())
                .build();

        String tcpSignalingResponseMessage = jsonConverter.convertToJson(tcpSignalingReceiveResponse);
        return tcpSignalingResponseMessage;
    }
    public String sendAuthServerStudyTimeMessage(RealTimeData realTimeData){
        TCPAuthReceiveResponse tcpAuthReceiveResponse = TCPAuthReceiveResponse.builder()
                .userId(realTimeData.getUserId())
                .studyTime(realTimeData.getStudyTime())
                .build();

        String tcpAuthResponseMessage = jsonConverter.convertToJson(tcpAuthReceiveResponse);
        return tcpAuthResponseMessage;
    }
    public String sendSignalingServerRoomMessage(TCPRoomReceiveRequest tcpRoomReceiveRequest){
        TCPSignalingSendRequest tcpSignalingSendRequest = tcpRoomReceiveRequest.toTCPSignalingSendRequest(tcpRoomReceiveRequest.getType());
        String tcpSignalingSendRequestMessage = jsonConverter.convertToJson(tcpSignalingSendRequest);
        return tcpSignalingSendRequestMessage;
    }
    public String sendSignalingServerAlertMessage(TCPRoomReceiveRequest tcpRoomReceiveRequest) {
        TCPSignalingSendAlertRequest tcpSignalingSendRequest = tcpRoomReceiveRequest.toTCPSignalingSendAlertRequest(tcpRoomReceiveRequest.getType());
        String tcpSignalingSendRequestMessage = jsonConverter.convertToJson(tcpSignalingSendRequest);
        return tcpSignalingSendRequestMessage;
    }
    public RealTimeData makeRealTimeData(TCPChatReceiveRequest chatRequest) {
        RealTimeData realTimeData = new RealTimeData();
        realTimeData.setUserId(chatRequest.getUserId());
        realTimeData.setRoomId(chatRequest.getRoomId());
        realTimeData.setSessionId(chatRequest.getSession());
        return realTimeData;
    }
    public RealTimeData makeRealTimeData(TCPSignalingReceiveRequest signalingRequest) {
        RealTimeData realTimeData = new RealTimeData();
        realTimeData.setUserId(signalingRequest.getUserId());
        realTimeData.setStudyTime(signalingRequest.getStudyTime());
        return realTimeData;
    }
    public RealTimeData makeRealTimeData(UserDto userDto) {
        RealTimeData realTimeData = new RealTimeData();
        realTimeData.setUserId(userDto.getUserId());
        realTimeData.setStudyTime(userDto.getStudyTime());

        return realTimeData;
    }
    public void processAndSendBatch(List<RealTimeData> batch) {
        ObjectMapper objectMapper = new ObjectMapper();

        // 모든 RealTimeData 객체를 UserDto 객체로 변환
        List<UserDto> userDtoBatch = batch.stream()
                .map(realTimeData -> new UserDto(realTimeData.getUserId(), realTimeData.getStudyTime()))
                .collect(Collectors.toList());
        Map<String, Object> data = new HashMap<>();
        data.put("server", "state");
        data.put("type", "SCHEDULER");
        data.put("users", userDtoBatch);

        // JSON 형태로 변환 후 TCP로 전송
        try {
            String dataAsString = objectMapper.writeValueAsString(data);

            log.debug(dataAsString);

            String response = tcpAuthClientGateway.send(dataAsString);
            redisService.deleteAllData();
//            if (response.contains("SUCCESS")) {
//                redisService.deleteAllData();
//            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
