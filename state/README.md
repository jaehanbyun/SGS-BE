# SGS-BE 상태관리 서버
### 해당 리드미는 서버에 대한 상세 정보를 담고 있습니다. 프로젝트에 관한 정보는 우측 링크에서 확인해주세요. [SGS-BE](https://github.com/jaehanbyun/SGS-BE)
###### 상태관리 서버 담당 : [이제호](https://github.com/jhl8109)

### 개요
- 목적 : 사용자 접속 상태 및 학습 시간 관리
- 설명 : 사용자는 WebRTC의 스터디방 접속 정보는 상태관리 서버에 의해 관리된다. 관리되는 접속 정보에는 접속 중인 방 위치, 세션ID, 금일 학습 시간이 있다. 상태관리 서버는 사용자와 직접 상호작용하지 않고, 인증서버, 채팅서버, 상태관리서버, 시그널링 서버, 방관리 서버와 상호작용한다.

- 서버 : Spring Boot
- 캐시 : Redis

## 주요 기능
- 접속 상태 관리 : STOMP 구독 시, SUBSCRIBE / UNSUBSCRIBE를 기준으로 접속 여부를 판단하고, 해당 정보는 채팅서버로 부터 소켓 구독 정보를 전달받아 관리한다.
- 학습 시간 관리 : 사용자는 WebRTC 관련 서버 중 시그널링 서버를 통해 실시간 학습 시간 정보를 타이머로 저장하며, 타이머 ON/OFF 또는 접속 ON/OFF 시 학습 시간 정보가 갱신된다.

## 상세 설명

### TCP 통신
서버 간 통신으로 TCP 통신을 활용한다. 이를 통해 실시간성을 높이고, 네트워크 부하를 일부 해소한다. <br>
상태관리 서버는 아키텍처 내 타 마이크로서비스들과 TCP 통신을 하며, 이를 통해 세션을 통해 접속 상태 및 접속 위치를 관리한다. <br>
#### TCP 서버 설정
```java
@Configuration
public class TCPServerConfig {

    @Value("${tcp.server.port}")
    private int port;

    @Bean
    public AbstractServerConnectionFactory serverConnectionFactory() {
        TcpNioServerConnectionFactory tcpNioServerConnectionFactory = new TcpNioServerConnectionFactory(port);
        tcpNioServerConnectionFactory.setUsingDirectBuffers(true);
        return tcpNioServerConnectionFactory;
    }

    @Bean
    public MessageChannel inboundChannel() {
        return new DirectChannel();
    }

    @Bean
    public TcpInboundGateway inboundGateway(AbstractServerConnectionFactory serverConnectionFactory) {
        TcpInboundGateway tcpInboundGateway = new TcpInboundGateway();
        tcpInboundGateway.setConnectionFactory(serverConnectionFactory);
        tcpInboundGateway.setRequestChannel(inboundChannel());
        return tcpInboundGateway;
    }
}
```
<br>

