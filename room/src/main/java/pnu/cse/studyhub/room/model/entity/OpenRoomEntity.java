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
import java.util.Objects;

@Entity
@Table(name="open_study_room")
@Getter
@Setter
@SequenceGenerator(
        name = "SEQ_GENERATOR",
        sequenceName = "MY_SEQ",
        initialValue = 1,
        allocationSize = 1
)
public class OpenRoomEntity {

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

    public void addUser()
    {
        this.curUser++;
        //this.setCurUser(this.getCurUser()+1);
    }
    public int minusUser(){
        this.curUser--;
        return this.curUser;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OpenRoomEntity that = (OpenRoomEntity) o;
        return Objects.equals(roomId, that.roomId) && Objects.equals(openUserRoomEntities, that.openUserRoomEntities) && Objects.equals(roomName, that.roomName) && channel == that.channel && Objects.equals(curUser, that.curUser) && Objects.equals(maxUser, that.maxUser) && Objects.equals(roomNotice, that.roomNotice) && Objects.equals(createdAt, that.createdAt) && Objects.equals(updatedAt, that.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roomId, openUserRoomEntities, roomName, channel, curUser, maxUser, roomNotice, createdAt, updatedAt);
    }

    @Override
    public String toString() {
        return "OpenRoomEntity{" +
                "roomId=" + roomId +
                ", roomName='" + roomName + '\'' +
                ", channel=" + channel +
                ", curUser=" + curUser +
                ", maxUser=" + maxUser +
                ", roomNotice='" + roomNotice + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
