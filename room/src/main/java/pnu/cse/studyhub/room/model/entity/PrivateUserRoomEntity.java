package pnu.cse.studyhub.room.model.entity;

import lombok.Getter;
import lombok.Setter;
import pnu.cse.studyhub.room.model.UserRoomId;

import javax.persistence.*;

import java.sql.Timestamp;
import java.time.Instant;

import static javax.persistence.FetchType.LAZY;

@Entity
@Table(name="private_user_room")
@Getter
@Setter
@IdClass(UserRoomId.class)
public class PrivateUserRoomEntity implements UserRoom{

    @Id
    private String userId;

    @Id
    private Long roomId;

    @MapsId("roomId")
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name="roomId")
    private PrivateRoomEntity privateRoomEntity;

    private boolean roomOwner;
    private boolean isMember;

    private int alert;
    private Boolean kick_out;

    private Timestamp createdAt;
    private Timestamp accessedAt;

    // private Boolean inRoom을 만들어야하나...
    // userRoom에 남아 있는 이유는

    @PrePersist
    void createdAt(){
        this.createdAt = Timestamp.from(Instant.now());
        this.accessedAt = Timestamp.from(Instant.now());
    }



    public static PrivateUserRoomEntity create(String userId, Long roomId,Boolean roomOwner){
        PrivateUserRoomEntity userRoom = new PrivateUserRoomEntity();
        userRoom.setUserId(userId);
        userRoom.setRoomId(roomId);
        userRoom.setRoomOwner(roomOwner);
        userRoom.setAlert(0);
        userRoom.setKick_out(false);
        userRoom.setMember(true);

        return userRoom;
    }

    @Override
    public int addAlert()
    {
        this.alert++;
        if(this.alert == 3) this.kickOut();

        return alert;
    }

    // 퇴장
    @Override
    public void kickOut(){
        this.kick_out = true;
    }

    //위임

    public void delegate(PrivateUserRoomEntity target){
        this.roomOwner = false;
        target.setRoomOwner(true);
    }


}
