package pnu.cse.studyhub.room.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RoomIdResponse {

    private Long roomId;

    public static RoomIdResponse fromRoomId(Long roomId){
        return new RoomIdResponse(roomId);
    }

}
