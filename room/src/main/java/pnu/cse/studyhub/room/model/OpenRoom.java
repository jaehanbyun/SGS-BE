package pnu.cse.studyhub.room.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import pnu.cse.studyhub.room.model.entity.OpenRoomEntity;
import pnu.cse.studyhub.room.model.entity.OpenUserRoomEntity;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
public class OpenRoom {

    private Long roomId;

    private String roomName;
    private RoomChannel channel;

    private Integer curUser;
    private Integer maxUser;

    private String roomNotice;

    private Timestamp createdAt;
    private Timestamp updatedAt;

    public static OpenRoom fromEntity(OpenRoomEntity entity){
        return new OpenRoom(
                entity.getRoomId(),
                entity.getRoomName(),
                entity.getChannel(),
                entity.getCurUser(),
                entity.getMaxUser(),
                entity.getRoomNotice(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }


}
