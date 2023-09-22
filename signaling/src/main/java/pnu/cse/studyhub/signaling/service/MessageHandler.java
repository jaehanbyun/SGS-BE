package pnu.cse.studyhub.signaling.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kurento.client.IceCandidate;
import org.kurento.client.KurentoClient;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import pnu.cse.studyhub.signaling.config.tcp.TCPMessageService;
import pnu.cse.studyhub.signaling.dao.request.*;
import pnu.cse.studyhub.signaling.dao.request.tcp.TCPTimerRequest;
import pnu.cse.studyhub.signaling.dao.request.tcp.TCPUserRequest;
import pnu.cse.studyhub.signaling.dao.response.TCPOwnerResponse;
import pnu.cse.studyhub.signaling.dao.response.TCPStudyTimeResponse;
import pnu.cse.studyhub.signaling.util.Room;
import pnu.cse.studyhub.signaling.util.RoomManager;
import pnu.cse.studyhub.signaling.util.UserRegistry;
import pnu.cse.studyhub.signaling.util.UserSession;

import java.io.IOException;
import java.time.LocalTime;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
// 클라이언트로부터 메시지를 수신하고 해당 메시지를 다른 클라이언트에게 전달해줌
public class MessageHandler extends TextWebSocketHandler {

    public static final String ID = "id";
    private static final Gson gson = new GsonBuilder().create();
    private final KurentoClient kurento;
    private final UserRegistry userRegistry;

    private final RoomManager roomManager;

    private final ObjectMapper mapper;
    private final TCPMessageService tcpMessageService;

