# SGS-BE 룸 서버
### 해당 리드미는 서버에 대한 상세 정보를 담고 있습니다. 프로젝트에 관한 정보는 우측 링크에서 확인해주세요. [SGS-BE](https://github.com/jaehanbyun/SGS-BE)
###### 룸 서버 담당 : [김돈우](https://github.com/kimdonwoo)

### 개요
- 목적 : StudyHub 서비스의 공개방과 스터디 그룹을 관리하는 서버이다.
- 설명 : 사용자는 메인 화면에서 현재 생성되어 있는 공개 스터디방을 무한 스크롤 형식으로 조회가 가능하고, 제목 또는 채널 별로 검색이 가능하다. 
공개방 CRUD, 스터디 그룹 CRUD 및 가입코드 생성과 가입등의 API를 제공한다. 방 안에서 방장(스터디그룹장)은 유저들의 경고/강퇴/방장위임을 할 수 있다.

  
- 서버 : Spring Boot
- 데이터베이스 : MySQL


## 주요 기능

- 공개 스터디방 조회 + 검색 기능 구현 : 검색 조건에 맞는 방에 대해서 무한 스크롤 방식의 조회 기능을 제공한다. 
- 유저 경고/강퇴/방장위임 : 채팅 방 안에서 방장이 유저들에게 경고, 강퇴, 방장위임을 하면, websocket을 활용하여 실시간으로 유저들에게 전달할 수 있도록 구현하였다.
- 공개방 관련 API 구현 : 공개방 생성 / 수정 / 정보 조회 / 입장 / 퇴장 API를 개발하였다.
- 스터디 그룹 관련 API 구현 : 스터디 그룹 조회 / 생성 / 수정 / 정보 조회 / 가입 / 탈퇴 / 입장 / 퇴장 API를 개발하였다.

## 상세 설명


### 공개방 조회 (무한 스크롤 구현) + 검색

#### RoomService.java
```java

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomService {
    
    // ...
    
    @Transactional
    public List<RoomListResponse> roomList(Long lastRoomId, int size, String keyword, RoomChannel channel) {

        PageRequest pageRequest = PageRequest.of(0, size);

        List<OpenRoomEntity> entityList = openRoomRepository.findByRoomIdLessThanAndRoomNameContainingAndChannelOrderByRoomIdDesc(
                lastRoomId, keyword, channel, pageRequest).getContent();

        if (entityList.isEmpty()) {
            throw new ApplicationException(ErrorCode.NO_CONTENT, "");
        }

        List<RoomListResponse> responseList = new ArrayList<>();
        for (OpenRoomEntity roomEntity : entityList) {
            responseList.add(RoomListResponse.fromEntity(roomEntity));
        }

        return responseList;
    }

    // ...
}
```


<br>

#### OpenRoomRepository.java
```java
@Repository
public interface OpenRoomRepository extends JpaRepository<OpenRoomEntity, Long> {

    // 무한 스크롤 처리
    @Query(value = "SELECT o FROM OpenRoomEntity o WHERE o.roomId < :lastRoomId AND (:Keyword IS NULL OR o.roomName LIKE %:Keyword%) " +
            "AND (:Channel IS NULL OR o.channel = :Channel) ORDER BY o.roomId DESC")
    Page<OpenRoomEntity> findByRoomIdLessThanAndRoomNameContainingAndChannelOrderByRoomIdDesc(
            @Param("lastRoomId") Long lastRoomId, @Param("Keyword") String keyword, @Param("Channel") RoomChannel channel, PageRequest pageRequest);



}
```

- 검색 키워드나 채널이 들어오면 해당 조건에 충족하는 방만 들고오도록 JPQL을 활용해서 No-Offset 방식의 무한스크롤 방식을 구현하였다.
- 서비스 특성상 공개방 조회 API에서 부하가 심할 것으로 예상하고, ngrinder를 활용하여 부하테스트를 진행하였다.

  +(그림 추가 예정)

  Offset방식과 No-Offset방식의 무한 스크롤 조회 기능의 성능 테스트를 통해 TPS 기준 약 250%의 성능향상을 확인할 수 있었다.

