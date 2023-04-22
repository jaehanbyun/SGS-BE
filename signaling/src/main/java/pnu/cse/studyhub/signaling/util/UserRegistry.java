package pnu.cse.studyhub.signaling.util;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UserRegistry {

    private final ConcurrentHashMap<String, UserSession> usersByUserId = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, UserSession> usersBySessionId = new ConcurrentHashMap<>();

    // userSession을 파라미터로 받은 후 해당 객체를
    public void register(UserSession user) {
        usersByUserId.put(user.getUserId(), user);
        usersBySessionId.put(user.getSession().getId(), user);
    }

    // userId로 user session 얻어오기
    public UserSession getByUserId(String userId) {
        return usersByUserId.get(userId);
    }

    // webSocketSession으로 userSession을 가져옴
    // 모든 유저는 하나의 WebSocketSession을 가지며, 각 WebSocketSession은 하나의 userSession을 가짐
    public UserSession getBySession(WebSocketSession session) {
        return usersBySessionId.get(session.getId());
    }

    public boolean exists(String userId) {
        return usersByUserId.keySet().contains(userId);
    }

    // webSocketSession을 받아서
    public UserSession removeBySession(WebSocketSession session) {
        final UserSession user = getBySession(session);
        // 웹소켓 연결만 하고 방에 입장하지 않고 종료하지 않는 경우 발생하는 예외 처리
        if (!Objects.isNull(user)) {
            usersByUserId.remove(user.getUserId());
            usersBySessionId.remove(session.getId());
            return user;
        }
        return null;
    }

}
