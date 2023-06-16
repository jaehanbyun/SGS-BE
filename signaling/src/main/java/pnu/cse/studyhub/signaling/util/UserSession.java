package pnu.cse.studyhub.signaling.util;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.kurento.client.*;
import org.kurento.jsonrpc.JsonUtils;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static pnu.cse.studyhub.signaling.util.Message.iceCandidate;
import static pnu.cse.studyhub.signaling.util.Message.receiveVideoAnswer;

@Slf4j
public class UserSession implements Closeable {

    private final String userId;
    private final WebSocketSession session;
    private final MediaPipeline pipeline;

    private final Long roomId;
    private boolean video;
    private boolean audio;
    private boolean timer;

    // TODO : 나중에 수정 될수도

    private LocalTime studyTime;
    private LocalTime onTime;

    /////

    // 현재 나의 webRtcEndPoint 객체니깐 밖으로 내보낸다는 의미
    private final WebRtcEndpoint outgoingMedia;

    // 나와 연결되어야 할 다른 사람의 webRtcEndPoint 객체들이라 map 형태로 저장
    private final ConcurrentMap<String, WebRtcEndpoint> incomingMedia = new ConcurrentHashMap<>();

    public UserSession(String userId, Long roomId, WebSocketSession session, MediaPipeline pipeline, boolean video, boolean audio, LocalTime studyTime) {

        this.userId = userId;
        this.session = session;
        this.pipeline = pipeline;
        this.roomId = roomId;
        this.video = video;
        this.audio = audio;

        // Timer 관련
        this.timer = false;
        this.studyTime = studyTime;
        this.onTime = null;

        this.outgoingMedia = new WebRtcEndpoint.Builder(pipeline).build();
        this.outgoingMedia.addIceCandidateFoundListener(new EventListener<IceCandidateFoundEvent>() {
            // iceCandidateFounder 이벤트 리스너 등록
            // 이벤트가 발생했을 때 다른 유저들에게 새로운 iceCandidate 후보를 알림
            @Override
            public void onEvent(IceCandidateFoundEvent event) {
                JsonObject response = iceCandidate(userId, JsonUtils.toJsonObject(event.getCandidate()));
                try {
                    // 여러 개의 스레드에서 동시에 session 객체에 접근하는 것을 막음
                    synchronized (session) {
                        session.sendMessage(new TextMessage(response.toString()));
                    }
                } catch (IOException e) {
                    log.debug(e.getMessage());
                }
            }
        });
    }



    public String getUserId() {
        return userId;
    }

    public WebSocketSession getSession() {
        return session;
    }

    public MediaPipeline getPipeline() {
        return pipeline;
    }

    public Long getRoomId() {
        return roomId;
    }

    public WebRtcEndpoint getOutgoingWebRtcPeer() {
        return outgoingMedia;
    }

    public boolean getVideo() { return this.video; }

    public void setVideo(boolean video) {
        this.video = video;
    }
    public boolean getAudio() { return this.audio; }

    public void setAudio(boolean audio) {
        this.audio = audio;
    }
    public boolean getTimer() {return this.timer;}
    public void setTimer(boolean timer) { this.timer = timer; }

    public LocalTime getStudyTime() {return studyTime;}

    public void setStudyTime(LocalTime studyTime) {this.studyTime = studyTime;}

    public LocalTime getOnTime() {return onTime;}

    public void setOnTime(LocalTime onTime) {this.onTime = onTime;}
    public void countStudyTime(LocalTime offTime, LocalTime onTime){

        onTime = onTime.minusHours(offTime.getHour())
                .minusMinutes(offTime.getMinute())
                .minusSeconds(offTime.getSecond());

        this.studyTime = this.studyTime.minusHours(onTime.getHour())
                .minusMinutes(onTime.getMinute())
                .minusSeconds(onTime.getSecond());

    }


    /*
         - SDP : 미디어 스트림 전송에 필요한 많은 정보 포함
                WebRTC 통신을 위해서는 먼저 SDP를 교환해야 함 (브라우저 끼리는 Offer/Answer 모델)
                SDP는 미디어 스트림을 통해 전송되는 미디어 데이터의 형식,코덱,대역폭제한등을 정의
                Offer는 SDP 프로토콜의 형식에 따라 생성된 정보로 상대방에게 미디어코덱, 대역폭, 포트번호등을 전달
                Answer은 Offer 기반으로 브라우저가 생성하는 정보

         - ICE Candidate : 네트워크 주소(IP 주소와 포트) 및 전송 프로토콜(UDP, TCP 등)을 포함하는 객체
                (NAT나 방화벽등 제한적인 환경에서도 연결 가능하게 함)
                ICE Agent가 본인의 사용 가능한 IP 주소와 포트를 다른 사람한테 ICE Candidate로 제공해줌

         SDP안에 포함된 ICE Candidate 정보를 통해 원격 피어에 대한 연결을 설정할 수 있음

         receiveVideoFrom을 통해 UserSession이랑 SDP를 전달

         UserSession.receiveVideoFrom()을 사용하면 sender의 userSession과 SDP를 받아서
         1. sender의 endpoint를 가져온 후 SDP offer를 처리
         2. Res-3 : SDP 정보에 대한 응답을 sender에게 보냄
         3. 마지막으로 sender의 endpoint를 가져와 gatherCandidates로 후보자를 수집.

         + 각 유저는 자신의 브라우저에서 동작하는 WebRTC EndPoint를 가지고 있고 이걸 통해 미디어 스트림 송수신 하고 처리
         처리 하기 위해서 ICE Candidate를 통해 NAT를 넘어 연결도 가능
     */

