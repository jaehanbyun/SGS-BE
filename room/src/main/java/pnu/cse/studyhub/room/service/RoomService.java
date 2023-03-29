package pnu.cse.studyhub.room.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pnu.cse.studyhub.room.model.RoomChannel;
import pnu.cse.studyhub.room.model.entity.OpenRoomEntity;
import pnu.cse.studyhub.room.repository.RoomRepository;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;

    @Transactional
    public Long create(Boolean roomType, String name, String userId, Integer maxUser, RoomChannel channel){
        // TODO : user 권한 check

        // roomType에 따라 repository 분리 후 save (request의 channel 유무로 나누는 것도 가능할듯..??)
        if(roomType){
            Long roomId = roomRepository.save(OpenRoomEntity.create(name, channel, maxUser));
            return roomId;
        }
        return 0L;
    }



}
