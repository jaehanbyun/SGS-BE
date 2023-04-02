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
import pnu.cse.studyhub.room.model.entity.OpenRoomEntity;
import pnu.cse.studyhub.room.repository.ListRepository;
import pnu.cse.studyhub.room.repository.RoomRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final ListRepository listRepository;


    @Transactional
    public Long modify(Boolean roomType, Long roomId, String userId,String roomName ,Integer maxUser, RoomChannel roomChannel){
        if(roomType){
            // roomId로 db에 있는지 확인 (없으면 Exception 던짐)
            OpenRoomEntity openRoomEntity = listRepository.findById(roomId).orElseThrow(()->
                    new ApplicationException(ErrorCode.ROOM_NOT_FOUND,String.format("%s not founded", roomId)));

            // TODO : 해당 roomId의 roomOwnere가 userId인지 확인 (권한 없으면 Exception 던짐)

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
    public Long delete(Boolean roomType,Long roomId, String userId){
        if(roomType){
            // roomId로 db에 있는지 확인 (없으면 Exception 던짐)
            OpenRoomEntity openRoomEntity = listRepository.findById(roomId).orElseThrow(()->
                    new ApplicationException(ErrorCode.ROOM_NOT_FOUND,String.format("%s not founded", roomId)));

            // TODO : 해당 roomId의 roomOwner가 userId인지 확인 ( 권한 없으면 Exception 던짐)

            // 해당 room 삭제
            listRepository.delete(openRoomEntity);

            return roomId;

        } // TODO : 스터디 그룹 삭제 (roomType : false)

        return 0L;
    }

    @Transactional
    public Long create(Boolean roomType, String name, String userId, Integer maxUser, RoomChannel channel){
        // TODO : 요청한 userId가 속한 Private 스터디 그룹이 5개일 때 , Private 스터디 그룹 생성 불가능 (유저 권한)


        if(roomType){ // 공개방 생성
            //Long roomId = roomRepository.save(OpenRoomEntity.create(name, channel, maxUser));
            return listRepository.save(OpenRoomEntity.create(name, channel, maxUser)).getRoomId();

        }// TODO : 스터디 그룹 생성

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
