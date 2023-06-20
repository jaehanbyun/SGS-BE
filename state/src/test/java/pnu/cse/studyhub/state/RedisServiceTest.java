package pnu.cse.studyhub.state;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pnu.cse.studyhub.state.repository.entity.RealTimeData;
import pnu.cse.studyhub.state.service.RedisService;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;


class RedisServiceTest {

    @Mock
    private RedisService redisService;
    public RealTimeData realTimeData = new RealTimeData();
    public RealTimeData realTimeData2 = new RealTimeData();
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
        MockitoAnnotations.openMocks(this);  // Initialize the mock objects

        realTimeData.setUserId(TEST_ID);
        realTimeData.setRoomId(TEST_ROOM);
        realTimeData.setSessionId(TEST_SESSION);
        realTimeData.setStudyTime(TEST_STUDYTIME);

        realTimeData2.setUserId(TEST_ID2);
        realTimeData2.setRoomId(TEST_ROOM2);
        realTimeData2.setSessionId(TEST_SESSION2);
        realTimeData2.setStudyTime(TEST_STUDYTIME2);

    }
//    @AfterEach
//    void tearDown() {
//        // Clear any existing data in Redis after each test
//        redisService.delData(TEST_ID);
//        redisService.delData(TEST_ID2);
//    }

    @Test
    @DisplayName("데이터 저장 테스트")
    void getDataTest() {
        when(redisService.findRealTimeData(TEST_ID)).thenReturn(realTimeData);

        RealTimeData savedData = redisService.findRealTimeData(TEST_ID);
        Assertions.assertEquals(TEST_SESSION, savedData.getSessionId());
        System.out.println(savedData);

    }
    @Test
    @DisplayName("모든 데이터 일괄 조회 테스트")
    void getAllDataListTest(){
        when(redisService.getAllRealTimeData()).thenReturn(Arrays.asList(realTimeData, realTimeData2));


        List<RealTimeData> allData = redisService.getAllRealTimeData();
        List<RealTimeData> testCases = List.of(
                realTimeData,
                realTimeData2
        );
        Assertions.assertEquals(2, allData.size());
        Assertions.assertEquals(allData, testCases);
        System.out.println(testCases + " VS " + allData);

    }

}
