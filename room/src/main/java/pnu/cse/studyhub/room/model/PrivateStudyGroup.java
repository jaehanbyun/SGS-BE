package pnu.cse.studyhub.room.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import pnu.cse.studyhub.room.model.entity.PrivateRoomEntity;
import pnu.cse.studyhub.room.model.entity.PrivateUserRoomEntity;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class PrivateStudyGroup {

    private Long roomId;
    private String roomName;

    private Integer curUser;
    private Integer maxUser;

    private String roomNotice;

    private Timestamp createdAt;
    private Timestamp updatedAt;

    private UUID roomCode;

    public static PrivateStudyGroup fromEntity(PrivateRoomEntity entity){
        return new PrivateStudyGroup(
                entity.getRoomId(),
                entity.getRoomName(),
                entity.getCurUser(),
                entity.getMaxUser(),
                entity.getRoomNotice(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getRoomCode()
        );
    }
}
