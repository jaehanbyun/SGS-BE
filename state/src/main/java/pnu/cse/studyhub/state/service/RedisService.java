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

    private final RedisRepository redisRepository;


    public RealTimeData getData(String userId) {
        RealTimeData realTimeData = redisRepository.findById(userId).orElse(null);
        return realTimeData;
    }
    public RealTimeData setData(RealTimeData realTimeData) {
        RealTimeData savedData = redisRepository.save(realTimeData);
        return savedData;
    }


    public void delData(String userId) {
        redisRepository.deleteById(userId);
    }
    public List<RealTimeData> getRealTimeData(List<String> userIds) {
        Iterable<RealTimeData> realTimeDataList = redisRepository.findAllById(userIds);
        List<RealTimeData> list = convert(realTimeDataList);
        return list;
    }
    public static List<RealTimeData> convert(Iterable<RealTimeData> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false)
                .collect(Collectors.toList());
    }


    public void delValues(String userId, String sessionId) {
        realTimeDataRedisTemplate.delete("realTimeData:" + userId);
        sessionRedisTemplate.delete("sessionIdIndex:" + sessionId);
    }
    public String findUserIdBySessionId(String sessionId) {
        Object userIdObj = sessionRedisTemplate.opsForValue().get("sessionIdIndex:" + sessionId);
        return userIdObj != null ? (String) userIdObj : null;
    }
    public RealTimeData setValue(RealTimeData realTimeData) {
        HashOperations<String, Object, Object> hashOps = realTimeDataRedisTemplate.opsForHash();
        hashOps.put("realTimeData:" + realTimeData.getUserId(), "data", realTimeData);
        sessionRedisTemplate.opsForValue().set("sessionIdIndex:" + realTimeData.getSessionId(), realTimeData.getUserId());

        return realTimeData;
    }
}
