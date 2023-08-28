# SGS-BE 채팅 서버
### 해당 리드미는 서버에 대한 상세 정보를 담고 있습니다. 프로젝트에 관한 정보는 우측 링크에서 확인해주세요. [SGS-BE](https://github.com/jaehanbyun/SGS-BE)
###### 채팅 서버 담당 : [이제호](https://github.com/jhl8109)

### 개요
- 목적 : WebRTC 방에서 사용자 간 채팅 구현
- 설명 : 사용자는 WebRTC의 스터디방 접속 시 채팅이 가능하다. 채팅 시 해당 방에 접속해있는 인원들에게 전송되며, 파일, 이미지 등도 가능하다.

- 서버 : Spring Boot
- 데이터베이스 : MongoDB
- 메시지 큐(브로커) : Kafka
- 외부 스토리지 : AWS S3

## 주요 기능
- 채팅 조회 : 채팅 조회 시 mongoDB로부터 채팅 내역을 읽어온다. pagination을 통해 요청받은 page, size 만큼 불러온다.
- 채팅 전송 : 채팅 전송 시 메시지는 메시지 큐에 들어간다. 메시지 큐는 해당 방의 접속자(구독한 사람)들에게 메시지를 전송한다(consume). 또한, 메시지는 mongoDB에 저장된다.

## 상세 설명

### 채팅
Kafka, pub/sub 구조를 활용하여 그룹 채팅을 구현한다.<br>
#### Kafka 설정
```java
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
        // 클라이언트가 메세지를 보낼 때, 경로 앞에 "/queue"이 붙어있으면 Broker로 보냄
        registry.setApplicationDestinationPrefixes("/queue");
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
```
<br>

#### Kafka Producer & Consumer
```java
@Service
@RequiredArgsConstructor // same as autowired
@Slf4j
public class KafkaConsumer {
    private final static String GROUPID = "${spring.kafka.consumer.group-id}";

    private final static String TOPICS = "${spring.kafka.topic}";

    private final SimpMessagingTemplate template;

    @KafkaListener(topics = TOPICS, groupId = GROUPID)
    public void consume(String stringChat) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        Chat chat = objectMapper.readValue(stringChat, Chat.class);
        log.info("Consumed Message : " + stringChat);
        template.convertAndSend("/topic/"+chat.getRoomId(), stringChat);
    }
}
@Service
@RequiredArgsConstructor // same as autowired
@Slf4j
public class KafkaProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void send(String topic, Chat chat) {
        log.info("producer_topic : " + topic);
        log.info("producer_content : " + chat.getContent());
        String type = chat.getMessageType();
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        try {
            String content = objectMapper.writeValueAsString(chat);
            kafkaTemplate.send(topic, content);
        } catch (JsonProcessingException e) { //json 파싱 실패
            e.printStackTrace();
        } catch (IOException e) { // 파일 변환 실패
            throw new RuntimeException(e);
        }
    }
}
```
---

### TCP 통신
서버 간 통신으로 TCP 통신을 활용한다. 이를 통해 실시간성을 높이고, 네트워크 부하를 일부 해소한다. <br>
채팅 서버는 상태관리 서버와 TCP 통신을 하며, 이를 통해 세션을 통해 접속 상태 및 접속 위치를 관리한다. <br>
접속 상태 ON은 소켓 구독 , 접속 상태 OFF는 소켓 구독 해제로 확인한다.<br>
#### TCP 서버 설정
```java
@Configuration
@EnableScheduling
public class TCPClientConfig implements ApplicationEventPublisherAware {
    @Value("${tcp.server.host}")
    private String host;

    @Value("${tcp.server.port}")
    private int port;

    @Value("${tcp.server.connection.poolSize}")
    private int connectionPoolSize;

    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Bean
    public AbstractClientConnectionFactory clientConnectionFactory() {
        TcpNioClientConnectionFactory tcpNioClientConnectionFactory = new TcpNioClientConnectionFactory(host, port);
        tcpNioClientConnectionFactory.setUsingDirectBuffers(true);
        tcpNioClientConnectionFactory.setApplicationEventPublisher(applicationEventPublisher);
        return new CachingClientConnectionFactory(tcpNioClientConnectionFactory, connectionPoolSize);
    }


    @Bean
    public MessageChannel outboundChannel() {
        return new DirectChannel();
    }

    @Bean
    @ServiceActivator(inputChannel = "outboundChannel")
    public MessageHandler outboundGateway(AbstractClientConnectionFactory clientConnectionFactory) {
        TcpOutboundGateway tcpOutboundGateway = new TcpOutboundGateway();
        tcpOutboundGateway.setConnectionFactory(clientConnectionFactory);
        return tcpOutboundGateway;
    }
}
```
<br>

