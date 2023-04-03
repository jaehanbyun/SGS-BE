package pnu.cse.studyhub.chat.repository;

import com.mongodb.client.result.DeleteResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import pnu.cse.studyhub.chat.exception.ChatNotFoundException;
import pnu.cse.studyhub.chat.repository.entity.Chat;

import java.util.List;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Repository
public class ChatRepositoryImpl implements ChatRepository{

    private final MongoTemplate mongoTemplate;

    public ChatRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }


    @Override
    public List<Chat> findByRoomId(String roomId) {
        // 최신 메시지부터 순서대로
        Query query = Query.query(where("roomId").is(roomId)).with(Sort.by(Sort.Direction.DESC, "createdAt"));
        // 메시지 고유 ID는 보내지 않음
//        query.fields().exclude("_id");
        List<Chat> chatList = mongoTemplate.find(query, Chat.class);

        return chatList;
    }

    @Override
    public Chat save(Chat chat) {
        return mongoTemplate.save(chat);
    }

    @Override
    public List<Chat> deleteAll(List<Chat> chatList) {
        int chatResult = 0;
        for (Chat chat: chatList ) {
            mongoTemplate.remove(chat);
            chatResult++;
        }
        if (chatResult != chatList.size()) {
            throw new RuntimeException();
        }
        return chatList;
    }

    @Override
    public Chat getLastMessage(String roomId) {
        Query query = Query.query(where("roomId").is(roomId)).with(Sort.by(Sort.Direction.DESC,"createdAt"));
//        query.fields().exclude("_id");

        Chat lastChat = mongoTemplate.findOne(query,Chat.class);
        return lastChat;
    }

    @Override
    public Page<Chat> findByRoomIdWithPagingAndFiltering(String roomId, int page, int size) {
        Pageable pageable = PageRequest.of(page,size,Sort.by("createdAt").descending());

        Query query = new Query()
                .with(pageable)
                .skip(pageable.getPageSize() * (pageable.getPageNumber() - 1)) // 생략할 document 수, //페이지는 0부터 시작하지 않고 1부터 시작하므로 - 1
                .limit(pageable.getPageSize()); // find한 document 중 limit 갯수 만큼 출력
        query.addCriteria(where("roomId").is(roomId));

        List<Chat> chatList = mongoTemplate.find(query, Chat.class);
        Page<Chat> chatPage = PageableExecutionUtils.getPage(
                chatList,
                pageable,
                // query.skip(-1).limit(-1)의 이유는 현재 쿼리가 페이징 하려고 하는 offset 까지만 보기에 이를 맨 처음부터 끝까지로 set 해줘 정확한 도큐먼트 개수를 구한다.
                () -> mongoTemplate.count(query.skip(-1).limit(-1),Chat.class)
        );
        return chatPage;
    }
}
