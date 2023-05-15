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
        }
        return ChannelInterceptor.super.preSend(message, channel);
    }

    // 메세지가 전송된 후
    // CONNECT : 클라이언트가 서버에 연결되었을 때
    // DISCONNECT : 클라이언트가 서버와 연결을 끊었을 때
    // 두 command 상태를 통해서 접속 상태를 관리함.
    @Override
    public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        String sessionId = accessor.getSessionId();
        String userId = accessor.getFirstNativeHeader("userId");
        log.info(accessor.getCommand().toString());
        switch (accessor.getCommand()){
            case CONNECT : // 소켓 연결 되었으므로 접속 상태 ON
                String connectRequest = TCPSocketSessionRequest.builder()
                        .type("CONNECT")
                        .userId(userId)
                        .session(sessionId)
                        .build().toString();
                log.info("UserId : " + userId);
                tcpMessageService.sendMessage(connectRequest);
            case DISCONNECT: // 소켓 연결 해제 되었으므로 접속 상태 OFF
                String disconnectRequest = TCPSocketSessionRequest.builder()
                        .type("DISCONNECT")
                        .userId(userId)
                        .session(sessionId)
                        .build().toString();
                log.info("UserId : " + userId);
                tcpMessageService.sendMessage(disconnectRequest);

        }
        ChannelInterceptor.super.postSend(message, channel, sent);
    }
}
