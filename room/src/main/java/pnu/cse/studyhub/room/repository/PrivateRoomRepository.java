package pnu.cse.studyhub.room.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pnu.cse.studyhub.room.model.entity.OpenUserRoomEntity;
import pnu.cse.studyhub.room.model.entity.PrivateRoomEntity;

import java.util.Optional;
import java.util.UUID;

public interface PrivateRoomRepository  extends JpaRepository<PrivateRoomEntity,Long> {

//    @Query("select o from PrivateRoomEntity o where o.roomCode = :roomCode")
//    Optional<PrivateRoomEntity> findByRoomCode(@Param("roomCode") UUID roomCode);

    Optional<PrivateRoomEntity> findByRoomCode(UUID roomCode);

    @Query("select o from PrivateUserRoomEntity o where o.roomId = :roomId And o.roomOwner = true")
    OpenUserRoomEntity findRoomOwnerByRoomId(@Param("roomId") Long roomId);
}
