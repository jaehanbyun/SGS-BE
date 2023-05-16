package pnu.cse.studyhub.room.model.entity;

import lombok.Getter;
import lombok.Setter;
import pnu.cse.studyhub.room.model.UserRoomId;

import javax.persistence.*;

import java.sql.Timestamp;
import java.time.Instant;

import static javax.persistence.FetchType.LAZY;

@Entity
@Table(name="open_user_room")
@Getter
@Setter
@IdClass(UserRoomId.class)
public class OpenUserRoomEntity {

    @Id
    private String userId;

    @Id
    private Long roomId;

    @MapsId("roomId")
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name="roomId")
    private OpenRoomEntity openRoomEntity;

    private boolean roomOwner;
    private Integer alert;
    private Integer kick_out;

    // 최초 입장 시간
    private Timestamp createdAt;

    @PrePersist
    void createdAt(){
        this.createdAt = Timestamp.from(Instant.now());
    }

    // TODO : 가장 최근 접근 시간
    //private Timestamp accessedAt;

    public static OpenUserRoomEntity create(String userId,Long roomId,Boolean roomOwner){
        OpenUserRoomEntity userRoom = new OpenUserRoomEntity();
        userRoom.setUserId(userId);
        userRoom.setRoomId(roomId);
        userRoom.setRoomOwner(roomOwner);
        userRoom.setAlert(0);
        userRoom.setKick_out(0);

        return userRoom;
    }


}
