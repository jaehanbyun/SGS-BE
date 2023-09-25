package pnu.cse.studyhub.room.repository;


import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DisplayName("스터디 그룹 관련 Repository 테스트")
@DataJpaTest
public class PrivateGroupRepositoryTest {
    private final OpenRoomRepository openRoomRepository;
    private final PrivateRoomRepository privateRoomRepository;
    private final PrivateUserRoomRepository privateUserRoomRepository;
    private final UserRoomRepository userRoomRepository;

    PrivateGroupRepositoryTest(@Autowired OpenRoomRepository openRoomRepository,
                           @Autowired PrivateRoomRepository privateRoomRepository,
                           @Autowired PrivateUserRoomRepository privateUserRoomRepository,
                           @Autowired UserRoomRepository userRoomRepository){

        this.openRoomRepository = openRoomRepository;
        this.privateRoomRepository = privateRoomRepository;
        this.privateUserRoomRepository = privateUserRoomRepository;
        this.userRoomRepository = userRoomRepository;
    }

    // 각 테스트별로 돌리기전에 셋업하고 싶은게 있으면 여기 정의
//    @BeforeEach
//    public void setUp(){
//        em.createNativeQuery("ALTER TABLE user_tb ALTER COLUMN id RESTART WITH 1").executeUpdate();
//        userJPARepository.save(newUser("ssar"));
//    }

    /*
        테스트 목록
        1. 공개방 생성 테스트
        2. 공개방 수정
        3. 공개방 전체 스크롤 조회
        4. 공개방 키워드 기반 검색 무한 스크롤 조회
        5. 공개방 채널별 조회
        6. 공개방 정보 조회
     */







}
