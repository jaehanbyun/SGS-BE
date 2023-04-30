package pnu.cse.studyhub.room.model.entity;

import com.sun.xml.bind.v2.TODO;
import lombok.Getter;
import lombok.Setter;
import pnu.cse.studyhub.room.model.RoomChannel;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="open_study_room")
@Getter
@Setter
@SequenceGenerator(
        name = "SEQ_GENERATOR",
        sequenceName = "MY_SEQ",
        allocationSize = 1
)
public class OpenRoomEntity {

/*
        TODO : UUID로 roomID를 만들려니 userRoom에서 roomID를 FK로 사용하기 때문에 성능문제
                생길수 있으니 custom generator를 만들 예정
 */

//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long roomId;
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_GENERATOR")
    private Long roomId;

    @OneToMany(mappedBy="openRoomEntity", cascade = CascadeType.ALL)
    private List<OpenUserRoomEntity> openUserRoomEntities = new ArrayList<>();


    private String roomName;

    @Enumerated(EnumType.STRING)
    private RoomChannel channel;

    private Integer curUser;
    private Integer maxUser;

    @Column(columnDefinition = "TEXT")
    private String roomNotice;

    private Timestamp createdAt;
    private Timestamp updatedAt;

    @PrePersist
    void createdAt(){
        this.createdAt = Timestamp.from(Instant.now());
    }

    @PreUpdate
    void updatedAt(){
        this.updatedAt = Timestamp.from(Instant.now());
    }

    // 생성 메소드
    public static OpenRoomEntity create(String name, RoomChannel channel, Integer maxUser){
        OpenRoomEntity room = new OpenRoomEntity();
        room.setRoomName(name);
        room.setChannel(channel);
        room.setCurUser(1);
        room.setMaxUser(maxUser);

        return room;
    }


    // 연관 메소드 : 이후에 입장할때 userRoom 만들고 addUserRoom으로 OpenRoomEntity랑 연결해줄때 사용
    public void addUserRoom(OpenUserRoomEntity userRoom){
        openUserRoomEntities.add(userRoom);
        userRoom.setOpenRoomEntity(this);
    }

    public void addUser(){
        this.setCurUser(this.getCurUser()+1);
    }


}
