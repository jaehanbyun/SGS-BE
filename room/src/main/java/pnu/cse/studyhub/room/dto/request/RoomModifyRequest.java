package pnu.cse.studyhub.room.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import pnu.cse.studyhub.room.model.RoomChannel;


@Getter
@AllArgsConstructor
public class RoomModifyRequest {

    private boolean roomType;
    private Long roomId;

    private String roomName;
    private Integer maxUser;

    private RoomChannel roomChannel;

}

//{
//        "roomName" : "★★ 스터디 그룹",
//        "roomChannel" : "UNIVERSITY",
//        "maxUser" : 3
//        }