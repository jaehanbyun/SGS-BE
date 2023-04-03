package pnu.cse.studyhub.room.service;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pnu.cse.studyhub.room.dto.response.AlertResponse;
import pnu.cse.studyhub.room.dto.response.DetailResponse;
import pnu.cse.studyhub.room.dto.response.RoomListResponse;
import pnu.cse.studyhub.room.dto.response.RoomTargetResponse;
import pnu.cse.studyhub.room.exception.ApplicationException;
import pnu.cse.studyhub.room.exception.ErrorCode;
import pnu.cse.studyhub.room.model.OpenRoom;
import pnu.cse.studyhub.room.model.RoomChannel;
import pnu.cse.studyhub.room.model.UserRoomId;
import pnu.cse.studyhub.room.model.entity.OpenRoomEntity;
import pnu.cse.studyhub.room.model.entity.OpenUserRoomEntity;
import pnu.cse.studyhub.room.repository.ListRepository;
import pnu.cse.studyhub.room.repository.RoomRepository;
import pnu.cse.studyhub.room.repository.UserRoomRepository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoomService {

    //private final RoomRepository roomRepository;
    private final ListRepository listRepository;
    private final UserRoomRepository userRoomRepository;

    @Transactional
    public RoomTargetResponse delegate(Boolean roomType, Long roomId, String roomOwner, String targetId){
        if(roomType) { // 공개방
            // roomId로 db에 있는지 확인 (없으면 Exception 던짐)
            OpenRoom openRoom = OpenRoom.fromEntity(checkRoomId(roomId));

            // roomOwner가 roomId의 방장인지 확인 (권한 없으면 Exception 던짐)
            OpenUserRoomEntity owner = checkRoomOwner(roomId, roomOwner);

            // targetId가 roomId의 멤버인지
            OpenUserRoomEntity target = userRoomRepository.findById(new UserRoomId(targetId, roomId)).orElseThrow(() ->
                    new ApplicationException(ErrorCode.User_NOT_FOUND, String.format("%s User is not founded in %d room", targetId, roomId)));

            // 방장 위임
            owner.delegate(target);

            return new RoomTargetResponse(roomId,target.getUserId());
        }

        // TODO : 스터디 그룹
        return new RoomTargetResponse(roomId,"스터디 그룹");

    }

    @Transactional
    public RoomTargetResponse kickout(Boolean roomType, Long roomId, String roomOwner, String targetId){
        if(roomType) { // 공개방
            // roomId로 db에 있는지 확인 (없으면 Exception 던짐)
            OpenRoom openRoom = OpenRoom.fromEntity(checkRoomId(roomId));

            // roomOwner가 roomId의 방장인지 확인 (권한 없으면 Exception 던짐)
            checkRoomOwner(roomId, roomOwner);

            // targetId가 roomId의 멤버인지
            OpenUserRoomEntity target = userRoomRepository.findById(new UserRoomId(targetId, roomId)).orElseThrow(() ->
                    new ApplicationException(ErrorCode.User_NOT_FOUND, String.format("%s User is not founded in %d room", targetId, roomId)));

            // 추방
            target.kickOut();

            return new RoomTargetResponse(roomId,target.getUserId());
        }

        // TODO : 스터디 그룹
        return new RoomTargetResponse(roomId,"스터디 그룹");

    }

    @Transactional
    public AlertResponse alert(Boolean roomType, Long roomId, String roomOwner, String targetId){

        if(roomType) { // 공개방
            // roomId로 db에 있는지 확인 (없으면 Exception 던짐)
            OpenRoom openRoom = OpenRoom.fromEntity(checkRoomId(roomId));

            // roomOwner가 roomId의 방장인지 확인 (권한 없으면 Exception 던짐)
            checkRoomOwner(roomId, roomOwner);

            // targetId가 roomId의 멤버인지
            OpenUserRoomEntity target = userRoomRepository.findById(new UserRoomId(targetId, roomId)).orElseThrow(() ->
                    new ApplicationException(ErrorCode.User_NOT_FOUND, String.format("%s User is not founded in %d room", targetId, roomId)));

            // 경고
            target.addAlert();

            return new AlertResponse(roomId,target.getUserId(),target.getAlert());

        }
// TODO : 스터디 그룹
        return new AlertResponse(roomId,"스터디그룹",3);

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

        List<OpenRoomEntity> entityList = listRepository.findByRoomIdLessThanAndRoomNameContainingAndChannelOrderByRoomIdDesc(
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
        // roomId로 db에 있는지 확인 (없으면 Exception 던짐)
        OpenRoomEntity openRoom = checkRoomId(roomId);

        // 해당 유저가 방장인지 확인 (권한 확인)
        checkRoomOwner(roomId,userId);

        // 공지 사항 설정 (JPA 변경 감지)
        if(roomType){
            openRoom.setRoomNotice(roomNotice);
            return openRoom.getRoomNotice();
        }

        return "스터디 그룹";
    }

    @Transactional
    public void out(String userId, Long roomId){
        // roomId로 db에 있는지 확인 (없으면 Exception 던짐)
        OpenRoomEntity openRoomEntity = checkRoomId(roomId);

        // userRoom에 해당방에 user 있는지 체크
        OpenUserRoomEntity openUserRoomEntity = userRoomRepository.findById(new UserRoomId(userId, roomId)).orElseThrow(() ->
                new ApplicationException(ErrorCode.User_NOT_FOUND, String.format("%s not founded", userId)));

        // 해당 userId가 해당 roomId의 roomOwner이면 삭제
        if(openUserRoomEntity.isRoomOwner()){
            listRepository.delete(openRoomEntity);
        }
        // TODO : roomOwner가 아닐경우 일반 유저

    }

    // 일반 user의 공개방 입장
    @Transactional
    public void in(Long roomId, String userId){
        // roomId로 db에 있는지 확인 (없으면 Exception 던짐)
        OpenRoomEntity openRoomEntity = checkRoomId(roomId);

        // TODO : 현재 방 인원 check
        //      방 입장하면 +1 , 누군가 퇴장해서 웹소켓 끊기면 상태관리 서버로부터 메시지 받아서 -1
        // 만약 현재 인원이 Max면 Exception
        if(openRoomEntity.getCurUser() == openRoomEntity.getMaxUser()){
            throw new ApplicationException(ErrorCode.MAX_USER,String.format("%d Room is full",roomId));
        }
        // 인원 추가
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
        }else{ // 해당 방 첫 입장 (UserRoomRepository로 테이블에 추가 (일반 user))
            OpenUserRoomEntity newUserRoom = userRoomRepository.save(OpenUserRoomEntity.create(userId, roomId, false));
            openRoomEntity.addUserRoom(newUserRoom);
        }

    }


    @Transactional
    public Long modify(Boolean roomType, Long roomId, String userId,String roomName ,Integer maxUser, RoomChannel roomChannel){
        if(roomType){
            // roomId로 db에 있는지 확인 (없으면 Exception 던짐)
            OpenRoomEntity openRoomEntity = checkRoomId(roomId);

            // 해당 roomId의 roomOwner가 userId인지 확인 (권한 없으면 Exception 던짐)
            checkRoomOwner(roomId, userId);

            // 해당 룸 수정
            openRoomEntity.setRoomName(roomName);
            openRoomEntity.setMaxUser(maxUser);
            openRoomEntity.setChannel(roomChannel);

            // TODO : 내용이 동일할 경우 굳이 넣을 필요 없을듯??


            return OpenRoom.fromEntity(listRepository.saveAndFlush(openRoomEntity)).getRoomId();


        }// TODO 스터디 그룹 수정

        return 0L;

    }


    @Transactional
    public Long create(Boolean roomType, String roomName, String userId, Integer maxUser, RoomChannel channel){
        if(roomType){ // 공개방 생성
            // TODO : 해당 userId가 owner인 방인 userRoom table에 존재하면 생성안됨 (Exception)


            //Long roomId = roomRepository.save(OpenRoomEntity.create(name, channel, maxUser));
            OpenRoomEntity saveRoom = listRepository.save(OpenRoomEntity.create(roomName, channel, maxUser));
            // saveRoom.getRoomId()가 안되면 saveandFlush

            OpenUserRoomEntity newUserRoom = userRoomRepository.save(
                    OpenUserRoomEntity.create(userId, saveRoom.getRoomId(), true));
            saveRoom.addUserRoom(newUserRoom);

            return saveRoom.getRoomId();

        }// TODO : 스터디 그룹 생성
        // TODO : 요청한 userId가 속한 Private 스터디 그룹이 5개일 때 , Private 스터디 그룹 생성 불가능 (유저 권한)


        return 0L;
    }




    // 공개방용 roomId 확인
    private OpenRoomEntity checkRoomId(Long roomId) {
        OpenRoomEntity openRoomEntity = listRepository.findById(roomId).orElseThrow(()->
                new ApplicationException(ErrorCode.ROOM_NOT_FOUND,String.format("%d Room is not founded", roomId)));
        return openRoomEntity;
    }

    // 공개방용 roomOwner 확인
    private OpenUserRoomEntity checkRoomOwner(Long roomId, String userId) {

        Optional<OpenUserRoomEntity> owner = userRoomRepository.findById(new UserRoomId(userId, roomId));

        if(!owner.get().isRoomOwner()){ // owner가 아니면 Exception
            throw new ApplicationException(ErrorCode.INVALID_PERMISSION,String.format("%s User is not %d's RoomOwner", userId, roomId));
        }

        return owner.get();

    }


}
