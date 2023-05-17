package pnu.cse.studyhub.state.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import pnu.cse.studyhub.state.repository.RedisRepository;
import pnu.cse.studyhub.state.repository.entity.RealTimeData;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate redisTemplate;
    private final RedisRepository redisRepository;

    public void setValues(String id, String session) {
        ValueOperations<String, String> values = redisTemplate.opsForValue();
        values.set(id, session);
    }
    public void setValuesWithTTL(String id, String session, long minutes) {
        ValueOperations<String, String> values = redisTemplate.opsForValue();
        values.set(id, session , Duration.ofMinutes(3));
    }
    public String getValues(String id) {
        ValueOperations<String,String> values = redisTemplate.opsForValue();
        return values.get(id);
    }
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


    public void delValues(String id) {
        redisTemplate.delete(id);
    }
}
