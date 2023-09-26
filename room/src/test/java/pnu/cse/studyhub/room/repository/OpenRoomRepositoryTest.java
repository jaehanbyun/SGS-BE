package pnu.cse.studyhub.room.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import pnu.cse.studyhub.room.model.RoomChannel;
import pnu.cse.studyhub.room.model.entity.OpenRoomEntity;

import javax.persistence.EntityManager;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("공개방관련 Repository 테스트")
@DataJpaTest
public class OpenRoomRepositoryTest {

    private final OpenRoomRepository openRoomRepository;
    private final PrivateRoomRepository privateRoomRepository;
    private final PrivateUserRoomRepository privateUserRoomRepository;
    private final UserRoomRepository userRoomRepository;
    private final EntityManager em;

    OpenRoomRepositoryTest(@Autowired OpenRoomRepository openRoomRepository,
                           @Autowired PrivateRoomRepository privateRoomRepository,
                           @Autowired PrivateUserRoomRepository privateUserRoomRepository,
                           @Autowired UserRoomRepository userRoomRepository,
                           @Autowired EntityManager em){

        this.openRoomRepository = openRoomRepository;
        this.privateRoomRepository = privateRoomRepository;
        this.privateUserRoomRepository = privateUserRoomRepository;
        this.userRoomRepository = userRoomRepository;
        this.em = em;
    }


    // 각 테스트별로 돌리기전에 셋업하고 싶은게 있으면 여기 정의
    @BeforeEach
    public void setUp(){
        //em.createNativeQuery("ALTER TABLE open_study_room ALTER COLUMN  RESTART WITH 1").executeUpdate();
        em.createNativeQuery("ALTER SEQUENCE my_seq RESTART WITH 1;").executeUpdate();
    }


    /*
        테스트 목록
        1. 공개방 생성 테스트 ㅇ
        2. 공개방 수정 테스트 ㅇ
        3. 공개방 전체 스크롤 조회
        4. 공개방 키워드 기반 검색 무한 스크롤 조회
        5. 공개방 채널별 조회
        6. 공개방 유저룸 생성
        7. roomId로 방Owner 찾기 테스트
        8. 현재 방에 있는 유저들 중 가장 빨리 들어온 유저 찾기
     */


    @DisplayName("1. 공개방 생성 테스트")
    @Test
    public void createOpenRoom(){

        // given
        String roomName = "Create Test OpenRoom";
        RoomChannel channel = RoomChannel.ELEMENTARY_SCHOOL;
        int maxUser = 3;

        // when
        OpenRoomEntity newOpenRoom = OpenRoomEntity.create(roomName, channel, maxUser);
        openRoomRepository.save(newOpenRoom);
        em.flush();

        // then
        Optional<OpenRoomEntity> savedRoom = openRoomRepository.findById(1L);
        assertEquals(newOpenRoom, savedRoom.get());
        assertNotNull(savedRoom.get().getRoomId());
        System.out.println("확인 :" + savedRoom.toString());
    }

    @DisplayName("2. 공개방 수정 테스트")
    @Test
    public void modifyOpenRoom(){

        // given
        String roomName = "Test OpenRoom";
        RoomChannel channel = RoomChannel.ELEMENTARY_SCHOOL;
        int maxUser = 3;
        OpenRoomEntity newRoom = openRoomRepository.save(OpenRoomEntity.create(roomName, channel, maxUser));
        em.flush();

        // when
        Optional<OpenRoomEntity> savedRoom = openRoomRepository.findById(1L);
        String newRoomName = "New Test OpenRoom";
        RoomChannel newChannel = RoomChannel.HIGH_SCHOOL;
        int newMaxUser = 5;

        savedRoom.get().setRoomName(newRoomName);
        savedRoom.get().setChannel(newChannel);
        savedRoom.get().setMaxUser(newMaxUser); // 수정하고 flush 시점이 언제더라?
        em.flush();

        // then
        Optional<OpenRoomEntity> modifiedRoom = openRoomRepository.findById(1L);
        assertEquals(modifiedRoom.get().getRoomName() , newRoomName);
        assertEquals(modifiedRoom.get().getChannel() , newChannel);
        assertEquals(modifiedRoom.get().getMaxUser() , newMaxUser);
        assertNotNull(modifiedRoom.get().getUpdatedAt());
        System.out.println("확인 :" + modifiedRoom);

    }

//    @DisplayName("공개방 수정 테스트")
//    @Test
//    public void save4(){
//
//        // given
//
//        // when
//
//        // then
//    }
}
