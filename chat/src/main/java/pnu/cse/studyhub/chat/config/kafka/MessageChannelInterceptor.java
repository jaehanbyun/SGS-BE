package pnu.cse.studyhub.chat.config.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;
import pnu.cse.studyhub.chat.config.tcp.TCPMessageService;
import pnu.cse.studyhub.chat.dto.request.TCPMessageRequest;
import pnu.cse.studyhub.chat.dto.request.TCPSocketSessionRequest;

@Slf4j
@RequiredArgsConstructor
@Component
public class MessageChannelInterceptor implements ChannelInterceptor {
    private final TCPMessageService tcpMessageService;

    // 메세지가 전송되기 전에 Intercept
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if (StompCommand.CONNECT.equals(accessor.getCommand())){
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
        String sessionId = accessor.getSessionId();
        String userId = accessor.getFirstNativeHeader("userId");
        switch (accessor.getCommand()){
            case SUBSCRIBE: // room ID에 들어갈 때(소켓 연결이 아니라 채팅방에 들어갈 때 )
                TCPSocketSessionRequest subscribeRequest = TCPSocketSessionRequest.builder()
                        .type("SUBSCRIBE")
                        // "Timer ON TIMER OFF 프론트에서 보내는ㅅ거, USEROUT USErIN 프론트에서 받는거
//                        .userId(userId)
                        .userId("test1")
                        .server("chat")
                        .roomId(accessor.getDestination().substring(7)) // 슬래쉬 ( '/topic/' ) 삭제
                        .session(sessionId)
                        .build();
                log.warn("test + " + subscribeRequest.toString());
                tcpMessageService.sendMessage(subscribeRequest.toString());
            case DISCONNECT: // 채팅방 나갈 때
                TCPSocketSessionRequest disconnectRequest = TCPSocketSessionRequest.builder()
                        .type("DISCONNECT")
//                        .userId(userId)
                        .server("chat")
                        .userId("test1")
                        .roomId(accessor.getDestination().substring(7)) // 슬래쉬 ( '/topic/' ) 삭제
                        .session(sessionId)
                        .build();
                log.info("UserId : " + userId);
                tcpMessageService.sendMessage(disconnectRequest.toString());

        }
        ChannelInterceptor.super.postSend(message, channel, sent);
    }
}
