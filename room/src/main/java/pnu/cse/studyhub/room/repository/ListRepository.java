package pnu.cse.studyhub.room.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pnu.cse.studyhub.room.model.RoomChannel;
import pnu.cse.studyhub.room.model.entity.OpenRoomEntity;

@Repository
public interface ListRepository extends JpaRepository<OpenRoomEntity, Long> {

    // 무한 스크롤 처리
    @Query(value = "SELECT o FROM OpenRoomEntity o WHERE o.roomId < :lastRoomId AND (:Keyword IS NULL OR o.roomName LIKE %:Keyword%) " +
            "AND (:Channel IS NULL OR o.channel = :Channel) ORDER BY o.roomId DESC")
    Page<OpenRoomEntity> findByRoomIdLessThanAndRoomNameContainingAndChannelOrderByRoomIdDesc(
            @Param("lastRoomId") Long lastRoomId, @Param("Keyword") String keyword, @Param("Channel") RoomChannel channel, PageRequest pageRequest);


}