#### TCP 메시지 수신 처리
```java
@Service
@Slf4j
@RequiredArgsConstructor
public class MessageService {

    private final RedisService redisService;
    private final TCPRoomClientGateway tcpRoomClientGateway;
    private final TCPSignalingClientGateway tcpSignalingClientGateway;
    private final TCPAuthClientGateway tcpAuthClientGateway;
    private final JsonConverter jsonConverter;

    public String processMessage(String message) {
        log.info("Received message: {}", message);
        ObjectMapper mapper = new ObjectMapper();
        try{
            TCPMessageReceiveRequest response = jsonConverter.convertFromJson(message, TCPMessageReceiveRequest.class);
            String responseMessage = "";
            switch (response.getServer()) {
                case "chat":
                    TCPChatReceiveRequest chatRequest = (TCPChatReceiveRequest) response;
                    if (chatRequest.getType().matches("SUBSCRIBE")) {
                        RealTimeData realTimeData =  redisService.findRealTimeData(chatRequest.getUserId());
                        if (realTimeData != null) { // 오늘 접속 이력이 있는 경우
                            realTimeData.setRoomId(chatRequest.getRoomId());
                            realTimeData.setSessionId(chatRequest.getSession());
                            RealTimeData chatSubscribeRealTimeData = redisService.saveRealTimeDataAndSession(realTimeData);

                            responseMessage = jsonConverter.convertToJson(chatSubscribeRealTimeData);
                        } else { // 오늘 접속 이력이 없는 경우
                            realTimeData = makeRealTimeData(chatRequest);
                            RealTimeData chatSubscribeRealTimeData = redisService.saveRealTimeDataAndSession(realTimeData);

                            responseMessage = jsonConverter.convertToJson(chatSubscribeRealTimeData);
                        }
                    } else if (chatRequest.getType().matches("DISCONNECT|UNSUBSCRIBE")) {
                        String userId = redisService.findUserIdBySessionId(chatRequest.getSession());

                        if (userId  != null) {
                            // RealTimeData, 즉 공부 시간 기록은 남기고, session 정보만 삭제
                            redisService.deleteSession(userId);
                            // redisService.deleteRealTimeDataAndSession(userId, chatRequest.getSession());
                            responseMessage = mapper.writeValueAsString(redisService.findRealTimeData(userId));
                        } else {
                            // 존재하지 않는 접속 이력에 대한 삭제 동작 , 예외처리
                        }

                    } else {
                        // 구독, 구독해제, 연결해제를 제외한 나머지 소켓 동작
                    };
                    break;
                case "signaling":
                    TCPSignalingReceiveRequest signalingRequest = new TCPSignalingReceiveRequest();
                    if (response.getType().startsWith("STUDY_TIME") && response instanceof TCPSignalingReceiveRequest) {
                        signalingRequest = (TCPSignalingReceiveRequest) response;

                        // Signaling <- state, StudyTime 조회, 방 들어옴.
                        if (signalingRequest.getType().matches("STUDY_TIME_FROM_TCP")) {
                            RealTimeData realTimeData =  redisService.findRealTimeData(signalingRequest.getUserId());
                            if (realTimeData != null) { // 공부 이력이 있는 경우
                                responseMessage = sendSignalingServerStudyTimeMessage(realTimeData);
                            } else { // 공부 이력이 없는 경우
                                realTimeData = makeRealTimeData(signalingRequest);

                                responseMessage = sendSignalingServerStudyTimeMessage(realTimeData);
                                log.debug("STUDY_TIME_FROM_TCP: {}", responseMessage);
                            }
                            // Signaling -> state,  StudyTime 저장, 방 나감.
                        } else if (signalingRequest.getType().matches("STUDY_TIME_TO_TCP")) {
                            RealTimeData realTimeData = redisService.findRealTimeData(signalingRequest.getUserId());
                            String roomOutResult = "";
                            if (realTimeData != null) { // 공부 이력이 있는 경우
                                // 공부 시간 저장
                                realTimeData.setStudyTime(signalingRequest.getStudyTime());
                                RealTimeData signalingSetRealTimeData = redisService.saveRealTimeData(realTimeData);
                                responseMessage = sendSignalingServerStudyTimeMessage(signalingSetRealTimeData);
                                // Room Out 알림
                                roomOutResult = sendRoomServerRoomOutMessage(realTimeData);
                            } else {
                                // 공부 이력이 없는 경우
                                realTimeData = makeRealTimeData(signalingRequest);
                                responseMessage = sendSignalingServerStudyTimeMessage(realTimeData);
                                roomOutResult = sendRoomServerRoomOutMessage(realTimeData);
                            }
                            tcpRoomClientGateway.send(roomOutResult);
                        } else {
                            // ClassCastException
                        }
                    } else {
                        // 에러처리
                    }
                    break;
                case "signaling_scheduling":
                    TCPSignalingReceiveSchedulingRequest signalingSchedulingRequest = new TCPSignalingReceiveSchedulingRequest();
                    if (response.getType().startsWith("SCHEDULER") && response instanceof TCPSignalingReceiveSchedulingRequest) {
                        signalingSchedulingRequest = (TCPSignalingReceiveSchedulingRequest) response;
                        // 새벽 05:00에 시그널링 서버로 부터 데이터 동기화를 위해 일괄적으로 데이터를 받음.
                        if (signalingSchedulingRequest.getType().matches("SCHEDULER")) {
                            List<UserDto> userList = signalingSchedulingRequest.getUsers();
                            for (UserDto userDto : userList) {
                                RealTimeData realTimeData = redisService.findRealTimeData(userDto.getUserId());
                                // 유저가 존재하면, redis에 유저의 시간 정보를 갱신함
                                if (realTimeData != null) {
                                    realTimeData.setStudyTime(userDto.getStudyTime());
                                    redisService.saveRealTimeData(realTimeData);
                                    // 유저가 존재하지 않으면 redis에 상태 정보를 생성하여 저장
                                } else {
                                    realTimeData = makeRealTimeData(userDto);
                                    redisService.saveRealTimeData(realTimeData);
                                }
                            }
                            responseMessage = signalingSchedulingRequest.toString();
                        } else if (signalingSchedulingRequest.getType().matches("SCHEDULER_LAST")) {
                            List<UserDto> userList = signalingSchedulingRequest.getUsers();
                            for (UserDto userDto : userList) {
                                RealTimeData realTimeData = redisService.findRealTimeData(userDto.getUserId());
                                // 유저가 존재하면, redis에 유저의 시간 정보를 갱신함
                                if (realTimeData != null) {
                                    realTimeData.setStudyTime(userDto.getStudyTime());
                                    redisService.saveRealTimeData(realTimeData);
                                // 유저가 존재하지 않으면 redis에 상태 정보를 생성하여 저장
                                } else {
                                    realTimeData = makeRealTimeData(userDto);
                                    redisService.saveRealTimeData(realTimeData);
                                }
                            }
                            List<RealTimeData> allData = redisService.getAllRealTimeData();
                            processAndSendBatch(allData);
                            responseMessage = signalingSchedulingRequest.toString();
                        } else {
                            // 에러처리
                        }
                    }
                        break;
                case "auth":
                    TCPAuthReceiveRequest authRequest = (TCPAuthReceiveRequest) response;
                    log.warn(authRequest.toString());
                    // 유저 서버에 공부 시간 전달
                    if (authRequest.getType().matches("STUDY_TIME_FROM_TCP")) {
                        try {
                            RealTimeData realTimeData =  redisService.findRealTimeData(authRequest.getUserId());
                            if (realTimeData != null) {
                                responseMessage = sendAuthServerStudyTimeMessage(realTimeData);
                            } else {
                                realTimeData = new RealTimeData();
                                realTimeData.setUserId(authRequest.getUserId());
                                responseMessage = sendAuthServerStudyTimeMessage(realTimeData);
                            }

                        }catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                    break;
                case "room":
                    TCPRoomReceiveRequest roomRequest = (TCPRoomReceiveRequest) response;
                    log.debug(roomRequest.toString());
                    if (roomRequest.getType().matches("ALERT")) {
                        String signalingServerRoomRequestMessage = sendSignalingServerAlertMessage(roomRequest);
                        String resp = tcpSignalingClientGateway.send(signalingServerRoomRequestMessage);
                        log.debug("resq : " + signalingServerRoomRequestMessage);
                        log.debug("resp : " + resp);
                    }
                    if (roomRequest.getType().matches("KICK_OUT|KICK_OUT_BY_ALERT|DELEGATE")) {
                        // 내부적으로 server : "room" 요청 온 것이 server : "state" 로 바뀜
                        String signalingServerRoomRequestMessage = sendSignalingServerRoomMessage(roomRequest);
                        String resp = tcpSignalingClientGateway.send(signalingServerRoomRequestMessage);
                        log.debug("resq : " + signalingServerRoomRequestMessage);
                        log.debug("resp : " + resp);
                    } else {
                        // 잘못된 type 입력 에러 처리
                    }
                    break;
            }
            log.debug("responseMessage : " + responseMessage);
            return responseMessage;
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
    public String sendRoomServerRoomOutMessage(RealTimeData rtData) {
        TCPRoomSendRequest tcpRoomSendRequest = TCPRoomSendRequest.builder()
                .server("state")
                .userId(rtData.getUserId())
                .roomId(rtData.getRoomId())
                .build();

        String tcpRoomOutResponseMessage = jsonConverter.convertToJson(tcpRoomSendRequest);
        return tcpRoomOutResponseMessage;
    }
    public String sendSignalingServerStudyTimeMessage(RealTimeData rtData) {
        TCPSignalingReceiveResponse tcpSignalingReceiveResponse = TCPSignalingReceiveResponse.builder()
                .userId(rtData.getUserId())
                .studyTime(rtData.getStudyTime())
                .build();

        String tcpSignalingResponseMessage = jsonConverter.convertToJson(tcpSignalingReceiveResponse);
        return tcpSignalingResponseMessage;
    }
    public String sendAuthServerStudyTimeMessage(RealTimeData realTimeData){
        TCPAuthReceiveResponse tcpAuthReceiveResponse = TCPAuthReceiveResponse.builder()
                .userId(realTimeData.getUserId())
                .studyTime(realTimeData.getStudyTime())
                .build();

        String tcpAuthResponseMessage = jsonConverter.convertToJson(tcpAuthReceiveResponse);
        return tcpAuthResponseMessage;
    }
    public String sendSignalingServerRoomMessage(TCPRoomReceiveRequest tcpRoomReceiveRequest){
        TCPSignalingSendRequest tcpSignalingSendRequest = tcpRoomReceiveRequest.toTCPSignalingSendRequest(tcpRoomReceiveRequest.getType());
        String tcpSignalingSendRequestMessage = jsonConverter.convertToJson(tcpSignalingSendRequest);
        return tcpSignalingSendRequestMessage;
    }
    public String sendSignalingServerAlertMessage(TCPRoomReceiveRequest tcpRoomReceiveRequest) {
        TCPSignalingSendAlertRequest tcpSignalingSendRequest = tcpRoomReceiveRequest.toTCPSignalingSendAlertRequest(tcpRoomReceiveRequest.getType());
        String tcpSignalingSendRequestMessage = jsonConverter.convertToJson(tcpSignalingSendRequest);
        return tcpSignalingSendRequestMessage;
    }
    public RealTimeData makeRealTimeData(TCPChatReceiveRequest chatRequest) {
        RealTimeData realTimeData = new RealTimeData();
        realTimeData.setUserId(chatRequest.getUserId());
        realTimeData.setRoomId(chatRequest.getRoomId());
        realTimeData.setSessionId(chatRequest.getSession());
        return realTimeData;
    }
    public RealTimeData makeRealTimeData(TCPSignalingReceiveRequest signalingRequest) {
        RealTimeData realTimeData = new RealTimeData();
        realTimeData.setUserId(signalingRequest.getUserId());
        realTimeData.setStudyTime(signalingRequest.getStudyTime());
        return realTimeData;
    }
    public RealTimeData makeRealTimeData(UserDto userDto) {
        RealTimeData realTimeData = new RealTimeData();
        realTimeData.setUserId(userDto.getUserId());
        realTimeData.setStudyTime(userDto.getStudyTime());

        return realTimeData;
    }
    public void processAndSendBatch(List<RealTimeData> batch) {
        ObjectMapper objectMapper = new ObjectMapper();

        // 모든 RealTimeData 객체를 UserDto 객체로 변환
        List<UserDto> userDtoBatch = batch.stream()
                .map(realTimeData -> new UserDto(realTimeData.getUserId(), realTimeData.getStudyTime()))
                .collect(Collectors.toList());
        Map<String, Object> data = new HashMap<>();
        data.put("server", "state");
        data.put("type", "SCHEDULER");
        data.put("users", userDtoBatch);

        // JSON 형태로 변환 후 TCP로 전송
        try {
            String dataAsString = objectMapper.writeValueAsString(data);

            log.debug(dataAsString);

            String response = tcpAuthClientGateway.send(dataAsString);
            redisService.deleteAllData();
//            if (response.contains("SUCCESS")) {
//                redisService.deleteAllData();
//            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
```
---

