package pnu.cse.studyhub.room.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pnu.cse.studyhub.room.model.entity.OpenRoomEntity;

public interface ListRepository extends JpaRepository<OpenRoomEntity, Long> {

    // 무한 스크롤 처리
    @Query(value="select o from OpenRoomEntity o where o.roomId <= :lastRoomId order by o.roomId DESC ")
    Page<OpenRoomEntity> findByRoomIdLessThanOrderByRoomIdDesc(Long lastRoomId, PageRequest pageRequest);

}
