package pnu.cse.studyhub.chat.config.kafka;

import io.grpc.ManagedChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.errors.InterruptException;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.errors.TimeoutException;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;
import pnu.cse.studyhub.chat.config.tcp.TCPMessageService;
import pnu.cse.studyhub.chat.dto.request.TCPSocketSessionRequest;
import pnu.cse.studyhub.chat.dto.response.FailedResponse;
import pnu.cse.studyhub.chat.exception.ErrorCode;
import pnu.cse.studyhub.chat.exception.kafka.*;
import pnu.cse.studyhub.chat.service.GrpcClientService;
import pnu.cse.studyhub.chat.service.JwtTokenProvider;

import java.net.ConnectException;

@Slf4j
@RequiredArgsConstructor
@Component
public class MessageChannelInterceptor implements ChannelInterceptor {
    private final TCPMessageService tcpMessageService;
    private final JwtTokenProvider jwtTokenProvider;
    private final GrpcClientService grpcClientService;

    // 메세지가 전송되기 전에 Intercept
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
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
        // jwt 토큰 검증, gateway에서 검증 되었기 때문에 userId를 쓰기만 하면됨.

        // 접속 상태 관리
        switch (accessor.getCommand()) {
            case SUBSCRIBE: // room ID에 들어갈 때(소켓 연결이 아니라 채팅방에 들어갈 때 )
                userId = getUserId(authorizationHeader);
                Long roomId = Long.valueOf(accessor.getDestination().substring(7)); // 슬래쉬 ( '/topic/' ) 삭제
                tcpMessageService.subscribeRoom(roomId, userId, sessionId);
                grpcClientService.subscribeRoom(userId,roomId,sessionId);
                break;
            case UNSUBSCRIBE: // 채팅방 나갈 때 (UNSUBSCRIBE || DISCONNECT)
            case DISCONNECT:
                tcpMessageService.unsubscribeRoom(userId,sessionId);
                grpcClientService.unsubscribeRoom(sessionId);
                break;
        }
        ChannelInterceptor.super.postSend(message, channel, sent);
    }

    public String getUserId(String authorizationHeader){
        String accessToken = authorizationHeader.replace("Bearer ", "");
        return jwtTokenProvider.getUserInfo(accessToken);
    }
    @Override
    public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, Exception ex) {
        ChannelInterceptor.super.afterSendCompletion(message, channel, sent, ex);
    }

    @Override
    public void afterReceiveCompletion(Message<?> message, MessageChannel channel, Exception ex) {
        ChannelInterceptor.super.afterReceiveCompletion(message, channel, ex);
    }
}
