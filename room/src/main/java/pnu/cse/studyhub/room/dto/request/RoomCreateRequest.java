package pnu.cse.studyhub.room.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import pnu.cse.studyhub.room.model.RoomChannel;

@Getter
@AllArgsConstructor
public class RoomCreateRequest {

    private boolean roomType;

    private String roomName;
    private Integer maxUser;

    private RoomChannel roomChannel;


}

//  ex)
//      {
//        "roomType" : "true" //true : study group , false : open room
//        "roomName" : "★★ 공개 스터디 방",
//        "roomChannel" : "university",
//        "maxUser" : 10
//        }