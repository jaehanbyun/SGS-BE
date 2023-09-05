package pnu.cse.studyhub.room.model.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name="\"private_study_group\"")
@Getter
@Setter
@SequenceGenerator(
        name = "SEQ_GENERATOR",
        sequenceName = "MY_SEQ",
        allocationSize = 1
)
public class PrivateRoomEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_GENERATOR")
    private Long roomId;

    @OneToMany(mappedBy="privateRoomEntity", cascade = CascadeType.ALL)
    private List<PrivateUserRoomEntity> privateUserRoomEntities = new ArrayList<>();

    private String roomName;

    private Integer curUser;
    private Integer maxUser;

    @Column(columnDefinition = "TEXT")
    private String roomNotice;

    private Timestamp createdAt;
    private Timestamp updatedAt;

    @Column(columnDefinition = "BINARY(16)")
    private UUID roomCode;

    @PrePersist
    void createdAt(){
        this.createdAt = Timestamp.from(Instant.now());
    }

    @PreUpdate
    void updatedAt(){
        this.updatedAt = Timestamp.from(Instant.now());
    }


    public static PrivateRoomEntity create(String name, Integer maxUser,UUID roomCode){
        PrivateRoomEntity room = new PrivateRoomEntity();
        room.setRoomName(name);
        room.setMaxUser(maxUser);
        room.setCurUser(1);
        room.setRoomCode(roomCode);

        return room;
    }

    // 연관 메소드
    public void addUserRoom(PrivateUserRoomEntity userRoom){
        privateUserRoomEntities.add(userRoom);
        userRoom.setPrivateRoomEntity(this);
    }

    public void addUser(){
        this.setCurUser(this.getCurUser()+1);
    }
    public void minusUser(){
        this.setCurUser(this.getCurUser()-1);
    }

}
