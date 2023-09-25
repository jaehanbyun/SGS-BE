package pnu.cse.studyhub.state;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import pnu.cse.studyhub.state.repository.entity.RealTimeData;
import pnu.cse.studyhub.state.service.RedisService;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisServiceTest {

    @Mock
    private RedisTemplate<String, RealTimeData> realTimeDataRedisTemplate;
    @Mock
    private RedisTemplate<String, String> sessionRedisTemplate;
    @InjectMocks
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
        redisService = new RedisService(realTimeDataRedisTemplate, sessionRedisTemplate);
        realTimeData.setUserId(TEST_ID);
        realTimeData.setRoomId(TEST_ROOM);
        realTimeData.setSessionId(TEST_SESSION);
        realTimeData.setStudyTime(TEST_STUDYTIME);

        realTimeData2.setUserId(TEST_ID2);
        realTimeData2.setRoomId(TEST_ROOM2);
        realTimeData2.setSessionId(TEST_SESSION2);
        realTimeData2.setStudyTime(TEST_STUDYTIME2);
    }



    @Test
    @DisplayName("데이터 저장 테스트")
    void getDataTest() {
        HashOperations<String, Object, Object> hashOperations = Mockito.mock(HashOperations.class);
        when(realTimeDataRedisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.get("realTimeData:" + TEST_ID, "data")).thenReturn(realTimeData);

        // Perform the test
        RealTimeData savedData = redisService.findRealTimeData(TEST_ID);
        System.out.println(TEST_SESSION +  savedData.getSessionId());
        Assertions.assertEquals(TEST_SESSION, savedData.getSessionId());

        // Verify the interactions
        verify(realTimeDataRedisTemplate.opsForHash(), times(1)).get("realTimeData:" + TEST_ID, "data");
    }

    @Test
    @DisplayName("데이터 삭제 테스트")
    void saveAndDeleteRealTimeDataAndSessionTest() {
        // Perform deleting operation
        redisService.deleteRealTimeDataAndSession(TEST_ID, TEST_SESSION);
        verify(realTimeDataRedisTemplate).delete("realTimeData:" + TEST_ID);
        verify(sessionRedisTemplate).delete("sessionIdIndex:" + TEST_SESSION);
    }

    @Test
    @DisplayName("세션을 통한 사용자 ID 조회 테스트")
    void findUserIdBySessionIdTest() {
        ValueOperations<String,String> valueOperations = Mockito.mock(ValueOperations.class);
        when(sessionRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(sessionRedisTemplate.opsForValue().get("sessionIdIndex:" + TEST_SESSION)).thenReturn(TEST_ID);

        String userId = redisService.findUserIdBySessionId(TEST_SESSION);
        Assertions.assertEquals(TEST_ID, userId);

        verify(sessionRedisTemplate.opsForValue()).get("sessionIdIndex:" + TEST_SESSION);
    }

    @Test
    @DisplayName("사용자 ID를 통한 데이터 조회 테스트")
    void findRealTimeDataByUserIdTest() {
        HashOperations<String, Object, Object> hashOperations = Mockito.mock(HashOperations.class);
        when(realTimeDataRedisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.get("realTimeData:" + TEST_ID, "data")).thenReturn(realTimeData);

        RealTimeData rtdata = redisService.findRealTimeData(TEST_ID);
        Assertions.assertEquals(realTimeData, rtdata);

        verify(realTimeDataRedisTemplate.opsForHash()).get("realTimeData:" + TEST_ID, "data");
    }
    @Test
    @DisplayName("모든 데이터 일괄 조회 테스트")
    void getAllDataListTest(){
        HashOperations<String, Object, Object> hashOperations = Mockito.mock(HashOperations.class);
        when(realTimeDataRedisTemplate.opsForHash()).thenReturn(hashOperations);

        when(realTimeDataRedisTemplate.keys("realTimeData:*")).thenReturn(Set.of("realTimeData:" + TEST_ID, "realTimeData:" + TEST_ID2));
        when(realTimeDataRedisTemplate.opsForHash().get("realTimeData:" + TEST_ID, "data")).thenReturn(realTimeData);
        when(realTimeDataRedisTemplate.opsForHash().get("realTimeData:" + TEST_ID2, "data")).thenReturn(realTimeData2);

        List<RealTimeData> allData = redisService.getAllRealTimeData();
        System.out.println(allData);

        Assertions.assertEquals(2, allData.size());
        Assertions.assertTrue(allData.containsAll(Arrays.asList(realTimeData, realTimeData2)));
    }

    @Test
    @DisplayName("모든 데이터 일괄 삭제 테스트")
    void deleteAllDataTest(){
        HashOperations<String, Object, Object> hashOperations = Mockito.mock(HashOperations.class);
        when(realTimeDataRedisTemplate.opsForHash()).thenReturn(hashOperations);


        // Mock the keys
        Set<String> keys = new HashSet<>(Arrays.asList("realTimeData:" + TEST_ID, "realTimeData:" + TEST_ID2));

        // Mock the session keys
        Set<String> sessionKeys = new HashSet<>(Arrays.asList("sessionIdIndex:" + TEST_SESSION, "sessionIdIndex:" + TEST_SESSION2));

        // Mock the findRealTimeData() method to return a RealTimeData object with a specific sessionId
        when(realTimeDataRedisTemplate.keys("realTimeData:*")).thenReturn(keys);
        when(realTimeDataRedisTemplate.opsForHash().get("realTimeData:" + TEST_ID, "data")).thenReturn(realTimeData);
        when(realTimeDataRedisTemplate.opsForHash().get("realTimeData:" + TEST_ID2, "data")).thenReturn(realTimeData2);

        // Call the deleteAllData() method
        redisService.deleteAllData();

        // Verify that the delete() method was called with the correct sets of keys
        verify(realTimeDataRedisTemplate).delete(keys);
        verify(sessionRedisTemplate).delete(sessionKeys);
    }

}