<br>

---

### 유저 경고/강퇴/방장위임 매커니즘
#### RoomService.java
```java

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomService {
    
    // ...
    
    @Transactional
    public RoomTargetResponse delegate(Boolean roomType, Long roomId, String userId, String targetId){
        if(roomType) { // 공개방
            // roomId로 db에 있는지 확인 (없으면 Exception 던짐)
            OpenRoom.fromEntity(checkRoomId(roomId));

            // roomOwner가 roomId의 방장인지 확인 (권한 없으면 Exception 던짐)
            OpenUserRoomEntity owner = checkRoomOwner(roomId, userId);

            // targetId가 roomId의 멤버인지
            OpenUserRoomEntity target = userRoomRepository.findById(new UserRoomId(targetId, roomId)).orElseThrow(() ->
                    new ApplicationException(ErrorCode.User_NOT_FOUND, String.format("%s User is not founded in %d room", targetId, roomId)));

            // 방장 위임
            owner.delegate(target);
            TCPToState(TCPToStateType.DELEGATE,target);

            return new RoomTargetResponse(roomId,target.getUserId());
        }else{
            checkPrivateRoomId(roomId);
            PrivateUserRoomEntity owner = checkPrivateRoomOwner(roomId, userId);
            PrivateUserRoomEntity target = privateUserRoomRepository.findById(new UserRoomId(targetId, roomId)).orElseThrow(() ->
                    new ApplicationException(ErrorCode.User_NOT_FOUND, String.format("%s User is not founded in %d study group", targetId, roomId)));

            owner.delegate(target);
            TCPToState(TCPToStateType.DELEGATE,target);


            return new RoomTargetResponse(roomId,target.getUserId());
        }


    }

    @Transactional
    public RoomTargetResponse kickout(Boolean roomType, Long roomId, String userId, String targetId){
        if(roomType) { // 공개방
            // roomId로 db에 있는지 확인 (없으면 Exception 던짐)
            OpenRoom.fromEntity(checkRoomId(roomId));
            // roomOwner가 roomId의 방장인지 확인 (권한 없으면 Exception 던짐)
            checkRoomOwner(roomId, userId);
            // targetId가 roomId의 멤버인지
            OpenUserRoomEntity target = userRoomRepository.findById(new UserRoomId(targetId, roomId)).orElseThrow(() ->
                    new ApplicationException(ErrorCode.User_NOT_FOUND, String.format("%s User is not founded in %d room", targetId, roomId)));

            // 추방
            target.kickOut();
            TCPToState(TCPToStateType.KICK_OUT,target);

            return new RoomTargetResponse(roomId,target.getUserId());
        }else{
            PrivateRoomEntity studyGroup = checkPrivateRoomId(roomId);
            checkPrivateRoomOwner(roomId, userId);
            PrivateUserRoomEntity target = privateUserRoomRepository.findById(new UserRoomId(targetId, roomId)).orElseThrow(() ->
                    new ApplicationException(ErrorCode.User_NOT_FOUND, String.format("%s User is not founded in %d study group", targetId, roomId)));

            target.kickOut();
            target.setMember(false);
            TCPToState(TCPToStateType.KICK_OUT,target);
            studyGroup.minusUser();

            return new RoomTargetResponse(roomId,target.getUserId());
        }
    }

    @Transactional
    public AlertResponse alert(Boolean roomType, Long roomId, String userId, String targetId){

        if(roomType) { // 공개방
            // roomId로 db에 있는지 확인 (없으면 Exception 던짐)
            OpenRoom.fromEntity(checkRoomId(roomId));
            // roomOwner가 roomId의 방장인지 확인 (권한 없으면 Exception 던짐)
            checkRoomOwner(roomId, userId);
            // targetId가 roomId의 멤버인지
            OpenUserRoomEntity target = userRoomRepository.findById(new UserRoomId(targetId, roomId)).orElseThrow(() ->
                    new ApplicationException(ErrorCode.User_NOT_FOUND, String.format("%s User is not founded in %d room", targetId, roomId)));

            // 경고
            if(target.addAlert() < 3) {
                TCPToState(TCPToStateType.ALERT, target);
            }else{
                TCPToState(TCPToStateType.KICK_OUT_BY_ALERT, target);
            }

            return new AlertResponse(roomId,target.getUserId(),target.getAlert());
        }else{
            checkPrivateRoomId(roomId);
            checkPrivateRoomOwner(roomId, userId);
            PrivateUserRoomEntity target = privateUserRoomRepository.findById(new UserRoomId(targetId, roomId)).orElseThrow(() ->
                    new ApplicationException(ErrorCode.User_NOT_FOUND, String.format("%s User is not founded in %d study group", targetId, roomId)));

            if(target.addAlert() < 3) {
                TCPToState(TCPToStateType.ALERT, target);
            }else{
                TCPToState(TCPToStateType.KICK_OUT_BY_ALERT, target);
            }

            return new AlertResponse(roomId,target.getUserId(),target.getAlert());
        }
    }
    
    // ...
    
}

```
- 공개방이나 스터디그룹에서 방장이 추방/경고/방장위임을 하면, 룸 서버와 연결되어 있는 DB에 반영이 된다.
- 실시간으로 추방/경고/방장위임을 받은 유저에게 알리기 위해, (룸 서버) -> (상태관리 서버) -> (시그널링 서버)를 통해 해당 유저와 연결된 websocket을 활용하여 같은 방 유저들에게 메시지를 전달하도록 구현하였다.


