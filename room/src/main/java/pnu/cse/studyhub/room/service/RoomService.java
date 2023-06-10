package pnu.cse.studyhub.room.service;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pnu.cse.studyhub.room.config.TcpMessageService;
import pnu.cse.studyhub.room.dto.request.tcp.TCPAlertRequest;
import pnu.cse.studyhub.room.dto.request.tcp.TCPDelegateRequest;
import pnu.cse.studyhub.room.dto.request.tcp.TCPKickoutRequest;
import pnu.cse.studyhub.room.dto.request.tcp.TCPToStateType;
import pnu.cse.studyhub.room.dto.response.*;
import pnu.cse.studyhub.room.model.entity.*;
import pnu.cse.studyhub.room.service.exception.ApplicationException;
import pnu.cse.studyhub.room.service.exception.ErrorCode;
import pnu.cse.studyhub.room.model.OpenRoom;
import pnu.cse.studyhub.room.model.RoomChannel;
import pnu.cse.studyhub.room.model.UserRoomId;
import pnu.cse.studyhub.room.repository.OpenRoomRepository;
import pnu.cse.studyhub.room.repository.PrivateRoomRepository;
import pnu.cse.studyhub.room.repository.PrivateUserRoomRepository;
import pnu.cse.studyhub.room.repository.UserRoomRepository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final OpenRoomRepository openRoomRepository;
    private final UserRoomRepository userRoomRepository;
    private final PrivateUserRoomRepository privateUserRoomRepository;
    private final PrivateRoomRepository privateRoomRepository;

    private final TcpMessageService tcpMessageService;


    //private study group 조회
    @Transactional
    public List<StudyGroupListResponse> studyGroupList(String userId){
        // 해당 userId의 is_member의 모든 roomname이랑 roomId 출력
        List<PrivateUserRoomEntity> userRoom = privateUserRoomRepository.findIsMemberByUserId(userId);

        if(userRoom.isEmpty()){
            throw new ApplicationException(ErrorCode.NO_CONTENT,"");
        }

        List<StudyGroupListResponse> responseList = new ArrayList<>();


        for(PrivateUserRoomEntity roomEntity : userRoom){
            responseList.add(new StudyGroupListResponse(roomEntity.getRoomId(), roomEntity.getPrivateRoomEntity().getRoomName()));
        }

        return responseList;

    }


    // private study group 입장
    @Transactional
    public void privateIn(Long roomId, String userId){
        // roomId 체크
        checkPrivateRoomId(roomId);

        // 해당 방에 user있는지 체크 있어도 ismember이지 체크
        PrivateUserRoomEntity userRoom = privateUserRoomRepository.findById(new UserRoomId(userId, roomId)).orElseThrow(() ->
                new ApplicationException(ErrorCode.User_NOT_FOUND,
                        String.format("%s User is not founded in %d study group", userId, roomId)));

        if(userRoom.isMember()){
            userRoom.setAccessedAt(Timestamp.from(Instant.now()));

        }else{
            throw new ApplicationException(ErrorCode.User_NOT_FOUND,
                    String.format("%s User is not founded in %d study group", userId, roomId));
        }
    }

    // roomID가 필요 없어야함
    // roomCode로만 roomID 방을 찾기
    @Transactional
    public void join(String userId,UUID roomCode){

        // 요청한 userId가 속한 Private 스터디 그룹이 5개일 때 , Private 스터디 그룹 생성 불가능 (유저 권한)
        if(privateUserRoomRepository.findIsMemberByUserId(userId).size() >= 5){
            throw new ApplicationException(ErrorCode.INVALID_PERMISSION, String.format("%s user has already joined 5 study groups ",userId));
        }
        System.out.println("UUID ROOM CODE : " + roomCode);

        // 해당 roomCode를 가진 방이 있는지 확인하고 없으면 exception
        PrivateRoomEntity studyGroup = privateRoomRepository.findByRoomCode(roomCode).orElseThrow(() ->
                new ApplicationException(ErrorCode.ROOM_NOT_FOUND, "This roomCode room does not exist."));

        // 만약 스터디 그룹의 현재 인원이 Max면 Exception
        if(studyGroup.getCurUser().equals(studyGroup.getMaxUser())){
            throw new ApplicationException(ErrorCode.MAX_USER,String.format("%d study group is full",studyGroup.getRoomId()));
        }

        // 그룹에 인원 증가
        studyGroup.addUser();

        Optional<PrivateUserRoomEntity> userRoom = privateUserRoomRepository.findById(new UserRoomId(userId, studyGroup.getRoomId()));

        if(userRoom.isPresent()){ // 재입장 유저
            //user의 kick_out으로 해당 방 권한 확인
            if(userRoom.get().getKick_out()){
                throw new ApplicationException(
                        ErrorCode.INVALID_PERMISSION,String.format("%s User is kicked out of the %d Room",userId,studyGroup.getRoomId()));
            }
            userRoom.get().setMember(true);

        }else{ // 첫입장 유저
            PrivateUserRoomEntity newUserRoom = privateUserRoomRepository.save(PrivateUserRoomEntity.create(userId, studyGroup.getRoomId(), false));
            studyGroup.addUserRoom(newUserRoom);
        }

    }

    // TODO : 리팩토링
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
            checkPrivateRoomId(roomId);
            checkPrivateRoomOwner(roomId, userId);
            PrivateUserRoomEntity target = privateUserRoomRepository.findById(new UserRoomId(targetId, roomId)).orElseThrow(() ->
                    new ApplicationException(ErrorCode.User_NOT_FOUND, String.format("%s User is not founded in %d study group", targetId, roomId)));

            target.kickOut();
            TCPToState(TCPToStateType.KICK_OUT,target);

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

    @Transactional
    public DetailResponse info(Long roomId){
        // roomId로 db에 있는지 확인 (없으면 Exception 던짐)
        OpenRoom openRoom = OpenRoom.fromEntity(checkRoomId(roomId));

        // 방장 이름
        String roomOwnerId= userRoomRepository.findRoomOwnerByRoomId(roomId).getUserId();

        // TODO : 더보기에 출력해줄 내용들 추가

        return new DetailResponse(openRoom.getRoomId(), openRoom.getRoomName(),openRoom.getChannel(), openRoom.getRoomNotice()
                ,roomOwnerId, openRoom.getCurUser(), openRoom.getMaxUser(), openRoom.getCreatedAt());

    }

    @Transactional
    public List<RoomListResponse> roomList(Long lastRoomId, int size , String keyword, RoomChannel channel){

        PageRequest pageRequest = PageRequest.of(0,size);

        List<OpenRoomEntity> entityList = openRoomRepository.findByRoomIdLessThanAndRoomNameContainingAndChannelOrderByRoomIdDesc(
                lastRoomId,keyword,channel, pageRequest).getContent();

        if(entityList.isEmpty()){
            throw new ApplicationException(ErrorCode.NO_CONTENT,"");
        }

        List<RoomListResponse> responseList = new ArrayList<>();
        for(OpenRoomEntity roomEntity: entityList){
            responseList.add(RoomListResponse.fromEntity(roomEntity));
        }

        return responseList;
    }

    // 공지사항 설정
    @Transactional
    public String notice(boolean roomType, Long roomId, String userId,String roomNotice){
        if(roomType){
            // roomId로 db에 있는지 확인 (없으면 Exception 던짐)
            OpenRoomEntity openRoom = checkRoomId(roomId);
            // 해당 유저가 방장인지 확인 (권한 확인)
            checkRoomOwner(roomId,userId);
            // 공지 사항 설정 (JPA 변경 감지)
            openRoom.setRoomNotice(roomNotice);

            return openRoom.getRoomNotice();
        }else{
            PrivateRoomEntity privateRoom = checkPrivateRoomId(roomId);
            checkPrivateRoomOwner(roomId,userId);
            privateRoom.setRoomNotice(roomNotice);

            return privateRoom.getRoomNotice();
        }
    }


    // 탈퇴 시 유저 -1
    @Transactional
    public void withdraw(String userId, Long roomId){
        PrivateRoomEntity studyGroup = checkPrivateRoomId(roomId);
        PrivateUserRoomEntity userRoom = privateUserRoomRepository.findById(new UserRoomId(userId, roomId)).orElseThrow(() ->
                new ApplicationException(ErrorCode.User_NOT_FOUND, String.format("%s not founded", userId)));
        if(userRoom.isRoomOwner()){ // 방장일때
            privateRoomRepository.delete(studyGroup);
        }else{
            userRoom.setMember(false);
            studyGroup.minusUser();
        }
    }

    @Transactional
    public String stateTCP(Long roomId, String userId){
        Optional<OpenRoomEntity> openRoomEntity = openRoomRepository.findById(roomId);

        if(openRoomEntity.isPresent()) { // 공개방이면
            Optional<OpenUserRoomEntity> openUserRoom = userRoomRepository.findById(new UserRoomId(userId, roomId));

            int userCount = openRoomEntity.get().minusUser();
            System.out.println("인원 수 : " + userCount);

            if(userCount == 0){
                openRoomRepository.delete(openRoomEntity.get());

            }else{ // 인원 존재할 때
                // 1. userRoom에 leaved_at 기록
                openUserRoom.get().setLeftAt(Timestamp.from(Instant.now()));

                if(openUserRoom.get().isRoomOwner()){ // 방장
                    // 2. leaved_at 기준으로 방장 위임
                    List<OpenUserRoomEntity> NextOwner =
                            userRoomRepository.findFastestAccessedRooms(roomId, PageRequest.of(0, 1));

                    System.out.println("다음 방장 :" + NextOwner.get(0).getUserId());

                    // 3. nextOwner한테 방장 위임하기
                    openUserRoom.get().delegate(NextOwner.get(0));

                    return "{\"type\":\"CHANGE\",\"user_id\":\""
                            +NextOwner.get(0).getUserId()+
                            "\",\"room_id\":"+NextOwner.get(0).getRoomId()+"}";
                }else{ // 일반 유저일때
                    return "{\"type\" : \"KEEP\"}";
                }
            }
        }
        // 스터디 그룹일 때
        return "{\"type\" : \"KEEP\"}";
    }

    // 일반 user의 공개방 입장
    @Transactional
    public void in(Long roomId, String userId){
        // roomId로 db에 있는지 확인 (없으면 Exception 던짐)
        OpenRoomEntity openRoomEntity = checkRoomId(roomId);

        // 만약 현재 인원이 Max면 Exception
        if(openRoomEntity.getCurUser().equals(openRoomEntity.getMaxUser())){
            throw new ApplicationException(ErrorCode.MAX_USER,String.format("%d Room is full",roomId));
        }
        // 인원 증가
        openRoomEntity.addUser();

        // 해당방에 이전에 들어온적 있는 유저인지 check
        Optional<OpenUserRoomEntity> userRoom = userRoomRepository.findById(new UserRoomId(userId, roomId));

        if(userRoom.isPresent()){
            //user의 alert 또는 kick_out으로 해당 방 권한 확인
            if(userRoom.get().getKick_out()){
                throw new ApplicationException(
                        ErrorCode.INVALID_PERMISSION,String.format("%s User is kicked out of the %d Room",userId,roomId));
            }
            userRoom.get().setAccessedAt(Timestamp.from(Instant.now()));
            userRoom.get().setLeftAt(null);

        }else{ // 해당 방 첫 입장 (UserRoomRepository로 테이블에 추가 (일반 user))
            OpenUserRoomEntity newUserRoom = userRoomRepository.save(OpenUserRoomEntity.create(userId, roomId, false));
            openRoomEntity.addUserRoom(newUserRoom);
        }

    }


    @Transactional
    public Long modify(Boolean roomType, Long roomId, String userId,String roomName ,Integer maxUser, RoomChannel roomChannel){
        // TODO : 현재인원보다 작게 변경 x -> Exception
        if(roomType){
            // roomId로 db에 있는지 확인 (없으면 Exception 던짐)
            OpenRoomEntity openRoomEntity = checkRoomId(roomId);

            // 해당 roomId의 roomOwner가 userId인지 확인 (권한 없으면 Exception 던짐)
            checkRoomOwner(roomId, userId);

            // 내용이 동일할 경우 굳이 넣을 필요 없을듯??
            if(openRoomEntity.getRoomName().equals(roomName) &&
            openRoomEntity.getMaxUser().equals(maxUser) &&
            openRoomEntity.getChannel().equals(roomChannel)){
                throw new ApplicationException(ErrorCode.SAME_DATA,"");
            }

            // 해당 룸 수정
            openRoomEntity.setRoomName(roomName);
            openRoomEntity.setMaxUser(maxUser);
            openRoomEntity.setChannel(roomChannel);

            return openRoomRepository.saveAndFlush(openRoomEntity).getRoomId();

        }else{
            PrivateRoomEntity privateRoomEntity = checkPrivateRoomId(roomId);
            checkPrivateRoomOwner(roomId,userId);
            if(privateRoomEntity.getRoomName().equals(roomName) &&
                    privateRoomEntity.getMaxUser().equals(maxUser)){
                throw new ApplicationException(ErrorCode.SAME_DATA,"");
            }
            privateRoomEntity.setRoomName(roomName);
            privateRoomEntity.setMaxUser(maxUser);

            return privateRoomRepository.saveAndFlush(privateRoomEntity).getRoomId();
        }

    }


    @Transactional
    public Long create(Boolean roomType, String roomName, String userId, Integer maxUser, RoomChannel channel){
        if(roomType){ // 공개방 생성

            OpenRoomEntity saveRoom = openRoomRepository.save(OpenRoomEntity.create(roomName, channel, maxUser));

            OpenUserRoomEntity newUserRoom = userRoomRepository.save(
                    OpenUserRoomEntity.create(userId, saveRoom.getRoomId(), true));
            saveRoom.addUserRoom(newUserRoom);

            return saveRoom.getRoomId();

        }else {
            // 요청한 userId가 속한 Private 스터디 그룹이 5개일 때 , Private 스터디 그룹 생성 불가능 (유저 권한)
            if(privateUserRoomRepository.findIsMemberByUserId(userId).size() >= 5){
                throw new ApplicationException(ErrorCode.INVALID_PERMISSION, String.format("%s user has already joined 5 study groups ",userId));
            }

            PrivateRoomEntity saveRoom = privateRoomRepository.save(PrivateRoomEntity.create(roomName,maxUser, UUID.randomUUID()));
            System.out.println("code : " + saveRoom.getRoomCode());
            PrivateUserRoomEntity newUserRoom = privateUserRoomRepository.save(
                    PrivateUserRoomEntity.create(userId, saveRoom.getRoomId(),true));
            saveRoom.addUserRoom(newUserRoom);

            return saveRoom.getRoomId();
        }
    }

    // private 스터디 그룹 roomCode 들고오기
    public UUID generateCode(Long roomId, String userId){
        // roomId 존재하는지 확인
        PrivateRoomEntity studyGroup = checkPrivateRoomId(roomId);

        // 방장인지 확인
        checkPrivateRoomOwner(roomId, userId);

        studyGroup.setRoomCode(UUID.randomUUID());

        privateRoomRepository.saveAndFlush(studyGroup);

        return studyGroup.getRoomCode();
    }

    // 스터디 그룹용 roomId 확인
    private PrivateRoomEntity checkPrivateRoomId(Long roomId){
        PrivateRoomEntity privateRoomEntity = privateRoomRepository.findById(roomId).orElseThrow(() ->
                new ApplicationException(ErrorCode.ROOM_NOT_FOUND, String.format("%d study group is not founded", roomId)));
        return privateRoomEntity;
    }

    // 스터디 그룹용 roomOwner 확인
    private PrivateUserRoomEntity checkPrivateRoomOwner(Long roomId, String userId){
        Optional<PrivateUserRoomEntity> owner = privateUserRoomRepository.findById(new UserRoomId(userId, roomId));
        if(!owner.get().isRoomOwner()){
            throw new ApplicationException(ErrorCode.INVALID_PERMISSION,String.format("%s User is not %d Study group's RoomOwner", userId, roomId));
        }
        return owner.get();
    }


    // 공개방용 roomId 확인
    private OpenRoomEntity checkRoomId(Long roomId) {
        OpenRoomEntity openRoomEntity = openRoomRepository.findById(roomId).orElseThrow(()->
                new ApplicationException(ErrorCode.ROOM_NOT_FOUND,String.format("%d open Room is not founded", roomId)));
        return openRoomEntity;
    }

    // 공개방용 roomOwner 확인
    private OpenUserRoomEntity checkRoomOwner(Long roomId, String userId) {

        Optional<OpenUserRoomEntity> owner = userRoomRepository.findById(new UserRoomId(userId, roomId));

        if(!owner.get().isRoomOwner()){ // owner가 아니면 Exception
            throw new ApplicationException(ErrorCode.INVALID_PERMISSION,String.format("%s User is not %d Open Study Room's RoomOwner", userId, roomId));
        }
        return owner.get();
    }

    // 경고, 강퇴 관련 상태관리서버로 TCP 보내기
    /*
        경고 처리를 어떻게 하지..?

     */
    private void TCPToState(TCPToStateType type, UserRoom target) {
        String tcpMessage = null;

        switch (type){
            case KICK_OUT:
            case KICK_OUT_BY_ALERT :
                tcpMessage = TCPKickoutRequest.builder()
                        .server("room")
                        .type(String.valueOf(type))
                        .userId(target.getUserId())
                        .roomId(target.getRoomId())
                        .build().toString();
                break;

            case ALERT :
                tcpMessage = TCPAlertRequest.builder()
                        .server("room")
                        .type(String.valueOf(type))
                        .userId(target.getUserId())
                        .roomId(target.getRoomId())
                        .alertCount(target.getAlert())
                        .build().toString();
                break;

            case DELEGATE :
                tcpMessage = TCPDelegateRequest.builder()
                        .server("room")
                        .type(String.valueOf(type))
                        .userId(target.getUserId())
                        .roomId(target.getRoomId())
                        .build().toString();
                break;

            default:
                break;
        }
        System.out.println(tcpMessage);
        tcpMessageService.onlySendMessage(tcpMessage);
    }


}
