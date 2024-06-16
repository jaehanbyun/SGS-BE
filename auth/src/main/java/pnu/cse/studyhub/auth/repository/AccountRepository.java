package pnu.cse.studyhub.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pnu.cse.studyhub.auth.model.UserAccount;

import java.util.List;

public interface AccountRepository extends JpaRepository<UserAccount, String> {
    UserAccount findByUserid(String id);
    UserAccount findByEmail(String email);
}
