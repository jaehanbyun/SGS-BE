# SGS-BE 시그널링 서버
### 해당 리드미는 서버에 대한 상세 정보를 담고 있습니다. 프로젝트에 관한 정보는 우측 링크에서 확인해주세요. [SGS-BE](https://github.com/jaehanbyun/SGS-BE)
###### 시그널링 서버 담당 : [김돈우](https://github.com/kimdonwoo)

### 개요
- 목적 : WebRTC 방에서 사용자 간 실시간 미디어(캠/화면/목소리) 공유 및 실시간 공부시간 공유
- 설명 : 사용자는 스터디방에 입장하면 같은 방 유저들과 미디어(캠/화면/목소리)를 실시간으로 공유할 수 있다. 
서로 다른 네트워크에 있는 유저들이 미디어를 공유하기 위해서는 다른 유저들의 정보를 
알아야 하는데, 각 유저들의 정보를 전달해주는 역할을 해당 시그널링 서버가 담당한다. 쉽게 설명하자면, 유저들끼리 연결에 필요한 각자의 정보를 중계해주는 역할을 하는 서버이다.
그리고 스터디방에 있는 Timer On/Off를 통해 실시간으로 유저들끼리 각자의 공부시간 타이머를 공유할 수 있다.


- 서버 : Spring Boot
- 미디어 서버 : Kurento Media Server
- STUN/TURN 서버 : COTURN Server

## 주요 기능
- 시그널링 기능 : 해당 시그널링 서버를 통해 본인의 정보를 같은 방에 있는 다른 유저들한테 전달해주고, 다른 유저들의 정보도 받아 미디어(캠/화면/목소리) 공유를 한다. 
- 공부시간 공유 기능 : 유저가 해당 방에서 timer를 on/off 하면 시그널링 서버를 통해 같은 방 유저들에게 실시간으로 공유해준다.
- 스케줄링 기능 : 새벽 5시마다 현재 서비스를 이용하고 있는 유저들의 공부시간을 모두 상태관리서버로 보내고 초기화 해준다.


## 상세 설명

### 시그널링 기능

<br>

