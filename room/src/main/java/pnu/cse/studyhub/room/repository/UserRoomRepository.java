package pnu.cse.studyhub.room.repository;

import org.springframework.data.domain.Pageable;
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

    @Query("SELECT o FROM OpenUserRoomEntity o WHERE o.roomId = :roomId And o.leftAt IS NULL ORDER BY o.accessedAt ASC")
    List<OpenUserRoomEntity> findFastestAccessedRooms(@Param("roomId") Long roomId, Pageable pageable);

}

//    String roomOwnerId= userRoomRepository.findRoomOwnerByRoomId(roomId).getUserId();
