package pnu.cse.studyhub.chat.config.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;
import pnu.cse.studyhub.chat.config.tcp.TCPMessageService;
import pnu.cse.studyhub.chat.dto.request.TCPSocketSessionRequest;
import pnu.cse.studyhub.chat.service.JwtTokenProvider;

@Slf4j
@RequiredArgsConstructor
@Component
public class MessageChannelInterceptor implements ChannelInterceptor {
    private final TCPMessageService tcpMessageService;
    private final JwtTokenProvider jwtTokenProvider;

    // 메세지가 전송되기 전에 Intercept
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
//        jwt 토큰 검증, gateway에서 검증 되었기 때문에 userId를 쓰기만 하면됨.
//        String authorizationHeader = String.valueOf(accessor.getFirstNativeHeader("Authorization"));
//        String accessToken = authorizationHeader.replace("Bearer ", "");
//        String userId = jwtTokenProvider.getUserInfo(accessToken);

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())){
            //jwt 토큰 필터링
            //토큰 데이터를 통해서 header에 userId를 넣어줌
            String destination = accessor.getDestination();
            log.warn("destination : " + destination);
        }

        return ChannelInterceptor.super.preSend(message, channel);
    }

    // 메세지가 전송된 후
    // CONNECT : 클라이언트가 서버에 연결되었을 때
    // DISCONNECT : 클라이언트가 서버와 연결을 끊었을 때
    // SUBSCRIBE : 채팅방에 들어갈 때
    // UNSUBSCRIBE : 채팅방에 나갈 때 혹은 소켓 연결 끊어질 때
    // command 상태를 통해서 접속 상태를 관리함.
    @Override
    public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        String authorizationHeader = String.valueOf(accessor.getFirstNativeHeader("Authorization"));
        String sessionId = accessor.getSessionId();
        String userId = "";
//        jwt 토큰 검증, gateway에서 검증 되었기 때문에 userId를 쓰기만 하면됨.

        log.debug(accessor.getCommand().toString());
        switch (accessor.getCommand()){
            case SUBSCRIBE: // room ID에 들어갈 때(소켓 연결이 아니라 채팅방에 들어갈 때 )
                userId = getUserId(authorizationHeader);
                TCPSocketSessionRequest subscribeRequest = TCPSocketSessionRequest.builder()
                        .type("SUBSCRIBE")
                        // "Timer ON TIMER OFF 프론트에서 보내는ㅅ거, USER_OUT, USER_IN 프론트에서 받는거
                        .userId(userId)
                        .server("chat")
                        .roomId(accessor.getDestination().substring(7)) // 슬래쉬 ( '/topic/' ) 삭제
                        .session(sessionId)
                        .build();
                log.debug(accessor.getCommand() + " : " + subscribeRequest.toString());
                tcpMessageService.sendMessage(subscribeRequest.toString());
                break;
            case DISCONNECT: // 채팅방 나갈 때
                TCPSocketSessionRequest disconnectRequest = TCPSocketSessionRequest.builder()
                        .type("DISCONNECT")
                        .userId(userId)
                        .server("chat")
                        .roomId(null) // 슬래쉬 ( '/topic/' ) 삭제
                        .session(sessionId)
                        .build();
                log.debug(accessor.getCommand() + " : " + disconnectRequest.toString());
                tcpMessageService.sendMessage(disconnectRequest.toString());
                break;
            case UNSUBSCRIBE:
                userId = getUserId(authorizationHeader);
                TCPSocketSessionRequest unsubscribeRequest = TCPSocketSessionRequest.builder()
                        .type("UNSUBSCRIBE")
                        .userId(userId)
                        .server("chat")
                        .roomId(null) // 슬래쉬 ( '/topic/' ) 삭제
                        .session(sessionId)
                        .build();
                log.debug(accessor.getCommand() + " : " + unsubscribeRequest.toString());
                tcpMessageService.sendMessage(unsubscribeRequest.toString());
                break;
        }
        ChannelInterceptor.super.postSend(message, channel, sent);
    }
    public String getUserId(String authorizationHeader){
        String accessToken = authorizationHeader.replace("Bearer ", "");
        return jwtTokenProvider.getUserInfo(accessToken);
    }
}
