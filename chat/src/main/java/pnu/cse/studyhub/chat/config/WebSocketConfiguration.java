package pnu.cse.studyhub.chat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@EnableWebSocketMessageBroker
@Configuration
public class WebSocketConfiguration implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // withSockJS - 소켓을 지원하지 않는 브라우저라면, sockJS사용하도록 설정
        registry
                .addEndpoint("/connect")
                        .setAllowedOrigins("*");
        registry.addEndpoint("/connect").setAllowedOrigins("*").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 메세지를 보낼 때, 관련 경로를 설정
        // 클라이언트가 메세지를 보낼 때, 경로 앞에 "/queue" 가 붙어 있으면 Broker로 보냄
        registry.setApplicationDestinationPrefixes("/kafka");
        // 메세지를 받을 때, 관련 경로를 설정
        //
        registry.enableSimpleBroker("/topic");
    }
}
