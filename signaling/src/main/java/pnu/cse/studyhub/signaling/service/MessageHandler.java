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
import pnu.cse.studyhub.signaling.dao.request.*;
import pnu.cse.studyhub.signaling.util.Room;
import pnu.cse.studyhub.signaling.util.RoomManager;
import pnu.cse.studyhub.signaling.util.UserRegistry;
import pnu.cse.studyhub.signaling.util.UserSession;

import java.io.IOException;
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

    // WebSocketSession 객체의 목록
    List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    // websocket 연결되면 WebSocketSession 추가
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
    }

    // 메시지를 브로드캐스트 함
//    @Override
//    public void handleTextMessage(WebSocketSession session, TextMessage message){
//        for (WebSocketSession webSocketSession : sessions) {
//            if (webSocketSession.isOpen() && !session.getId().equals(webSocketSession.getId())) {
//                webSocketSession.sendMessage(message);
//            }
//        }
//    }

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
                    break;

                // SDP 정보 전송
                case "receiveVideo":
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
                    break;

                // 오디오 설정 변경
                case "audioState":
                    AudioRequest audioRequest
                            = mapper.readValue(message.getPayload(), AudioRequest.class);
                    updateAudio(audioRequest, user);
                    break;

                // 타이머 설정 변경
                case "timerState":
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

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {

        UserSession user = userRegistry.removeBySession(session);
        if (Objects.isNull(user)) return;
        Room room = roomManager.getRoom(user.getRoomId());
        room.leave(user);

        // redis에서 유저 삭제
        SetOperations<Long, String> setOperations = redisTemplate.opsForSet();
        try {
            log.info("[redis] remove key : {}, value : {}", room.getRoomId(), user.getUserId());
            setOperations.remove(room.getRoomId(), user.getUserId());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (room.getParticipants().isEmpty()) {
            roomManager.removeRoom(room);

            try {
                log.info("[redis] remove key : {}", room.getRoomId());
                redisTemplate.delete(room.getRoomId());
                redisTemplate.delete(room.getRoomId()+PIPELINE);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // kurento media pipeline 삭제
            kurento.getServerManager().getPipelines().stream()
                    .filter(pipeline -> pipeline.getId().equals(room.getPipeLineId()))
                    .findAny().ifPresent(pipeline -> pipeline.release());

//            try {
//                log.info("[redis] remove key : {}, value : {}", SERVER + IP, room.getRoomId());
//                setOperations.remove(SERVER + IP, room.getRoomId());
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
        }

        // TODO : 상태관리 서버로 접속 정보 전송
    }


    private void join(JoinRequest request, WebSocketSession session) throws IOException {
        // TODO : 토큰

        final String userId = request.getUserId();
        final Long roomId = request.getRoomId();
        final boolean video = request.isVideo();
        final boolean audio = request.isAudio();

        // roomManager에서 roomId를 매개변수로 room을 받아옴
        // 해당 room에 있는 멤버들에 대한 sdp, ice candidate 관련 정보 필요
        Room room = roomManager.getRoom(roomId);

        // 해당 room에 join한다고 request를 보낸 user에 대한 내용을 집어 넣음
        final UserSession newUser = room.join(userId, session, video, audio);
        // TODO : 최대 접속 인원 초과 시 입장 제한을 어디서 해줘야하지 (누가 동시에 하면..?)
        if (Objects.isNull(newUser)) return;
        userRegistry.register(newUser);


        try {
            log.info("[redis] save key : {}, value : {}", roomId+"d", room.getPipeLineId());
            // 레디스에 roomId+Pipeline, room.getPiptelineid를 저장
            // room마다 MediaPipeline의 Id 값은 수정되지 않는 값이라 캐시에 두면 좋은 성능을 볼 수 있음
            ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
            valueOperations.set(roomId + PIPELINE, room.getPipeLineId(), TIME, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            log.info("[redis] save key : {}, value : {}", roomId, userId);
            //log.info("[redis] save key : {}, value : {}", SERVER + IP, roomId);
            // redis에 roomId를 key로 하고 userId를 집어 넣음 (해당 room에 있는 user들)
            // Server+IP를 key로 하고 roomId를 집어 넣음
            SetOperations<Long, String> setOperations = redisTemplate.opsForSet();
            setOperations.add(roomId, userId);
            // IP에 대해 room id를 가지고 있음 (시그널링 서버 여러개 일때)
            //setOperations.add(SERVER + IP, roomId);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        // TODO
    }

}
