package pnu.cse.studyhub.room.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import pnu.cse.studyhub.room.model.entity.OpenRoomEntity;

import javax.persistence.EntityManager;

@Repository
@RequiredArgsConstructor
public class RoomRepository {

    private final EntityManager em;

    public Long save(OpenRoomEntity room){
        em.persist(room);

        return room.getRoomId();
    }




}
