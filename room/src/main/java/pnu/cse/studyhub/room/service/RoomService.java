package pnu.cse.studyhub.room.service;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pnu.cse.studyhub.room.dto.response.RoomListResponse;
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

    private final RoomRepository roomRepository;
    private final ListRepository listRepository;
    private final UserRoomRepository userRoomRepository;

    @Transactional
    public void out(String userId, Long roomId){
        // roomId로 db에 있는지 확인 (없으면 Exception 던짐)
        OpenRoomEntity openRoomEntity = listRepository.findById(roomId).orElseThrow(()->
                new ApplicationException(ErrorCode.ROOM_NOT_FOUND,String.format("%d not founded", roomId)));

        // userRoom에 해당방에 user 있는지 체크
        OpenUserRoomEntity openUserRoomEntity = userRoomRepository.findById(new UserRoomId(userId, roomId)).orElseThrow(() ->
                new ApplicationException(ErrorCode.User_NOT_FOUND, String.format("%s not founded", userId)));

        // 해당 userId가 해당 roomId의 roomOwner이면 삭제
        if(openUserRoomEntity.isRoomOwner()){
            listRepository.delete(openRoomEntity);
        }
        // TODO : roomOwner가 아닐경우 일반 유저 (상태 관리 서버로 보내줘야하나..?)
        //         더 고민 해보자

    }


//    @Transactional
//    public Long delete(Boolean roomType,Long roomId, String userId){
//        if(roomType){
//            // roomId로 db에 있는지 확인 (없으면 Exception 던짐)
//            OpenRoomEntity openRoomEntity = listRepository.findById(roomId).orElseThrow(()->
//                    new ApplicationException(ErrorCode.ROOM_NOT_FOUND,String.format("%s not founded", roomId)));
//
//            // TODO : 해당 roomId의 roomOwner가 userId인지 확인 (권한 없으면 Exception 던짐)
////            if(!userRoomRepository.findById(new UserRoomId(userId, roomId)).get().isRoomOwner()){
////                throw new ApplicationException(ErrorCode.INVALID_PERMISSION,String.format("%s User is not %d's RoomOwner",userId,roomId));
////            }
//
//            // 해당 room 삭제
//            listRepository.delete(openRoomEntity);
//
//            return roomId;
//
//        } // TODO : 스터디 그룹 삭제 (roomType : false)
//
//        return 0L;
//    }



    // 일반 user의 공개방 입장
    @Transactional
    public void in(Long roomId, String userId){

        // roomId로 db에 있는지 확인 (없으면 Exception 던짐)
        OpenRoomEntity openRoomEntity = listRepository.findById(roomId).orElseThrow(()->
                new ApplicationException(ErrorCode.ROOM_NOT_FOUND,String.format("%s not founded", roomId)));

        // 해당방에 이전에 들어온적 있는 유저인지 check
        Optional<OpenUserRoomEntity> userRoom = userRoomRepository.findById(new UserRoomId(userId, roomId));

        //TODO : 현재 방 입장 정보에 대해서 상태관리 서버로 보내기 (일단 관리 x)

        if(userRoom.isPresent()){// 이전에 존재한적 있으면 접근시간 바꾸고 상태관리 서버로 보내기
            // 이전에 존재한적있으면 user의 권한 확인 (kickout, alert관련)
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
            OpenRoomEntity openRoomEntity = listRepository.findById(roomId).orElseThrow(()->
                    new ApplicationException(ErrorCode.ROOM_NOT_FOUND,String.format("%s not founded", roomId)));

            // 해당 roomId의 roomOwner가 userId인지 확인 (권한 없으면 Exception 던짐)
            if(!userRoomRepository.findById(new UserRoomId(userId, roomId)).get().isRoomOwner()){
                throw new ApplicationException(ErrorCode.INVALID_PERMISSION,String.format("%s User is not %d's RoomOwner",userId,roomId));
            }

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



}
