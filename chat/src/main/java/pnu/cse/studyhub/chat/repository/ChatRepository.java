package pnu.cse.studyhub.chat.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import pnu.cse.studyhub.chat.repository.entity.Chat;

public interface ChatRepository extends MongoRepository<Chat,String> {

}