#### WebSocketConfig.java
```java
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {
    
    //...

    private final MessageHandler messageHandler;
    //...

// socket으로 요청이 왔을때 meesageHandler로
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(messageHandler, "/socket")
                .setAllowedOriginPatterns("*");

    }
}
```
- 유저는 방에 입장하면 해당 시그널링 서버로 웹소켓 연결을 하게 되고, registry.addHandler를 통해 유저가 서버로 메시지를 보내면 MessageHandler.java에서 처리를 한다.
- 유저와 시그널링 서버간에 주고받는 Request/Response 메시지의 구조는 [다음](https://www.notion.so/4d4987e3e9cf4076bcd2f533f916836e?pvs=4)과 같다.
<br>

#### MessageHandler.java

```java
@Slf4j
@Component
@RequiredArgsConstructor

public class MessageHandler extends TextWebSocketHandler {

    // ...
    
    // 해당 webSocketsession으로 메시지 보내기
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {// 해당 session으로부터 message가 날라옴
        try {
            
            final JsonObject jsonMessage = gson.fromJson(message.getPayload(), JsonObject.class);
            final UserSession user = userRegistry.getBySession(session);

            if (user != null) {
                log.info("Incoming message from user     '{}': {}", user.getUserId(), jsonMessage);
            } else {
                log.info("Incoming message from new user: {}", jsonMessage);
            }

            switch (jsonMessage.get(ID).getAsString()) {
                // 방 접속
                case "joinRoom":
                    log.info("방접속");
                    JoinRequest joinRequest = mapper.readValue(message.getPayload(), JoinRequest.class);
                    join(joinRequest, session);
                    break;

                // SDP 정보 전송
                case "receiveVideoFrom":
                    log.info("SDP 정보 전송");
                    // json -> java 객체
                    ReceiveVideoRequest request
                            = mapper.readValue(message.getPayload(), ReceiveVideoRequest.class);

                    //userID 는 request 보낸 놈의 userId임
                    final String userId = request.getUserId();
                    final UserSession sender = userRegistry.getByUserId(userId);
                    final String sdpOffer = request.getSdpOffer();

                    // 즉, user는 sender로부터 SDP offer를 받고 처리해서 비디오 스트림을 받을 준비를 함
                    user.receiveVideoFrom(sender, sdpOffer);
                    break;

                // ICE Candidate 정보 전송
                case "onIceCandidate":
                    CandidateRequest candidateRequest
                            = mapper.readValue(message.getPayload(), CandidateRequest.class);
                    CandidateRequest.Candidate candidate = candidateRequest.getCandidate();

                    if (user != null) {
                        IceCandidate cand = new IceCandidate(
                                candidate.getCandidate(),
                                candidate.getSdpMid(),
                                candidate.getSdpMLineIndex());
                        user.addCandidate(cand, candidateRequest.getUserId());
                    }
                    break;

                // 비디오 설정 변경
                case "videoState":
                    VideoRequest videoRequest
                            = mapper.readValue(message.getPayload(), VideoRequest.class);
                    updateVideo(videoRequest, user);
                    break;

                // 오디오 설정 변경
                case "audioState":
                    AudioRequest audioRequest
                            = mapper.readValue(message.getPayload(), AudioRequest.class);
                    updateAudio(audioRequest, user);
                    break;

                // ...
                
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // ...

    private void join(JoinRequest request, WebSocketSession session) throws IOException {

        final String userId = request.getUserId();
        final Long roomId = request.getRoomId();
        final boolean video = request.isVideo();
        final boolean audio = request.isAudio();
        
        //LocalTime studyTime = LocalTime.of(0,0,0);
        String userStudyTime = userStudyTimeFromTCP(userId);

        TCPStudyTimeResponse response = mapper.readValue(TCPConverter(userStudyTime), TCPStudyTimeResponse.class);
        LocalTime studyTime;
        if(response.getStudyTime() == null){
            studyTime = LocalTime.of(0,0,0);
        }else{
            studyTime = LocalTime.parse(response.getStudyTime());
        }

        Room room = roomManager.getRoom(roomId);
        final UserSession newUser = room.join(userId, session, video, audio,studyTime);

        if (Objects.isNull(newUser)) return;
        userRegistry.register(newUser);

    }

    private void updateVideo(VideoRequest request, UserSession user) throws IOException {
        final Room room = roomManager.getRoom(user.getRoomId());
        room.updateVideo(request);
    }

    private void updateAudio(AudioRequest request, UserSession user) throws IOException {
        final Room room = roomManager.getRoom(user.getRoomId());
        room.updateAudio(request);
    }

    private String userStudyTimeFromTCP(String userId) {
        String tcpMessage;
        // TCP 서버로 userId보내고 userId랑 studyTime을 response로 받음
        tcpMessage = TCPUserRequest.builder()
                .server("signaling")
                .type("STUDY_TIME_FROM_TCP")
                .userId(userId)
                .build().toString();

        log.info("[tcp from state] request {} user's studyTime",userId);
        return tcpMessageService.sendMessage(tcpMessage);

    }
    //...

}
```
- (여기 SDP, ICE Candidate 관련 설명 추가 예정) 
- 해당 서비스는 상태관리서버에서 유저들의 당일 공부 시간을 관리하고 있다. 따라서 유저가 방에 입장할 때, Spring Integration을 통해 
상태관리서버로부터 해당 유저의 공부시간을 가져와 유저의 객체를 생성한다.


<br>

#### 방 나갈 때 (WebSocket 연결 끊길 시)

```java
@Slf4j
@Component
@RequiredArgsConstructor
// 클라이언트로부터 메시지를 수신하고 해당 메시지를 다른 클라이언트에게 전달해줌
public class MessageHandler extends TextWebSocketHandler {

    //...

    // websocket 연결 끊길때
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("[ws] Session has been closed with status [{} {}]", status, session);
        // userRegistry에서 삭제하고 유저 세션 객체 들고옴
        final UserSession user = userRegistry.removeBySession(session);
        if (Objects.isNull(user)) return;

        // user의 타이머가 켜져 있는 상태일 때
        if(user.getTimer()){
            user.setTimer(false);
            user.countStudyTime(LocalTime.now(),user.getOnTime());
        }

        // 유저의 공부시간 상태관리서버로 보내기
        // 방장이면 CHANGE / 일반 유저 or 방 사라질때 KEEP

        String response = userStudyTimeToTCP(user);
        String res = TCPConverter(response);

        // 현재 방
        final Room room = roomManager.onlyGetRoom(user.getRoomId());
        // 현재 방에서 유저 지우기, 유저도 userSession close하기
        room.leave(user);
        // 만약 방에 아무도 없으면 해당 방 지우기
        if (room.getParticipants().isEmpty()) {
            roomManager.removeRoom(room);

            kurento.getServerManager().getPipelines().stream()
                    .filter(pipeline -> pipeline.getId().equals(room.getPipeLineId()))
                    .findAny().ifPresent(pipeline -> pipeline.release());
        }else{ // 방에 누군가 존재하면 (1명일때도)
            Gson gson = new Gson();
            TCPOwnerResponse tcpOwnerResponse = gson.fromJson(res, TCPOwnerResponse.class);
            if(tcpOwnerResponse.getType().equals("CHANGE")){
                // tcpOwnerResponse.getUserId() 에서 받은 userId가 새로운 방장임을 알림
                room.delegateOwner("DELEGATE",tcpOwnerResponse.getUserId());
            }
        }
    }
    // ...
    private String userStudyTimeToTCP(UserSession user) {
        String tcpMessage;
        // TCP 서버로 userId랑 studyTime 보내줌
        tcpMessage = TCPTimerRequest.builder()
                .server("signaling")
                .type("STUDY_TIME_TO_TCP")
                .userId(user.getUserId())
                .studyTime(user.studyTimeToString())
                .build().toString();

        log.info("[tcp to state] {} user's studyTime : {}",user.getUserId(),user.studyTimeToString());
        return tcpMessageService.sendMessage(tcpMessage);
    }
    
}

```

- 유저가 방을 나가거나 브라우저를 닫아서 웹소켓이 끊기게 되면, userStudyTimeToTCP(UserSession user) 메소드를 사용해 해당 방에서 공부한 시간을 상태관리서버로 보내준다.
- 그리고 userRegistry.java랑 Room.java에서 해당 유저를 삭제한 후, 유저 객체를 close한다. 만약 마지막 유저가 나가서 해당 방의 인원이 0명이 되면 해당 Room 객체를 삭제해준다.
- 공개방에서 방장이 나가게 된다면 남아 있는 사람들 중, 가장 먼저 들어온 유저가 방장이 되도록 구현하였다.
    ```
      1. 유저가 방을 나가게 되면 시그널링 서버에서 상태관리 서버로 해당 유저의 공부시간이 담긴 Request를 보낸다.
      2. 상태관리 서버는 해당 유저의 정보가 담긴 Request를 룸서버에 날려 나간 유저가 방장인지 확인한다.
      3. 만약 나간 유저가 방장이라면 룸서버는 상태관리 서버로부터 해당 방의 남은 유저 중 가장 먼저 들어온 유저를 방장으로 바꾸고 상태관리버서로 Response를 보낸다.
      4. 상태관리서버는 받은 Response를 그대로 시그널링 서버로 보내준다.
      5. 시그널링 서버에서는 해당 Response를 받으면 해당 방에 남아 있는 모든 유저들에게 새로운 방장의 Id가 담긴 메시지를 보내준다.
    ```

<br>

### 공부시간 공유 기능

```java
@Slf4j
@Component
@RequiredArgsConstructor
// 클라이언트로부터 메시지를 수신하고 해당 메시지를 다른 클라이언트에게 전달해줌
public class MessageHandler extends TextWebSocketHandler {

    // ...

    // 해당 webSocketsession으로 메시지 보내기
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {// 해당 session으로부터 message가 날라옴
        try {


            final JsonObject jsonMessage = gson.fromJson(message.getPayload(), JsonObject.class);
            final UserSession user = userRegistry.getBySession(session);

            if (user != null) {
                log.info("Incoming message from user     '{}': {}", user.getUserId(), jsonMessage);
            } else {
                log.info("Incoming message from new user: {}", jsonMessage);
            }

            switch (jsonMessage.get(ID).getAsString()) {
                // ...

                // 타이머 설정 변경
                case "timerState":
                    // id, userid, timerState / user : userSession type
                    TimerRequest timerRequest = mapper.readValue(message.getPayload(), TimerRequest.class);
                    updateTimer(timerRequest, user);
                    break;

                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void updateTimer(TimerRequest request, UserSession user) throws IOException {
        log.info("[in {} room] {}'s timer request : timerState - {} , time - {}",user.getRoomId(),user.getUserId(),request.isTimerState(),request.getTime());
        final Room room = roomManager.getRoom(user.getRoomId());
        room.updateTimer(request,user);

    }
}
```


- 같은 방(스터디 그룹) 유저들끼리 실시간으로 본인들의 공부시간을 공유할 때, 매 초마다 유저의 공부시간을 시그널링 서버를 통해서
공유하는 방식으로 하게 되면 서버에 부담이 너무 심하다. 따라서 Timer On/Off 버튼을 누를때에만 해당 서버로 유저가 메시지를 날리고 다른 유저들에게도 공유되도록 구현하였다.

- 각 유저 객체마다 공부시간, 타이머상태, 타이머 On을 누른시각의 데이터를 변수로 가지고 있다. 
제일 처음 방에 입장하면 공부시간은 상태관리서버로 부터 가져온 공부시간(해당 날에 누적된 공부시간), 타이머 상태는 False를 값으로 가지게 된다.

- 현재 타이머 상태가 False인 유저는 공부시간 변수만 화면에 고정으로 출력해주면 된다.
타이머 상태가 On인 유저는 (타이머 On 누르기 전까지의 공부시간) + (현재 시각) - (타이머 On을 누른 시각)을 화면에 출력해주면 현재 시각이 계속 1초씩 증가하기 때문에
    같은 방에 있는 유저들의 (타이머 On 누르기 전까지의 공부시간)과 (타이머 On을 누른 시각)만 서로 공유할 수 있게 구현한다면 실시간 공유가 가능하다.

- 유저가 timer 상태를 바꿀 때

    ```
        [timer off -> on 일때]
        1. 현재 타이머 상태가 Off인 유저가 타이머를 On을 한다면, 시그널링 서버로 userId, 타이머 상태(timerState)와 타이머를 On으로 바꾼 시각(onTime)을 보낸다.
        2. 시그널링 서버에서 해당 유저 객체의 타이머 상태를 On으로 하고, 타이머를 On으로 바꾼 시각(onTime)을 저장하고 해당 방에 있는 모든 유저들에게도 userId, 타이머 상태(timerState)와 타이머를 On으로 바꾼 시각(onTime)을 보내준다.
        3. 해당 데이터를 받은 유저들의 화면에 해당 유저의 공부시간을 (타이머 On 누르기 전까지의 공부시간) + (현재 시각) - (타이머를 On으로 바꾼 시각)로 화면에 출력하면 공부시간을 실시간으로 공유할 수 있게 된다.
        
        [timer on -> off 일때]
        1. 현재 타이머 상태가 On인 유저가 타이머를 Off를 한다면, 시그널링 서버로 userId, 타이머 상태(timerState)와 타이머를 Off로 바꾼 시각(offTime)을 보낸다.
        2. 시그널링 서버에서 해당 유저 객체의 타이머 상태를 Off로 하고, 공부시간 변수에 (기존 공부시간 변수에 저장된 값) + ((타이머를 Off로 바꾼 시각) - (이전에 타이머를 On으로 바꿨던 시각))을 저장한다.
        3. 그리고 해당 방에 있는 모든 유저들에게 해당 유저의 공부시간과 타이머 상태들을 시그널링 서버를 통해 보내준다.
        4. 해당 데이터를 받은 유저들의 화면에 해당 유저의 공부시간를 화면에 고정으로 출력해주면 실시간으로 공유가 가능하다.
    ```

<br>

### 스케줄링 기능
```java
@Slf4j
@Service
@RequiredArgsConstructor
public class SchedulerService {

    private final UserRegistry userRegistry;
    private final TCPMessageService tcpMessageService;

    @Scheduled(cron = "0 0 5 * * *")
    //@Scheduled(cron = "0 */5 * * * *")
    public void run() throws IOException {
        log.info("[05:00] Scheduling Start !!");

        int countInTCP = 0;
        int nowUserCount = 0;

        // 현재 접속 되어 있는 모든 유저 객체 들고옴
        Collection<UserSession> users = userRegistry.getAllUsers();
        List<Map<String, Object>> serializedUsers = new ArrayList<>();
        int userSize = users.size();

        if(userSize == 0){ // 아무도 없다면

            studyTimeScheduledTCP(serializedUsers,"SCHEDULER_LAST");
            log.info("[05:00] Scheduling End !!");
            return;
        }

        for (UserSession user : users) {

            if(user.getTimer()){ // 켜져있다면
                user.setTimer(false); // 끄고 시간 업데이트
                user.countStudyTime(LocalTime.now(),user.getOnTime());
            }

            serializedUsers.add(Map.of(
                    "study_time", user.studyTimeToString(),
                    "user_id", user.getUserId()
            ));

            user.setStudyTime(LocalTime.of(0,0,0));
            user.sendMessage(resetStudyTimeMessage(user.getUserId()));

            countInTCP++;
            nowUserCount++;
            if(nowUserCount == userSize) continue;

            if (countInTCP == 25) {
                studyTimeScheduledTCP(serializedUsers,"SCHEDULER");
                serializedUsers.clear();
                countInTCP = 0;
            }
        }
        if (!serializedUsers.isEmpty()) {
            studyTimeScheduledTCP(serializedUsers,"SCHEDULER_LAST");
        }

        log.info("[05:00] Scheduling End !!");
    }

    private void studyTimeScheduledTCP(List<Map<String, Object>> Users,String Type) {
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("server", "signaling_scheduling");
        jsonMap.put("type", Type);
        jsonMap.put("users", Users);

        Gson gson = new Gson();
        String json = gson.toJson(jsonMap);

        tcpMessageService.sendMessage(json);
    }
    
}
```

- 스케줄링 과정
    ```
    1. 해당 서비스에서 매일 새벽 5시를 기준으로 상태관리서버에서 유저서버로 모든 유저의 공부시간을 보낸다.
    그리고 유저서버 DB에 저장한 후, 유저들에게 차트로 과거에 측정한 공부시간을 제공하고 있다.
    2. 처음 방에 입장할 때, 시그널링 서버에서 상태관리서버로부터 유저의 공부시간을 받아와 유저 객체를 생성하고, 방을 나갈 때 상태관리서버로 추가적으로 공부한
    시간을 더한 값을 보내준다.
    3. 그렇기 때문에 스케줄링 시간인 새벽 5시에 만약 유저가 해당 서비스를 사용하고 있다면, 상태관리서버에 있는 공부시간이 제일 최신 공부시간이 아니다.
    4. 따라서 현재 서비스를 사용하고 있는 유저들의 공부시간을 모두 상태관리서버로 보내어 상태관리서버의 공부시간을 최신 정보로 업데이트를 해주고 난 후, 유저서버로 보내도록 구현하였다.
    5. 방을 나갈때(웹소켓 끊길 때) 시그널링 서버에서 상태관리서버로 공부시간을 보내는 메소드를 사용하게되면, 
    현재(새벽 5시) 서비스를 이용하고 있는 유저의 수(N) 만큼 메시지가 날라가기 때문에, 하나의 메시지 마다 25명의 데이터를 담아 메시지 수를 N/25로 줄여 성능 개선을 하였다.
    6. 상태관리서버로 보내고 난 후, 유저들의 타이머 시간을 00:00:00으로 타이머 상태를 Off로 초기화 해주었다.  
    ```

<br>

---
## 향 후 계획


...