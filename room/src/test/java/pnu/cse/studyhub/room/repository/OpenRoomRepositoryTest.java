package pnu.cse.studyhub.room.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import pnu.cse.studyhub.room.model.RoomChannel;
import pnu.cse.studyhub.room.model.entity.OpenRoomEntity;

import javax.persistence.EntityManager;

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
        1. 공개방 생성 테스트
        2. 공개방 수정 테스트
        3. 공개방 전체 스크롤 조회
        4. 공개방 키워드 기반 검색 무한 스크롤 조회
        5. 공개방 채널별 조회
        6. 공개방 유저룸 생성
        7. roomId로 방Owner 찾기 테스트
        8. 현재 방에 있는 유저들 중
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
        OpenRoomEntity savedRoom = openRoomRepository.save(newOpenRoom);

        // then
        assertEquals(newOpenRoom, savedRoom);
        assertNotNull(savedRoom.getRoomId());
        System.out.println("확인 :" + savedRoom.toString());
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
