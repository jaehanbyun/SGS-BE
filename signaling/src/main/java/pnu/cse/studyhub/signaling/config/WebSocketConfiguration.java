package pnu.cse.studyhub.signaling.config;


import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;
import pnu.cse.studyhub.signaling.service.MessageHandler;

import javax.servlet.ServletContext;
import javax.websocket.server.ServerContainer;


@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfiguration implements WebSocketConfigurer {

    private final MessageHandler messageHandler;


    // Spring Boot에서 기본적으로 내장 Tomcat을 사용하여 WebSocket연결을 지원하지만
    // WebSocket 서버 컨테이너 객체를 생성할 수 있다면, websocket 연결 구성이나 구현에 유연성 제공

//    private static final int MESSAGE_BUFFER_SIZE = 8192;
//
//    private final ServletContext servletContext;
//
//    @Bean
//    public ServletServerContainerFactoryBean createServletServerContainerFactoryBean() {
//        if (ignoreNullWsContainer) {
//            ServerContainer serverContainer =
//                    (ServerContainer) this.servletContext.getAttribute("javax.websocket.server.ServerContainer");
//            if (serverContainer == null) {
//                return null;
//            }
//        }
//
//        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
//        container.setMaxTextMessageBufferSize(MESSAGE_BUFFER_SIZE);
//        container.setMaxBinaryMessageBufferSize(MESSAGE_BUFFER_SIZE);
//        return container;
//    }



    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(messageHandler, "/socket")
                .setAllowedOrigins("*")
                .withSockJS();
    }
}
