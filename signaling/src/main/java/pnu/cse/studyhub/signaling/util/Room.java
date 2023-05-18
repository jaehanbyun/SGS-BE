package pnu.cse.studyhub.signaling.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.kurento.client.Continuation;
import org.kurento.client.MediaPipeline;
import org.kurento.jsonrpc.JsonUtils;
import org.springframework.web.socket.WebSocketSession;
import pnu.cse.studyhub.signaling.dao.request.AudioRequest;
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
                newUser.getUserId(), newUser.getVideo(), newUser.getAudio(), false, newUser.getStudyTime(),null);

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

    private void removeParticipant(String userId) throws IOException {
        participants.remove(userId);

        final List<String> unnotifiedParticipants = new ArrayList<>();
        final JsonObject participantLeftJson = participantLeft(userId);
        for (final UserSession participant : participants.values()) {
            try {
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
                        participant.getUserId(), participant.getVideo(), participant.getAudio(),participant.getTimer(),participant.getStudyTime(),participant.getOnTime());
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