<br>

---

### 공개방 관련 API

- 공개방 조회(위에서 다룸) / 생성 / 수정 / 정보 조회 등의 API를 구현하였다.
- 공개방 입장/퇴장 API를 구현하였다.

```
    - 공개방은 입장과 퇴장만 존재한다.
    - 만약 방장이 공개방에서 퇴장을 하게 되면, 현재 남아 있는 멤버들 중 가장 먼저 들어온 유저가 방장이 되도록 구현하였다.
    - 유저들이 방을 나가지 않고 브라우저를 강제로 종료할 수 있으니 웹소켓 연결이 끊어질 때를 기준으로 방 나가기를 구현하였다.
    - 마지막 유저가 방을 나가서 방의 인원이 0명이 되면 방이 삭제되도록 구현하였다.
```

(따로 코드 첨부는 하지 않았습니다.)

<br>

---

### 스터디 그룹 관련 API

- 스터디 그룹 조회 / 생성 / 수정 / 정보 조회 등의 API 구현하였다.
- 스터디 그룹 가입/탈퇴/입장/퇴장 API를 구현하였다.

```
    - 스터디 그룹은 공개방과 다르게 입장/퇴장뿐 만 아니라 가입/탈퇴도 존재한다.
    
    - 이러한 방식 때문에 공개방과 다르게 구현하였다.
        1. 먼저 스터디 그룹에 가입한 멤버만 해당 스터디 그룹 방에 입장할 수 있다.
        2. 유저가 특정 스터디 그룹 방에 대해서 퇴장을 하여도 탈퇴를 하지 않았으면 계속해서 해당 스터디 그룹의 멤버이다.
        3. 스터디 그룹장 또한 방을 나가더라도 새로운 그룹장이 탄생하지않고 계속해서 스터디 그룹장이다.
    
    - 스터디 그룹 가입하는 방법
        1. 스터디 그룹장이 스터디 그룹 코드 생성을 하고 코드를 전달한다.
        2. 유저들은 메인페이지에서 그룹 가입을 누르고 받은 코드를 입력하면 해당 스터디 그룹에 가입이 된다. (유저 한명당 스터디 그룹 5개만 가입가능하도록 구현)
        3. 가입된 유저는 자유롭게 스터디 그룹 방에 참가하여 같은 그룹 멤버들과 미디어와 공부시간을 공유하며 공부 진행이 가능하다.
```

(따로 코드 첨부는 하지 않았습니다.)

<br>


