package pnu.cse.studyhub.room.dto.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import pnu.cse.studyhub.room.model.RoomChannel;

import java.sql.Timestamp;

@Getter
@AllArgsConstructor
public class OpenDetailResponse implements DetailResponse {
    private Long roomId;

    private String roomName;
    private RoomChannel channel;

    private String roomNotice;
    private String roomOwner;

    private Integer curUser;
    private Integer maxUser;

    private Timestamp createdAt;

}
