package pnu.cse.studyhub.room.dto.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import pnu.cse.studyhub.room.model.RoomChannel;

import java.sql.Timestamp;

@Getter
@AllArgsConstructor
public class DetailResponse {
    private Long roomId;

    private String roomName;
    private RoomChannel channel;

    private String roomNotice;
    private String roomOwner;

    private Integer curUser;
    private Integer maxUser;

    private Timestamp createdAt;



}

    /*
        채팅방 이름, 인원/전체인원 , 채널, 공지사항
        방장 이름 ( + 현재 들어와있는 멤버들 이름)

        -> 룸서버에서는 현재 멤버들이 어떤 상태인지 모름

     */