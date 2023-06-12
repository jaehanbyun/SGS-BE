package pnu.cse.studyhub.state;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import pnu.cse.studyhub.state.repository.entity.RealTimeData;
import pnu.cse.studyhub.state.service.RedisService;

import java.time.Duration;
import java.util.List;

@SpringBootTest
@Slf4j
class RedisServiceTest {

    @Autowired
    private RedisService redisService;

    private static final String TEST_ID = "testId1";
    private static final Long TEST_ROOM = 1L;
    private static final String TEST_SESSION = "testSession";
    private static final String TEST_STUDYTIME = "100100";
    private static final String TEST_ID2 = "testId2";
    private static final Long TEST_ROOM2 = 2L;
    private static final String TEST_SESSION2 = "testSession2";
    private static final String TEST_STUDYTIME2 = "100102";

    @BeforeEach
    void setUp() {
        RealTimeData realTimeData = new RealTimeData();
        realTimeData.setUserId(TEST_ID);
        realTimeData.setRoomId(TEST_ROOM);
        realTimeData.setSessionId(TEST_SESSION);
        realTimeData.setStudyTime(TEST_STUDYTIME);

        RealTimeData realTimeData2 = new RealTimeData();
        realTimeData2.setUserId(TEST_ID2);
        realTimeData2.setRoomId(TEST_ROOM2);
        realTimeData2.setSessionId(TEST_SESSION2);
        realTimeData2.setStudyTime(TEST_STUDYTIME2);

        // Clear any existing data in Redis before each test
        redisService.saveRealTimeData(realTimeData);
        redisService.saveRealTimeData(realTimeData2);
    }
//    @AfterEach
//    void tearDown() {
//        // Clear any existing data in Redis after each test
//        redisService.delData(TEST_ID);
//        redisService.delData(TEST_ID2);
//    }

    @Test
    void getDataTest() {
        // Call the setValues method
        RealTimeData savedData = redisService.findRealTimeData(TEST_ID);
        Assertions.assertEquals(TEST_SESSION, savedData.getSessionId());
        System.out.println(savedData);
    }
    @Test
    void getDataListTest(){
        RealTimeData savedData = redisService.findRealTimeData(TEST_ID);
        System.out.println(savedData);
        Assertions.assertEquals(TEST_SESSION, savedData.getSessionId());

    }

}
