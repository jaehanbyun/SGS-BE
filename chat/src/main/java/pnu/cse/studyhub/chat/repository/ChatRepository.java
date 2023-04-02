package pnu.cse.studyhub.chat.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import pnu.cse.studyhub.chat.repository.entity.Chat;

import java.util.ArrayList;
import java.util.Optional;

public interface ChatRepository extends MongoRepository<Chat,String> {
    Optional<ArrayList<Chat>> findByRoomId(String roomId);
}