#### TCP 전송
```java
@Slf4j
@RequiredArgsConstructor
@Component
public class MessageChannelInterceptor implements ChannelInterceptor {
    private final TCPMessageService tcpMessageService;
    private final JwtTokenProvider jwtTokenProvider;

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
    @Override
    public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, Exception ex) {
        ChannelInterceptor.super.afterSendCompletion(message, channel, sent, ex);
    }

    @Override
    public void afterReceiveCompletion(Message<?> message, MessageChannel channel, Exception ex) {
        ChannelInterceptor.super.afterReceiveCompletion(message, channel, ex);
    }

}
```
---

### 예외 처리
socket, kafka, http 등 다양한 부분에서 예외가 발생할 수 있다. 이를 ErrorCode 형태로 정의한다. <br>
HTTP의 경우 response를 해당 에러 코드에 적합하게 보낸다.<br>
Socket의 경우도 동일하게 response를 사용자에게 보낸다.<br>
Kafka의 경우 예외 발생 시 ELK와 같은 로그 시스템 연결 시, 로그로 남길 예졍이다.<br>
#### enum 예외코드 정리
```java
@Getter
public enum ErrorCode {
    //SERVER
    USER_NOT_FOUND(HttpStatus.NOT_FOUND.toString(),"CHAT-001", "해당 유저가 존재하지 않음"),
    MESSAGE_NOT_DELIVERED(HttpStatus.INTERNAL_SERVER_ERROR.toString(), "CHAT-002", "메시지 전송 실패"),
    INVALID_MESSAGE_FORMAT(HttpStatus.BAD_REQUEST.toString(), "CHAT-003", "메시지 형식이 잘못됨"),
    //SOCKET
    KAFKA_CONNECTION_FAILED("INTERNAL_SOCKET_ERROR", "CHAT-011", "소켓-카프카 연결 실패"),
    KAFKA_INTERRUPTTED("INTERNAL_SOCKET_ERROR", "CHAT-012", "카프카 인터럽트 발생"),
    KAFKA_SERIALIZE_FAILED("INTERNAL_SOCKET_ERROR", "CHAT-013", "카프카 직렬화 실패"),
    KAFKA_TIMEOUT("INTERNAL_SOCKET_ERROR", "CHAT-014", "카프카 연결 시간 초과"),
    KAFKA_UNKNOWN_ERROR("INTERNAL_SOCKET_ERROR", "CHAT-015", "알 수 없는 카프카 오류"),
    //TCP
    TCP_CONNECTION_FAILED("TCP-ERROR", "CHAT-021", "TCP 연결 실패"),
    TCP_BIND_FAILED("TCP-ERROR", "CHAT-022", "TCP 바인딩 실패"),
    TCP_TIMEOUT("TCP-ERROR", "CHAT-023", "TCP 연결 시간 초과"),
    //ETC
    UNKNOWN_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.toString(), "CHAT-031", "알 수 없는 서버 오류"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED.toString(), "CHAT-032", "토큰이 유효하지 않은 경우"),
    FILE_CONVERSION_ERROR(HttpStatus.BAD_REQUEST.toString(), "CHAT-033", "파일 변환 실패"),;

    private final String status;

    private final String code;
    private final String message;

    ErrorCode(String status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}

```
<br>

