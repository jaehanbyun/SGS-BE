package pnu.cse.studyhub.chat.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebSocketIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testChat() throws Exception {
        String webSocketEndpoint = "ws://localhost:" + port + "/chat/connect";
        String subscriptionTopic = "/topic/1";
        System.out.println(webSocketEndpoint);
        WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        StompSession stompSession = stompClient.connect(webSocketEndpoint, new WebSocketHttpHeaders(), new StompSessionHandlerAdapter() {}).get(1, TimeUnit.SECONDS);

        CountDownLatch latch = new CountDownLatch(1);

        stompSession.subscribe(subscriptionTopic, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return byte[].class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                System.out.println("Received: " + new String((byte[]) payload));
                latch.countDown();
            }
        });

        restTemplate.postForEntity("/chat/send", null, Void.class);

        if (!latch.await(10, TimeUnit.SECONDS)) {
            throw new TimeoutException("Failed to receive the message");
        }
    }
}
