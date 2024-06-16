package pnu.cse.studyhub.room.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pnu.cse.studyhub.room.model.UserRoomId;
import pnu.cse.studyhub.room.model.entity.OpenUserRoomEntity;
import pnu.cse.studyhub.room.model.entity.PrivateRoomEntity;
import pnu.cse.studyhub.room.model.entity.PrivateUserRoomEntity;

import java.util.List;
import java.util.UUID;

public interface PrivateUserRoomRepository extends JpaRepository<PrivateUserRoomEntity, UserRoomId> {
    // userId만 받아서 사용

    @Query("select o from PrivateUserRoomEntity o where o.userId = :userId And o.isMember = true")
    List<PrivateUserRoomEntity> findIsMemberByUserId(@Param("userId") String userId);

    @Query("select o from PrivateUserRoomEntity o where o.roomId = :roomId And o.roomOwner = true")
    PrivateUserRoomEntity findRoomOwnerByRoomId(@Param("roomId") Long roomId);


}
