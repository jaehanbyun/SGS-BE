package pnu.cse.studyhub.room.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pnu.cse.studyhub.room.model.UserRoomId;
import pnu.cse.studyhub.room.model.entity.OpenUserRoomEntity;

import java.util.List;

@Repository
public interface UserRoomRepository extends JpaRepository<OpenUserRoomEntity, UserRoomId>{

    @Query("select o from OpenUserRoomEntity o where o.roomId = :roomId And o.roomOwner = true")
    OpenUserRoomEntity findRoomOwnerByRoomId(@Param("roomId") Long roomId);

}

//    String roomOwnerId= userRoomRepository.findRoomOwnerByRoomId(roomId).getUserId();