    // websocket 연결되면 WebSocketSession 추가
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("user establish websocket connection  : {}", session);
    }


    // 해당 webSocketsession으로 메시지 보내기
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {// 해당 session으로부터 message가 날라옴
        try {


            final JsonObject jsonMessage = gson.fromJson(message.getPayload(), JsonObject.class);
            final UserSession user = userRegistry.getBySession(session);

            if (user != null) {
                log.info("Incoming message from user     '{}': {}", user.getUserId(), jsonMessage);
            } else {
                log.info("Incoming message from new user: {}", jsonMessage);
            }

            switch (jsonMessage.get(ID).getAsString()) {
                // 방 접속
                case "joinRoom":
                    JoinRequest joinRequest = mapper.readValue(message.getPayload(), JoinRequest.class);
                    join(joinRequest, session);
                    log.info("User {} join {} Room ",joinRequest.getUserId(),joinRequest.getRoomId());
                    break;

                // SDP 정보 전송
                case "receiveVideoFrom":
                    log.info("SDP 정보 전송");
                    // json -> java 객체
                    ReceiveVideoRequest request
                            = mapper.readValue(message.getPayload(), ReceiveVideoRequest.class);

                    //userID 는 request 보낸 놈의 userId임
                    final String userId = request.getUserId();
                    final UserSession sender = userRegistry.getByUserId(userId);
                    final String sdpOffer = request.getSdpOffer();

                    // 즉, user는 sender로부터 SDP offer를 받고 처리해서 비디오 스트림을 받을 준비를 함
                    user.receiveVideoFrom(sender, sdpOffer);
                    break;

                // ICE Candidate 정보 전송
                case "onIceCandidate":
                    CandidateRequest candidateRequest
                            = mapper.readValue(message.getPayload(), CandidateRequest.class);
                    CandidateRequest.Candidate candidate = candidateRequest.getCandidate();

                    if (user != null) {
                        IceCandidate cand = new IceCandidate(
                                candidate.getCandidate(),
                                candidate.getSdpMid(),
                                candidate.getSdpMLineIndex());
                        user.addCandidate(cand, candidateRequest.getUserId());
                    }
                    break;

                // 비디오 설정 변경
                case "videoState":
                    VideoRequest videoRequest
                            = mapper.readValue(message.getPayload(), VideoRequest.class);
                    updateVideo(videoRequest, user);
                    log.info("User {} change videoState",videoRequest.getUserId());
                    break;

                // 오디오 설정 변경
                case "audioState":
                    AudioRequest audioRequest
                            = mapper.readValue(message.getPayload(), AudioRequest.class);
                    updateAudio(audioRequest, user);
                    log.info("User {} change videoState",audioRequest.getUserId());
                    break;

                // 타이머 설정 변경
                case "timerState":
                    // id, userid, timerState / user : userSession type
                    TimerRequest timerRequest = mapper.readValue(message.getPayload(), TimerRequest.class);
                    updateTimer(timerRequest, user);
                    log.info("User {} change timerState",user.getUserId());
                    break;

                // 방 나가기 : 그냥 방나가면 웹소켓 끊어주면 됨
//                case "leaveRoom":
//                    leave(user);
//                    break;

                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // websocket 연결 끊길때
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("[ws] Session has been closed with status [{} {}]", status, session);
        // userRegistry에서 삭제하고 유저 세션 객체 들고옴
        final UserSession user = userRegistry.removeBySession(session);
        if (Objects.isNull(user)) return;

        // user의 타이머가 켜져 있는 상태일 때
        if(user.getTimer()){
            user.setTimer(false);
            user.countStudyTime(LocalTime.now(),user.getOnTime());
        }

        // 유저의 공부시간 상태관리서버로 보내기
        // 방장이면 CHANGE / 일반 유저 or 방 사라질때 KEEP

        String response = userStudyTimeToTCP(user);
        String res = TCPConverter(response);

        // 현재 방
        final Room room = roomManager.onlyGetRoom(user.getRoomId());
        // 현재 방에서 유저 지우기, 유저도 userSession close하기
        room.leave(user);
        // 만약 방에 아무도 없으면 해당 방 지우기
        if (room.getParticipants().isEmpty()) {
            roomManager.removeRoom(room);

            kurento.getServerManager().getPipelines().stream()
                    .filter(pipeline -> pipeline.getId().equals(room.getPipeLineId()))
                    .findAny().ifPresent(pipeline -> pipeline.release());
        }else{ // 방에 누군가 존재하면 (1명일때도)
            Gson gson = new Gson();
            TCPOwnerResponse tcpOwnerResponse = gson.fromJson(res, TCPOwnerResponse.class);
            if(tcpOwnerResponse.getType().equals("CHANGE")){
                // tcpOwnerResponse.getUserId() 에서 받은 userId가 새로운 방장임을 알림
                room.delegateOwner("DELEGATE",tcpOwnerResponse.getUserId());
            }
        }

    }

//    private void leave(UserSession user) throws IOException {
//
//        if(user.getTimer()){ // 타이머가 on이라면
//            user.setTimer(false);
//            ValueOperations<String, String> userTime = redisTemplate.opsForValue();
//            user.countStudyTime(LocalTime.now(),user.getOnTime());
//            userTime.set(user.getUserId(), user.studyTimeToString());
//        }
//
//        //userStudyTimeToTCP(user);
//
//        final Room room = roomManager.getRoom(user.getRoomId());
//        room.leave(user);
//
//
//        if (room.getParticipants().isEmpty()) {
//            roomManager.removeRoom(room);
//
//            // kurento media pipeline 삭제
//            kurento.getServerManager().getPipelines().stream()
//                    .filter(pipeline -> pipeline.getId().equals(room.getPipeLineId()))
//                    .findAny().ifPresent(pipeline -> pipeline.release());
//        }
//    }

    private void join(JoinRequest request, WebSocketSession session) throws IOException {

        final String userId = request.getUserId();
        final Long roomId = request.getRoomId();
        final boolean video = request.isVideo();
        final boolean audio = request.isAudio();

        //LocalTime studyTime = LocalTime.of(0,0,0);
        String userStudyTime = userStudyTimeFromTCP(userId);

        TCPStudyTimeResponse response = mapper.readValue(TCPConverter(userStudyTime), TCPStudyTimeResponse.class);
        LocalTime studyTime;
        if(response.getStudyTime() == null){
            studyTime = LocalTime.of(0,0,0);
        }else{
            studyTime = LocalTime.parse(response.getStudyTime());
        }

        Room room = roomManager.getRoom(roomId);
        final UserSession newUser = room.join(userId, session, video, audio,studyTime);

        if (Objects.isNull(newUser)) return;
        userRegistry.register(newUser);

    }

    private void updateVideo(VideoRequest request, UserSession user) throws IOException {
        final Room room = roomManager.getRoom(user.getRoomId());
        room.updateVideo(request);
    }

    private void updateAudio(AudioRequest request, UserSession user) throws IOException {
        final Room room = roomManager.getRoom(user.getRoomId());
        room.updateAudio(request);
    }

    private void updateTimer(TimerRequest request, UserSession user) throws IOException {
        log.info("[in {} room] {}'s timer request : timerState - {} , time - {}",user.getRoomId(),user.getUserId(),request.isTimerState(),request.getTime());
        final Room room = roomManager.getRoom(user.getRoomId());
        room.updateTimer(request,user);

    }

    private String userStudyTimeToTCP(UserSession user) {
        String tcpMessage;
        // TCP 서버로 userId랑 studyTime 보내줌
        tcpMessage = TCPTimerRequest.builder()
                .server("signaling")
                .type("STUDY_TIME_TO_TCP")
                .userId(user.getUserId())
                .studyTime(user.studyTimeToString())
                .build().toString();

        log.info("[tcp to state] {} user's studyTime : {}",user.getUserId(),user.studyTimeToString());
        return tcpMessageService.sendMessage(tcpMessage);
    }

    private String userStudyTimeFromTCP(String userId) {
        String tcpMessage;
        // TCP 서버로 userId보내고 userId랑 studyTime을 response로 받음
        tcpMessage = TCPUserRequest.builder()
                .server("signaling")
                .type("STUDY_TIME_FROM_TCP")
                .userId(userId)
                .build().toString();

        log.info("[tcp from state] request {} user's studyTime",userId);
        return tcpMessageService.sendMessage(tcpMessage);

    }

    public String TCPConverter(String response){

        String[] arr = response.split(",");
        StringBuilder sb = new StringBuilder();
        for(String code : arr){
            int ascii = Integer.parseInt(code.trim());
            sb.append((char) ascii);
        }

        String res = sb.toString();

        log.info(res);

        return res;
    }

}
