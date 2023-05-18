package pnu.cse.studyhub.signaling.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kurento.client.IceCandidate;
import org.kurento.client.KurentoClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import pnu.cse.studyhub.signaling.config.tcp.TCPMessageService;
import pnu.cse.studyhub.signaling.dao.request.*;
import pnu.cse.studyhub.signaling.util.Room;
import pnu.cse.studyhub.signaling.util.RoomManager;
import pnu.cse.studyhub.signaling.util.UserRegistry;
import pnu.cse.studyhub.signaling.util.UserSession;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

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

    private final RedisTemplate redisTemplate;
    private final ObjectMapper mapper;
    public static final String PIPELINE = "-pipeline";

    // redis 저장 유효기간을 하루로 설정
    private static final long TIME = 24 * 60 * 60 * 1000L;

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
                    log.info("방접속");
                    JoinRequest joinRequest = mapper.readValue(message.getPayload(), JoinRequest.class);
                    join(joinRequest, session);
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
                    log.info("ICE Candidate 정보 받음");
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
                    break;

                // 오디오 설정 변경
                case "audioState":
                    AudioRequest audioRequest
                            = mapper.readValue(message.getPayload(), AudioRequest.class);
                    updateAudio(audioRequest, user);
                    break;

                // 타이머 설정 변경
                case "timerState":
                    // id, userid, timerState / user : userSession type
                    TimerRequest timerRequest = mapper.readValue(message.getPayload(), TimerRequest.class);
                    updateTimer(timerRequest, user);
                    break;

                // 방 나가기
                case "leaveRoom":
                    leave(user);
                    break;

                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // TODO : 여기 수정 필수 !!
    // websocket 연결 끊길때
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("[ws] Session has been closed with status [{} {}]", status, session);
        UserSession user = userRegistry.removeBySession(session);

        if(user.getTimer()){ // 만약 타이머가 On 상태이면
            user.setTimer(false);
            String tcpMessage = TCPTimerRequest.builder()
                    .type("TIMER_OFF")
                    .userId(user.getUserId())
                    .time(LocalDateTime.now().toString())
                    .build().toString();

            tcpMessageService.sendMessage(tcpMessage);
            log.info("[tcp to state] {}'s timer stopped.",user.getUserId());
        }
        if (Objects.isNull(user)) return;
        Room room = roomManager.getRoom(user.getRoomId());
        room.leave(user);


        if (room.getParticipants().isEmpty()) {
            roomManager.removeRoom(room);

            // kurento media pipeline 삭제
            kurento.getServerManager().getPipelines().stream()
                    .filter(pipeline -> pipeline.getId().equals(room.getPipeLineId()))
                    .findAny().ifPresent(pipeline -> pipeline.release());
        }
    }


    private void join(JoinRequest request, WebSocketSession session) throws IOException {

        final String userId = request.getUserId();
        final Long roomId = request.getRoomId();
        final boolean video = request.isVideo();
        final boolean audio = request.isAudio();

        // TODO : 여기서 User 생성해줄 때 TCP 서버에 해당 userId의 공부시간 요청해야할듯?
        //          그렇게 되면 응답 올때까지 기다려야 하나..? redis로 시그널링 서버에서 관리?
        //          비동기로 처리..? 일단 redis로 하자

        ValueOperations<String, LocalTime> userTime = redisTemplate.opsForValue();
        LocalTime studyTime;

        if(redisTemplate.hasKey(userId)){
            studyTime = userTime.get(userId);
        }else{
            studyTime = LocalTime.of(0,0,0);
        }

        Room room = roomManager.getRoom(roomId);

        final UserSession newUser = room.join(userId, session, video, audio,studyTime);

        if (Objects.isNull(newUser)) return;
        userRegistry.register(newUser);

    }

    private void leave(UserSession user) throws IOException {
        final Room room = roomManager.getRoom(user.getRoomId());
        // room 매니저로 룸을 가져 온 후에 room에서 해당 user를 없앰
        room.leave(user);
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
        String tcpMessage;

        if (request.isTimerState()) { // Timer Start 누를 때
            user.setTimer(true);
            tcpMessage = TCPTimerRequest.builder()
                    .type("TIMER_ON")
                    .userId(request.getUserId())
                    .time(LocalDateTime.now().toString())
                    .build().toString();

            log.info("[tcp to state] {}'s timer has started.",user.getUserId());

        }else{ // Timer Stop 누를 때
            user.setTimer(false);
            tcpMessage = TCPTimerRequest.builder()
                    .type("TIMER_OFF")
                    .userId(request.getUserId())
                    //.session(session.getId())
                    .time(LocalDateTime.now().toString())
                    .build().toString();

            log.info("[tcp to state] {}'s timer stopped.",user.getUserId());
        }

        tcpMessageService.sendMessage(tcpMessage);


    }
}