#### HTTP 예외 처리
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(InvalidMessageFormatException.class)
    public ResponseEntity<FailedResponse> handleInvalidMessageFormatException(Exception e) {
        FailedResponse failedResponse = new FailedResponse<>(
                "Failed",
                ErrorCode.INVALID_MESSAGE_FORMAT.getMessage(),
                ErrorCode.INVALID_MESSAGE_FORMAT.getStatus(),
                ErrorCode.INVALID_MESSAGE_FORMAT.getCode(),
                e.getMessage());
        return ResponseEntity.status(HttpStatus.valueOf(ErrorCode.INVALID_MESSAGE_FORMAT.getStatus())).body(failedResponse);
    }
    @ExceptionHandler(FileConversionException.class)
    public ResponseEntity<FailedResponse> handleFileConversionException(Exception e) {
        FailedResponse failedResponse = new FailedResponse<>(
                "Failed",
                ErrorCode.FILE_CONVERSION_ERROR.getMessage(),
                ErrorCode.FILE_CONVERSION_ERROR.getStatus(),
                ErrorCode.FILE_CONVERSION_ERROR.getCode(),
                e.getMessage());
        return ResponseEntity.status(HttpStatus.valueOf(ErrorCode.FILE_CONVERSION_ERROR.getStatus())).body(failedResponse);
    }
    @ExceptionHandler(KafkaConnectionException.class)
    public ResponseEntity<FailedResponse> handleKafkaConnectionException(Exception e) {
        FailedResponse failedResponse = new FailedResponse<>(
                "Failed",
                ErrorCode.KAFKA_CONNECTION_FAILED.getMessage(),
                ErrorCode.KAFKA_CONNECTION_FAILED.getStatus(),
                ErrorCode.KAFKA_CONNECTION_FAILED.getCode(),
                e.getMessage());
        return ResponseEntity.status(HttpStatus.valueOf(ErrorCode.KAFKA_UNKNOWN_ERROR.getStatus())).body(failedResponse);
    }
    @ExceptionHandler(KafkaSerializationException.class)
    public ResponseEntity<FailedResponse> handleKafkaSerializationException(Exception e) {
        FailedResponse failedResponse = new FailedResponse<>(
                "Failed",
                ErrorCode.KAFKA_SERIALIZE_FAILED.getMessage(),
                ErrorCode.KAFKA_SERIALIZE_FAILED.getStatus(),
                ErrorCode.KAFKA_SERIALIZE_FAILED.getCode(),
                e.getMessage());
        return ResponseEntity.status(HttpStatus.valueOf(ErrorCode.KAFKA_SERIALIZE_FAILED.getStatus())).body(failedResponse);
    }
    @ExceptionHandler(KafkaInterruptException.class)
    public ResponseEntity<FailedResponse> handleKafkaInterruptedException(Exception e) {
        FailedResponse failedResponse = new FailedResponse<>(
                "Failed",
                ErrorCode.KAFKA_INTERRUPTTED.getMessage(),
                ErrorCode.KAFKA_INTERRUPTTED.getStatus(),
                ErrorCode.KAFKA_INTERRUPTTED.getCode(),
                e.getMessage());
        return ResponseEntity.status(HttpStatus.valueOf(ErrorCode.KAFKA_INTERRUPTTED.getStatus())).body(failedResponse);
    }

}
```
<br>

####Socket 예외 처리
```java
@Controller
@RequiredArgsConstructor
@Slf4j
public class SocketChatController {