    public void receiveVideoFrom(UserSession sender, String sdpOffer) throws IOException {
        log.info("receiveVideoFrom------>");
        // 해당 sender의 WebRtcEndpoint를 가져와 SDP offer를 처리해서 sdpAnser를 만듬
        final String ipSdpAnswer = this.getEndpointForUser(sender).processOffer(sdpOffer);
        final JsonObject scParams = receiveVideoAnswer(sender.getUserId(), ipSdpAnswer);
        this.sendMessage(scParams);
        this.getEndpointForUser(sender).gatherCandidates();
        log.info("<------receiveVideoFrom");
    }

    /*
        userSession을 통해서 해당 유저의 WebRTCEndPoint 객체를 가져옴
     */
    public WebRtcEndpoint getEndpointForUser(final UserSession sender) {
        // 보내는 유저가 this.user이면, 즉 sdpOffer 제안을 보내는 쪽과 받는 쪽이 동일하면
        // 그대로 outgoingMedia를 return
        if (sender.getUserId().equals(userId)) {
            return outgoingMedia;
        }

        // 나와 연결되어야 할 다른 사람의 webRtcEndPoint 객체들이라 map 형태로 저장하고 관리
        // sender의 WebRtcEndpoint가 incoming임
        WebRtcEndpoint incoming = incomingMedia.get(sender.getUserId());

        // 해당 유저에 대한 WebRtcEndpoint 객체를 가지고 있지 않으면 새로 만들어서 넣어줌
        if (incoming == null) {
            incoming = new WebRtcEndpoint.Builder(pipeline).build();
            incoming.addIceCandidateFoundListener(new EventListener<IceCandidateFoundEvent>() {

                // 새로운 WebRtcEndpoint 객체를 만들고 ICE Candidate를 발견할 때마다 처리
                @Override
                public void onEvent(IceCandidateFoundEvent event) {
                    JsonObject response = iceCandidate(sender.getUserId(), JsonUtils.toJsonObject(event.getCandidate()));
                    try {
                        synchronized (session) {
                            session.sendMessage(new TextMessage(response.toString()));
                        }
                    } catch (IOException e) {
                        log.debug(e.getMessage());
                    }
                }
            });
            incomingMedia.put(sender.getUserId(), incoming);
        }

        // 연결
        sender.getOutgoingWebRtcPeer().connect(incoming);
        return incoming;
    }

    public void cancelVideoFrom(final UserSession sender) {
        this.cancelVideoFrom(sender.getUserId());
    }

    public void cancelVideoFrom(final String senderName) {
        System.out.println("=============>>>>>>");
        System.out.println(this.userId + "'s incomingMedia : "+incomingMedia.toString());
        if(incomingMedia.containsKey(senderName)){
            System.out.println("노에러!!");
        }else{
            // 안에 없으면
            System.out.println("에러!!");
        }
        final WebRtcEndpoint incoming = incomingMedia.remove(senderName);
        System.out.println(this.userId + "'s incomingMedia : "+incomingMedia.toString());
        System.out.println("<<<<<<=============");

        // TODO : 그냥 브라우저 종료는 에러 x , leave room 했을때
        incoming.release(new Continuation<Void>() {
            @Override
            public void onSuccess(Void result) throws Exception { }
            @Override
            public void onError(Throwable cause) throws Exception { }
        });
    }

    @Override
    public void close() throws IOException {
        for (final String remoteParticipantName : incomingMedia.keySet()) {
            final WebRtcEndpoint ep = this.incomingMedia.get(remoteParticipantName);
            ep.release(new Continuation<Void>() {
                @Override
                public void onSuccess(Void result) throws Exception { }
                @Override
                public void onError(Throwable cause) throws Exception { }
            });
        }

        outgoingMedia.release(new Continuation<Void>() {
            @Override
            public void onSuccess(Void result) throws Exception { }
            @Override
            public void onError(Throwable cause) throws Exception { }
        });
    }

    // 서버에서 클라이언트 측으로 메시지 보냄
    public void sendMessage(JsonObject message) throws IOException {
        synchronized (session) {
            session.sendMessage(new TextMessage(message.toString()));
        }
    }

    public void addCandidate(IceCandidate candidate, String userId) {
        if (this.userId.compareTo(userId) == 0) {
            outgoingMedia.addIceCandidate(candidate);
        } else {
            WebRtcEndpoint webRtc = incomingMedia.get(userId);
            if (webRtc != null) {
                webRtc.addIceCandidate(candidate);
            }
        }
    }

    // userSession 객체가 user.getStudyTime().toString() 이렇게해서 자기의 studyTime을 String으로 바꿈
    // 메소드 하나 만들어주자 (초까지 출력)
    // 00:00:00 이런식으로 String 출력하기 (StringBuffer)
    public String studyTimeToString(){

        DateTimeFormatter formatter =  DateTimeFormatter.ofPattern("HH:mm:ss");
        String formattedTime = this.studyTime.format(formatter);

        return formattedTime;
    }

    public String onTimeToString(){

        if(this.onTime != null){
            DateTimeFormatter formatter =  DateTimeFormatter.ofPattern("HH:mm:ss");
            String formattedTime = this.onTime.format(formatter);

            return formattedTime;
        }
        else{ // null이면
            return "";
        }

    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof UserSession)) {
            return false;
        }
        UserSession other = (UserSession) obj;
        boolean eq = userId.equals(other.userId);
        eq &= roomId.equals(other.roomId);
        return eq;
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + userId.hashCode();
        result = 31 * result + roomId.hashCode();
        return result;
    }


}
