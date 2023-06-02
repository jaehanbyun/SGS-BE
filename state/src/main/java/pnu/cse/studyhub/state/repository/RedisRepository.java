package pnu.cse.studyhub.state.repository;

import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pnu.cse.studyhub.state.repository.entity.RealTimeData;

import java.util.List;

@Repository
@EnableRedisRepositories
public interface RedisRepository extends CrudRepository<RealTimeData, String> {
    RealTimeData findBySessionId(String sessionId);

}
