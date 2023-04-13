package pnu.cse.studyhub.signaling.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Component
@RequiredArgsConstructor
// 클라이언트로부터 메시지를 수신하고 해당 메시지를 다른 클라이언트에게 전달해줌
public class MessageHandler extends TextWebSocketHandler {

    public static final String ID = "id";
    private static final Gson gson = new GsonBuilder().create();

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

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {// 해당 session으로부터 message가 날라옴
        try {

            final JsonObject jsonMessage = gson.fromJson(message.getPayload(), JsonObject.class);


//            final UserSession user = registry.getBySession(session);

//            if (user != null) {
//                log.info("Incoming message from user     '{}': {}", user.getUserId(), jsonMessage);
//            } else {
//                log.info("Incoming message from new user: {}", jsonMessage);
//            }

            switch (jsonMessage.get(ID).getAsString()) {
                // 방 접속
                case "JOIN":
                    break;
                // SDP 정보 전송
                case "RECEIVE_VIDEO_FROM":
                    break;
                // ICE Candidate 정보 전송
                case "ON_ICE_CANDIDATE":
                    break;
                // 비디오 설정 변경
                case "VIDEO_STATE_FROM":
                    break;
                // 오디오 설정 변경
                case "AUDIO_STATE_FROM":
                    break;
                // 방 나가기
                case "LEAVE":
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}
