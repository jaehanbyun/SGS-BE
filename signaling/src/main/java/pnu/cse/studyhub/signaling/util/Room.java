package pnu.cse.studyhub.signaling.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.kurento.client.Continuation;
import org.kurento.client.MediaPipeline;
import org.kurento.jsonrpc.JsonUtils;
import org.springframework.cglib.core.Local;
import org.springframework.web.socket.WebSocketSession;
import pnu.cse.studyhub.signaling.dao.request.AudioRequest;
import pnu.cse.studyhub.signaling.dao.request.TimerRequest;
import pnu.cse.studyhub.signaling.dao.request.VideoRequest;
import pnu.cse.studyhub.signaling.dao.response.ParticipantResponse;

import javax.annotation.PreDestroy;
import java.io.Closeable;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static pnu.cse.studyhub.signaling.util.Message.*;

@Slf4j
public class Room implements Closeable {

    private final Long roomId;

    private final MediaPipeline pipeline;

    private final ConcurrentMap<String, UserSession> participants = new ConcurrentHashMap<>();

    public Room(Long roomId, MediaPipeline pipeline) {
        this.roomId = roomId;
        this.pipeline = pipeline;
    }

    public Long getRoomId() {
        return this.roomId;
    }

    public String getPipeLineId() {
        return this.pipeline.getId();
    }

    @PreDestroy
    private void shutdown() {
        this.close();
    }

    public UserSession join(String userId, WebSocketSession session, boolean video, boolean audio, LocalTime studyTime) throws IOException {
        // TODO : 여기서 redis 사용하자 ??

        final UserSession participant = new UserSession(userId,this.roomId, session, this.pipeline, video, audio,studyTime);

        joinRoom(participant);
        participants.put(participant.getUserId(), participant);

        sendParticipantNames(participant);
        return participant;
    }

    public Collection<UserSession> getParticipants() {
        return participants.values();
    }

    public void leave(UserSession user) throws IOException {
        this.removeParticipant(user.getUserId());
        user.close();
    }

    // room에 새 참가자를 추가하고 방에 있는 다른 모든 참가자에게 새 참가자의 도착을 알리는 것
    // TODO 코드 다시
    private Collection<String> joinRoom(UserSession newUser) throws IOException {

        ParticipantResponse participantResponse = new ParticipantResponse(
                newUser.getUserId(), newUser.getVideo(), newUser.getAudio(), newUser.getTimer(), newUser.studyTimeToString(),null);

        final JsonElement participant = JsonUtils.toJsonObject(participantResponse);

        final List<String> participantsList = new ArrayList<>(participants.values().size());

        for (final UserSession user : participants.values()) {
            try {
                user.sendMessage(newParticipantArrived(participant));
            } catch (final IOException e) {
                log.debug("ROOM {}: participant {} could not be notified", roomId, user.getUserId(), e);
            }
            participantsList.add(user.getUserId());
        }
        return participantsList;
    }

    // 특정 유저가 방에서 나갔을 때, 그 유저가 떠난 사실을 해당 방에 참여하고 있는 모든 유저들에게 알림

    public void removeParticipant(String userId) throws IOException {
        // 해당 방의 참가자들(participants) 중에 방을 나간 userId를 삭제하고
        participants.remove(userId);

        final List<String> unnotifiedParticipants = new ArrayList<>();
        final JsonObject participantLeftJson = participantLeft(userId);
        // 남아 있는 참가자들한테 해당 userId가 나간사실을 알림
        for (final UserSession participant : participants.values()) {
            try {
                // 남아 있는 참가자의 incomingMedia에서 userId를 없애줘야함
                participant.cancelVideoFrom(userId);
                participant.sendMessage(participantLeftJson);
            } catch (final IOException e) {
                unnotifiedParticipants.add(participant.getUserId());
            }
        }

        if (!unnotifiedParticipants.isEmpty()) {
            log.debug("ROOM {}: The users {} could not be notified that {} left the room", this.roomId,
                    unnotifiedParticipants, userId);
        }
    }

    // 새로운 유저 방 접속시 기존 유저들에 대한 정보를 새로운 유저한테 전달
    public void sendParticipantNames(UserSession user) throws IOException {

        final JsonArray participantsArray = new JsonArray();
        for (final UserSession participant : participants.values()) {
            if (!participant.equals(user)) {
                ParticipantResponse participantResponse = new ParticipantResponse(
                        participant.getUserId(), participant.getVideo(), participant.getAudio(),participant.getTimer(),participant.studyTimeToString(), participant.onTimeToString());
                final JsonElement participantInfo = JsonUtils.toJsonObject(participantResponse);
                participantsArray.add(participantInfo);
            }
        }

        user.sendMessage(existingParticipants(participantsArray));
    }

