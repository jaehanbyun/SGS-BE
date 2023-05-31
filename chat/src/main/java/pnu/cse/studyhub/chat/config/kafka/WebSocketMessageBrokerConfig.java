package pnu.cse.studyhub.chat.config.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@EnableWebSocketMessageBroker
@RequiredArgsConstructor
@Configuration
public class WebSocketMessageBrokerConfig implements WebSocketMessageBrokerConfigurer {
    private final MessageChannelInterceptor messageChannelInterceptor;
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // withSockJS - 소켓을 지원하지 않는 브라우저라면, sockJS사용하도록 설정, test 도메인 : https://jxy.me
        registry.addEndpoint("/chat/connect").setAllowedOriginPatterns("*").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 메세지를 보낼 때, 관련 경로를 설정
        // 클라이언트가 메세지를 보낼 때, 경로 앞에 "/topic"이 붙어있으면 Broker로 보냄
        registry.setApplicationDestinationPrefixes("/pub");
        // 메세지를 받을 때, 관련 경로를 설정
//        registry.enableSimpleBroker("/sub");
        registry.enableSimpleBroker("/topic");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(messageChannelInterceptor);
        WebSocketMessageBrokerConfigurer.super.configureClientInboundChannel(registration);
    }
}
