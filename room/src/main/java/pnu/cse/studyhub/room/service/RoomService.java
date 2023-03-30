package pnu.cse.studyhub.room.service;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pnu.cse.studyhub.room.dto.response.RoomListResponse;
import pnu.cse.studyhub.room.model.OpenRoom;
import pnu.cse.studyhub.room.model.RoomChannel;
import pnu.cse.studyhub.room.model.entity.OpenRoomEntity;
import pnu.cse.studyhub.room.repository.ListRepository;
import pnu.cse.studyhub.room.repository.RoomRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final ListRepository listRepository;

    @Transactional
    public Long create(Boolean roomType, String name, String userId, Integer maxUser, RoomChannel channel){
        // TODO : user 권한 check

        // TODO : 요청한 userId가 속한 Private 스터디 그룹이 5개일 때 , Private 스터디 그룹 생성 불가능


        // roomType에 따라 repository 분리 후 save (request의 channel 유무로 나누는 것도 가능할듯..??)
        if(roomType){
            Long roomId = roomRepository.save(OpenRoomEntity.create(name, channel, maxUser));
            return roomId;
        }
        return 0L;
    }


    @Transactional
    public List<RoomListResponse> fetchPostPagesBy(Long lastRoomId, int size){

        //TODO : 방 하나도 없을시 Exception 던지기

        PageRequest pageRequest = PageRequest.of(0,size);
        List<OpenRoomEntity> entityList = listRepository.findByRoomIdLessThanOrderByRoomIdDesc(lastRoomId, pageRequest).getContent();

        List<RoomListResponse> responseList = new ArrayList<>();
        for(OpenRoomEntity roomEntity: entityList){
            responseList.add(RoomListResponse.fromEntity(roomEntity));
        }

        return responseList;

    }


}
