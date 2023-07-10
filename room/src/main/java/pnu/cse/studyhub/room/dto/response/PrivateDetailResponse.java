package pnu.cse.studyhub.room.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import pnu.cse.studyhub.room.model.RoomChannel;

import java.sql.Timestamp;

@Getter
@AllArgsConstructor
public class PrivateDetailResponse implements DetailResponse{

    private Long roomId;

    private String roomName;

    private String roomNotice;
    private String roomOwner;

    private Integer curUser;
    private Integer maxUser;

    private Timestamp createdAt;

}