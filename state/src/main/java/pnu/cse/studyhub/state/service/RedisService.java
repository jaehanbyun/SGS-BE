package pnu.cse.studyhub.state.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import pnu.cse.studyhub.state.repository.RedisRepository;
import pnu.cse.studyhub.state.repository.entity.RealTimeData;

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
        Object userIdObj = sessionRedisTemplate.opsForValue().get("sessionIdIndex:" + sessionId);
        return userIdObj != null ? (String) userIdObj : null;
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
}