    @Value("${spring.kafka.topic}")
    private String TOPIC;
    private final KafkaProducer kafkaProducer;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @MessageMapping("/send")
    @SendTo("/topic/group")
    public Chat broadcastMessage(@Payload Chat chat, Principal principal) {
        try {
            kafkaProducer.send(TOPIC, chat);
        } catch (TimeoutException e) {
            throw new KafkaTimeOutException(e.getMessage());
        } catch (SerializationException e) {
            throw new KafkaSerializationException(e.getMessage());
        } catch (InterruptException e) {
            throw new KafkaInterruptException(e.getMessage());
        } catch (KafkaException e) {
            throw new KafkaAbnormalException(e.getMessage());
        }
        return chat;
    }
    // MessageMapping에 연결된 메소드에서 발생하는 예외를 처리하는 핸들러
    // @MessageMapping 또는 @SubscribeEvent로 주석이 달린 메서드에서 WebSocket 메시지를 처리하는 동안 발생하는 예외를 처리
    @MessageExceptionHandler({KafkaTimeOutException.class, KafkaSerializationException.class, KafkaInterruptException.class, KafkaAbnormalException.class})
    @SendToUser("/queue/errors")
    public FailedResponse handleException(Throwable exception, StompHeaderAccessor stompHeaderAccessor) {
        log.debug("ExceptionHandler : {}", exception.getMessage());
        FailedResponse failedResponse;
        if (exception instanceof TimeoutException)
            failedResponse = createFailedResponse(exception.getMessage(), ErrorCode.KAFKA_TIMEOUT);
        else if (exception instanceof SerializationException)
            failedResponse = createFailedResponse(exception.getMessage(), ErrorCode.KAFKA_SERIALIZE_FAILED);
        else if (exception instanceof  InterruptException)
            failedResponse = createFailedResponse(exception.getMessage(), ErrorCode.KAFKA_INTERRUPTTED);
        else if (exception instanceof KafkaException)
            failedResponse = createFailedResponse(exception.getMessage(), ErrorCode.KAFKA_UNKNOWN_ERROR);
        else
            failedResponse = createFailedResponse(exception.getMessage(), ErrorCode.UNKNOWN_SERVER_ERROR);
        return failedResponse;
    }
    // MessageMapping에 연결되지 않은 메소드에서 발생하는 예외를 처리하는 핸들러
    // 전반적으로 발생하는 예외 처리
//    @ExceptionHandler({KafkaTimeOutException.class, KafkaSerializationException.class, KafkaInterruptException.class, KafkaAbnormalException.class})
//    @SendToUser("/queue/errors")
//    public FailedResponse handleKafkaException(Exception exception, Principal principal) {
//        log.debug("ExceptionHandler : {}", exception.getMessage());
//        FailedResponse failedResponse;
//        if (exception instanceof TimeoutException)
//            failedResponse = createFailedResponse(exception.getMessage(), ErrorCode.KAFKA_TIMEOUT);
//        else if (exception instanceof SerializationException)
//            failedResponse = createFailedResponse(exception.getMessage(), ErrorCode.KAFKA_SERIALIZE_FAILED);
//        else if (exception instanceof  InterruptException)
//            failedResponse = createFailedResponse(exception.getMessage(), ErrorCode.KAFKA_INTERRUPTTED);
//        else if (exception instanceof KafkaException)
//            failedResponse = createFailedResponse(exception.getMessage(), ErrorCode.KAFKA_UNKNOWN_ERROR);
//        else
//            failedResponse = createFailedResponse(exception.getMessage(), ErrorCode.UNKNOWN_SERVER_ERROR);
//        return failedResponse;
//    }
    private FailedResponse createFailedResponse(String message, ErrorCode errorCode) {
        return new FailedResponse<>(
                "Failed",
                errorCode.getMessage(),
                errorCode.getStatus(),
                errorCode.getCode(),
                message);
    }
}
```
---
## 향 후 계획

향 후  채팅서버 <-> 상태관리서버에 grpc를 구현할 예정이다. 

그 이유는 아래의 기대요소와 같다.
- 성능 개선 기대
- 예외 처리의 직관성 개선 기대
- 네트워크 사용량 감소 기대

현재 개발 진척도는 아래 내용까지 완성되었다.
- 채팅서버 grpc client 개발
- 상태관리서버 grpc server 개발
- 통신을 위한 protocol buffer 파일 개발
  
추 후 이를 통해 `1. TCP VS gRPC 송수신 성능 평가` , `2. 예외처리 코드 작성 및 직관성 개선 확인(지극히 개인적, 정성적.. 으로 평가할 예정)`, `3. Prometheus & Grafana를 통한 네트워크 부하(usage) 비교` 를 할 예졍이다.

