package pnu.cse.studyhub.chat.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import pnu.cse.studyhub.chat.repository.entity.Chat;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

//public interface ChatRepository extends MongoRepository<Chat,String> {

public interface ChatRepository {
    List<Chat> findByRoomId(Long roomId);
    Chat save(Chat chat);
    List<Chat> deleteAll(List<Chat> chat);

    Chat getLastMessage(Long roomId);


    Page<Chat> findByRoomIdWithPagingAndFiltering(Long roomId, int page, int size);
}
