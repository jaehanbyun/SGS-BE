package pnu.cse.studyhub.room.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pnu.cse.studyhub.room.model.UserRoomId;
import pnu.cse.studyhub.room.model.entity.OpenUserRoomEntity;

@Repository
public interface UserRoomRepository extends JpaRepository<OpenUserRoomEntity, UserRoomId>{


}



