package pnu.cse.studyhub.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pnu.cse.studyhub.auth.model.User;

import java.util.List;

public interface StudyTimeRepository extends JpaRepository<User, String> {
    List<User> findByUseridAndMonth(String id, String month);
    List<User> findByUseridAndMonthAndDay(String id, String month, String day);
}
