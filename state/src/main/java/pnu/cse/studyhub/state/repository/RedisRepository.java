package pnu.cse.studyhub.state.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pnu.cse.studyhub.state.repository.entity.RealTimeData;

@Repository
public interface RedisRepository extends CrudRepository<RealTimeData, String> {
}
