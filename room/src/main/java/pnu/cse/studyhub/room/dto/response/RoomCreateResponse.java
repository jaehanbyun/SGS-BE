package pnu.cse.studyhub.room.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RoomCreateResponse {

    private Long roomId;

    public static RoomCreateResponse fromRoomId(Long roomId){
        return new RoomCreateResponse(roomId);
    }

}
