package pnu.cse.studyhub.state.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;
import pnu.cse.studyhub.state.repository.RedisRepository;
import pnu.cse.studyhub.state.repository.entity.RealTimeData;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String,RealTimeData> realTimeDataRedisTemplate;
    private final RedisTemplate<String,String> sessionRedisTemplate;
    public RealTimeData findRealTimeData(String userId) {
        HashOperations<String, Object, Object> hashOps = realTimeDataRedisTemplate.opsForHash();
        return (RealTimeData) hashOps.get("realTimeData:" + userId, "data");
    }
    public RealTimeData saveRealTimeData(RealTimeData realTimeData) {
        HashOperations<String, Object, Object> hashOps = realTimeDataRedisTemplate.opsForHash();
        hashOps.put("realTimeData:" + realTimeData.getUserId(), "data", realTimeData);

        return realTimeData;
    }
    public String findUserIdBySessionId(String sessionId) {
        String userId = sessionRedisTemplate.opsForValue().get("sessionIdIndex:" + sessionId);
        return userId;
    }
    public RealTimeData saveRealTimeDataAndSession(RealTimeData realTimeData) {
        HashOperations<String, Object, Object> hashOps = realTimeDataRedisTemplate.opsForHash();
        hashOps.put("realTimeData:" + realTimeData.getUserId(), "data", realTimeData);
        sessionRedisTemplate.opsForValue().set("sessionIdIndex:" + realTimeData.getSessionId(), realTimeData.getUserId());

        return realTimeData;
    }
    public void deleteRealTimeDataAndSession(String userId, String sessionId) {
        realTimeDataRedisTemplate.delete("realTimeData:" + userId);
        sessionRedisTemplate.delete("sessionIdIndex:" + sessionId);
    }
    public void deleteSession(String userId) {
        sessionRedisTemplate.delete("sessionIdIndex:" + findRealTimeData(userId).getSessionId());
    }

    public List<RealTimeData> getAllRealTimeData() {
        //  "realTimeData:*"에 해당하는 모든 키를 가져옴
        Set<String> keys = realTimeDataRedisTemplate.keys("realTimeData:*");
        List<RealTimeData> allData = new ArrayList<>();

        if (keys != null) {
            for(String key : keys) {
                String userId = key.replace("realTimeData:", "");

                // Fetch the data for each user
                RealTimeData realTimeData = findRealTimeData(userId);
                if(realTimeData != null) {
                    allData.add(realTimeData);
                }
            }
        }

        return allData;
    }
    public void deleteAllData() {
        Set<String> keys = realTimeDataRedisTemplate.keys("realTimeData:*");

        if (keys!= null) {
            // realTimeData 삭제
            realTimeDataRedisTemplate.delete(keys);
            Set<String> sessionKeys = keys.stream()
                    .map(key -> findRealTimeData(key.replace("realTimeData:", "")).getSessionId())
                    .map(sessionId->"sessionIdIndex:"+sessionId)
                    .collect(Collectors.toSet());
            // sessionIdIndex 삭제
            sessionRedisTemplate.delete(sessionKeys);

        }
    }
}