    public void updateVideo(VideoRequest request) throws IOException {
        final UserSession user = participants.get(request.getUserId());
        user.setVideo(request.isVideo());

        final JsonObject updateVideoStateJson = videoStateAnswer(request.getUserId(), request.isVideo());
        for (final UserSession participant: participants.values()) {
            participant.sendMessage(updateVideoStateJson);
        }
    }

    public void updateAudio(AudioRequest request) throws IOException {

        final UserSession user = participants.get(request.getUserId());
        user.setAudio(request.isAudio());

        final JsonObject updateAudioStateJson = audioStateAnswer(request.getUserId(), request.isAudio());
        for (final UserSession participant: participants.values()) {
            participant.sendMessage(updateAudioStateJson);
        }
    }

    /*
        젤 처음 들어오면 각 유저의 (userId, videoState, audioState, 공부시간, 타이머상태, 타이머 누른 시간)을 받을거임.
            Off 유저 : (공부시간) 화면에 출력
            On 유저 : (공부시간) + (현재시각) - (On 누른 시각)를 화면에 출력

        유저가 Timer 버튼을 누르면 TimerRequest(id, userId, timerState, time)가 서버로 날라옴
             Off -> On 누른 유저 : TimerRequest(id, userId, timerState:True, time(On 누른 시간))
             On -> Off 누른 유저 : TimerRequest(id, userId, timerState:False, time(Off 누른 시간))

        해당 UserSession의 변수에 값을 바꿔주고 (set)
            Off -> On 누른 유저 : timer, studyTime, onTime 변수 입력 // studyTime 동기화 문제 없나?
                                timer = True , studyTime = 그대로 , onTime = TimerRequest.time
            On -> Off 누른 유저 : timer, studyTime, onTime 변수 입력
                                timer = False, studyTime = studyTime + TimerRequest.time(Off 누른 시각) - onTime(On 눌렀던 시각)
                                그리고 redis에 userId Key로 studyTime 저장하기 (제일 처음 유저 생성될 때 불러오기 위해서)

        해당 방에 있는 모든유저(본인 포함)에게 timerStateAnswer을 보내줌
            만약 Timer On 요청이면 timeStateAnswer(id, userId, timer, time)을 보내주고
            만약 Timer Off 요청이면 timeStateAnswer(id, userId, timer, studyTime) 보내주기

        유저는 timeStateAnswer를 받아서 출력
        
        <화면 출력>    
        Off -> On 누른 유저 : (공부시간) + (현재시각) - (timerStateAnswer.time)를 화면에 출력
        On -> Off 누른 유저 : (timerStateAnswer.studyTime) 화면에 출력


        웹 소켓 끊겼을 때 (브라우저 종료)
            변수 기반으로 redis에 userId Key로 studyTime만 저장하면 될 듯
     */

    // 여기서 request.getTime이 hh:mm:ss 형식의 String
    // TODO : 자 타이머를 시작한 것부터 차근차근해보자!
    public void updateTimer(TimerRequest request, UserSession user) throws IOException {
        //request : timerState, time
        //final UserSession user = participants.get(request.getUserId());
        final JsonObject updateTimerStateJson;

        user.setTimer(request.isTimerState());

        if(!user.getTimer()){ // 유저가 타이머를 멈추면
            log.info("user studyTime count before : {} / {}",LocalTime.parse(request.getTime()),user.getOnTime());
            user.countStudyTime(LocalTime.parse(request.getTime()),user.getOnTime());
            log.info("{}'s studyTime : {} / timerState : {}",user.getUserId(),user.getStudyTime(),user.getTimer());

        }
        // 타이머 상태 상관 없나..?
        user.setOnTime(LocalTime.parse(request.getTime()));



        if(!user.getTimer()){ // 유저가 타이머를 멈추면
            updateTimerStateJson = timerStateAnswer(user.getUserId(), user.getTimer(), user.studyTimeToString());
        }else{ // 유저가 타이머를 시작 시키면
            updateTimerStateJson = timerStateAnswer(user.getUserId(), user.getTimer(),user.onTimeToString());
        }

        for (final UserSession participant: participants.values()) {
            participant.sendMessage(updateTimerStateJson);
        }
    }


    // Room이 close 될때 모든 userSession을 close
    // 해당 participants 도 clear하고
    // pipeline도 메모리에서 비동기적으로 release 해준다
    @Override
    public void close() {
        for (final UserSession user : participants.values()) {
            try {
                user.close();
            } catch (IOException e) {
                log.debug("ROOM {}: Could not invoke close on participant {}", this.roomId, user.getUserId(), e);
            }
        }

        participants.clear();
        pipeline.release(new Continuation<Void>() {

            @Override
            public void onSuccess(Void result) throws Exception { }

            @Override
            public void onError(Throwable cause) throws Exception { }
        });
    }

}