### Redis 
Redis를 통해 정보를 1일치 접속자에 관한 상태 관리 정보를 임시 저장/조회한다. <br>
해당 정보에는 UserID, RoomId, SessionId, studyTime이 포함된다. <br>
redis는 key-value 저장소이기 때문에 userId-realTimeData를 key-value로 저장하면 sessionId 기반 조회가 불가능하다.(sessionId를 통해 접속 해제)<br>
따라서, sessionID를 key로 가지는 데이터와, userId를 key로 가지는 데이터를 분리하여 저장한다. <br>

#### 상태관리 정보 Entity
```java
@NoArgsConstructor
@Data
@RedisHash("realTimeData")
public class RealTimeData {
    @Id
    @Indexed
    private String userId;
    private Long roomId;
    private String sessionId;
    private String studyTime;
//    // 현재시각 - 타이머 시작 시간
//    private LocalDateTime studyStartTime;
//    // 이전까지 기록된 총 공부 시간
//    private Duration recordTime;
    public UserDto toUserStudyTime() {
        return UserDto.builder()
                .userId(this.userId)
                .studyTime(this.studyTime)
                .build();
    }
}
```
<br>

#### Redis 로직
```java
@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String,RealTimeData> realTimeDataRedisTemplate;
    private final RedisTemplate<String,String> sessionRedisTemplate;
    public RealTimeData findRealTimeData(String userId) {
        HashOperations<String, Object, Object> hashOps = realTimeDataRedisTemplate.opsForHash();
        return (RealTimeData) hashOps.get("realTimeData:" + userId, "data");
    }
    public RealTimeData saveRealTimeData(RealTimeData realTimeData) {
        HashOperations<String, Object, Object> hashOps = realTimeDataRedisTemplate.opsForHash();
        hashOps.put("realTimeData:" + realTimeData.getUserId(), "data", realTimeData);

        return realTimeData;
    }
    public String findUserIdBySessionId(String sessionId) {
        Object userIdObj = sessionRedisTemplate.opsForValue().get("sessionIdIndex:" + sessionId);
        return userIdObj != null ? (String) userIdObj : null;
    }
    public RealTimeData saveRealTimeDataAndSession(RealTimeData realTimeData) {
        HashOperations<String, Object, Object> hashOps = realTimeDataRedisTemplate.opsForHash();
        hashOps.put("realTimeData:" + realTimeData.getUserId(), "data", realTimeData);
        sessionRedisTemplate.opsForValue().set("sessionIdIndex:" + realTimeData.getSessionId(), realTimeData.getUserId());

        return realTimeData;
    }
    public void deleteRealTimeDataAndSession(String userId, String sessionId) {
        realTimeDataRedisTemplate.delete("realTimeData:" + userId);
        sessionRedisTemplate.delete("sessionIdIndex:" + sessionId);
    }
    public void deleteSession(String userId) {
        sessionRedisTemplate.delete("sessionIdIndex:" + findRealTimeData(userId).getSessionId());
    }

    public List<RealTimeData> getAllRealTimeData() {
        //  "realTimeData:*"에 해당하는 모든 키를 가져옴
        Set<String> keys = realTimeDataRedisTemplate.keys("realTimeData:*");
        List<RealTimeData> allData = new ArrayList<>();

        if (keys != null) {
            for(String key : keys) {
                String userId = key.replace("realTimeData:", "");

                // Fetch the data for each user
                RealTimeData realTimeData = findRealTimeData(userId);
                if(realTimeData != null) {
                    allData.add(realTimeData);
                }
            }
        }

        return allData;
    }
    public void deleteAllData() {
        Set<String> keys = realTimeDataRedisTemplate.keys("realTimeData:*");

        if (keys!= null) {
            // realTimeData 삭제
            realTimeDataRedisTemplate.delete(keys);
            Set<String> sessionKeys = keys.stream()
                    .map(key -> findRealTimeData(key.replace("realTimeData:", "")).getSessionId())
                    .map(sessionId->"sessionIdIndex:"+sessionId)
                    .collect(Collectors.toSet());
            // sessionIdIndex 삭제
            sessionRedisTemplate.delete(sessionKeys);

        }
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

