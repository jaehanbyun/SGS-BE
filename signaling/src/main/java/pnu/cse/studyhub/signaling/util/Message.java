package pnu.cse.studyhub.signaling.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.time.LocalTime;

public class Message {

    // Res-0 : 본인의 정보 받아오기 (userId, studyTime만??)
    public static JsonObject myInfo(JsonElement myInfo) {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", "myInfo");
        jsonObject.add("member", myInfo);
        return jsonObject;
    }

    // Res-1 : 새로운 유저 방접속 시 기존 유저들에 대한 정보(userId, video, audio, timer, studyTime, onTime)를 새로운 유저에게 전달
    public static JsonObject existingParticipants(JsonArray participantsArray) {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", "existingParticipants");
        jsonObject.add("members", participantsArray);
        return jsonObject;
    }

    // Res-2 : 새로운 유저 방접속 시 기존 유저들에게 새로운 유저에 대한 정보(userId, video, audio, timer=false, studyTime, onTime = null) 전달
    public static JsonObject newParticipantArrived(JsonElement participantInfo) {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", "newParticipantArrived");
        jsonObject.add("member", participantInfo);
        return jsonObject;
    }

    // Res-3 : SDP 정보에 대한 응답
    public static JsonObject receiveVideoAnswer(String userId, String ipSdpAnswer) {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", "receiveVideoAnswer");
        jsonObject.addProperty("userId", userId);
        jsonObject.addProperty("sdpAnswer", ipSdpAnswer);
        return jsonObject;
    }

    // Req-4 : 방에 입장해있는 사용자들, ICE candidate 정보 전송
    public static JsonObject iceCandidate(String userId, JsonElement candidate) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", "onIceCandidate"); // TODO : 수정해야할수도..?
        jsonObject.addProperty("userId", userId);
        jsonObject.add("candidate", candidate);
        return jsonObject;
    }

    // Res-5 : 참가자 접속 종료에 대한 정보 전송
    public static JsonObject participantLeft(String userId) {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", "participantLeft");
        jsonObject.addProperty("userId", userId);
        return jsonObject;
    }

    // Res-6 : 비디오 상태 변경에 대한 응답
    public static JsonObject videoStateAnswer(String userId, boolean video) {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", "videoStateAnswer");
        jsonObject.addProperty("userId", userId);
        jsonObject.addProperty("video", video);
        return jsonObject;
    }

    // Res-7 : 오디오 상태 변경에 대한 응답
    public static JsonObject audioStateAnswer(String userId, boolean audio) {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id","audioStateAnswer");
        jsonObject.addProperty("userId", userId);
        jsonObject.addProperty("audio", audio);
        return jsonObject;
    }

    // Res-8 : 타이머 상태 변경에 대한 응답
    public static JsonObject timerStateAnswer(String userId, boolean timer, String time) {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id","timerStateAnswer");
        jsonObject.addProperty("userId", userId);
        jsonObject.addProperty("timer", timer);
        if(timer){ // on 이면 현재시간도 추가
            jsonObject.addProperty("onTime", time);
        }else{
            jsonObject.addProperty("studyTime", time);
        }
        return jsonObject;
    }

    // Res-9 : 경고받은 유저에게 보낼 메시지
    //      id : alert , userId : donu , alertCount : 1
    //      해당 메시지 받으면 화면에 alertCount 번째 경고라고 출력시켜주면 될듯..?
    public static JsonObject userAlertMessage(String userId, int alertCount) {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id","ALERT");
        jsonObject.addProperty("userId", userId);
        jsonObject.addProperty("alertCount", alertCount);
        return jsonObject;
    }

    // Res-10 : 강제퇴장, 경고 누적으로 퇴장, 방장위임 시 유저에게 보낼 메시지
    public static JsonObject userTypeMessage(String type, String userId) {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id",type);
        jsonObject.addProperty("userId", userId);
        return jsonObject;
    }

    // Res-11 : 새벽 5시에 초기화 되었을 때, 현재 존재하는 유저들 한테 보낼 메시지
    public static JsonObject resetStudyTimeMessage(String userId) {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id","RESET");
        jsonObject.addProperty("userId", userId);
        return jsonObject;
    }


}
