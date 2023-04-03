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
    // 일단 3번의 경고로
    private Integer alert;
    private Boolean kick_out;

    // 최초 입장 시간
    private Timestamp createdAt;

    @PrePersist
    void createdAt(){
        this.createdAt = Timestamp.from(Instant.now());
        this.accessedAt = Timestamp.from(Instant.now());
    }

    private Timestamp accessedAt;

    public static OpenUserRoomEntity create(String userId,Long roomId,Boolean roomOwner){
        OpenUserRoomEntity userRoom = new OpenUserRoomEntity();
        userRoom.setUserId(userId);
        userRoom.setRoomId(roomId);
        userRoom.setRoomOwner(roomOwner);
        userRoom.setAlert(0);
        userRoom.setKick_out(false);

        return userRoom;
    }

    // 경고
    public void addAlert()
    {
        this.setAlert(this.getAlert()+1);
        if(this.getAlert() == 3) this.kickOut();
    }

    // 퇴장
    public void kickOut(){
        this.setKick_out(true);
    }

    //위임
    public void delegate(OpenUserRoomEntity target){
        this.setRoomOwner(false);
        target.setRoomOwner(true);
    }



}
