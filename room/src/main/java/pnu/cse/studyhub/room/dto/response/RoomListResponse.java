package pnu.cse.studyhub.room.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import pnu.cse.studyhub.room.model.OpenRoom;
import pnu.cse.studyhub.room.model.RoomChannel;
import pnu.cse.studyhub.room.model.entity.OpenRoomEntity;

import java.sql.Timestamp;

@Getter
@AllArgsConstructor
public class RoomListResponse {
    private Long roomId;

    private String roomName;
    private RoomChannel channel;

    private Integer curUser;
    private Integer maxUser;

    private Timestamp createdAt;

    // 대기실에 방 리스트 보여줄 데이터들만 response에 넣기
    public static RoomListResponse fromRoom(OpenRoom openRoom){
        return new RoomListResponse(
                openRoom.getRoomId(),
                openRoom.getRoomName(),
                openRoom.getChannel(),
                openRoom.getCurUser(),
                openRoom.getMaxUser(),
                openRoom.getCreatedAt()
        );

    }

    public static RoomListResponse fromEntity(OpenRoomEntity entity){
        return new RoomListResponse(
                entity.getRoomId(),
                entity.getRoomName(),
                entity.getChannel(),
                entity.getCurUser(),
                entity.getMaxUser(),
                entity.getCreatedAt()
        );
    }




}
