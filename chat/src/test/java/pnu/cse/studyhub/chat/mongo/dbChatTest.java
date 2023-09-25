package pnu.cse.studyhub.chat.mongo;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.core.MongoTemplate;
import pnu.cse.studyhub.chat.dto.request.ChatRequest;
import pnu.cse.studyhub.chat.repository.ChatRepository;
import pnu.cse.studyhub.chat.repository.ChatRepositoryImpl;
import pnu.cse.studyhub.chat.repository.entity.Chat;

import java.util.ArrayList;
import java.util.List;

@DataMongoTest
@Slf4j
public class dbChatTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    private static ChatRequest chatRequest;
    private static Chat savedChat;

    @BeforeAll
    public static void initTest(){
        //chat
        chatRequest = new ChatRequest(999L, "test-user1", "TEXT", "테스트 메시지 입니다.");
    }

//    @AfterAll
//    public static void afterAll(){
//        chatRepository.delete(savedChat);
//    }
    @Test
    @DisplayName("채팅 저장 테스트")
    @Order(1)
    void saveChat() {
        ChatRepository chatRepository = new ChatRepositoryImpl(mongoTemplate);
        Chat tempChat = chatRequest.toEntity();
        savedChat = chatRepository.save(tempChat);
//        log.info("채팅 저장 : " + savedChat);

    }

    @Test
    @DisplayName("Room Id를 통해 채팅 가져오기")
    @Order(2)
    void findChatByRoomId() {
        ChatRepository chatRepository = new ChatRepositoryImpl(mongoTemplate);
        List<Chat> dbChat = chatRepository.findByRoomId(savedChat.getRoomId());
//        log.info("채팅 리스트 : " + dbChat);

        Assertions.assertEquals(savedChat.get_id(),dbChat.get(0).get_id());
//        chatRepository.deleteAll(dbChat);
    }
    @Test
    @DisplayName("Room Id와 Paging을 통해 채팅 가져오기")
    @Order(3)
    void findChatByRoomIdWithPaging(){
        ChatRepository chatRepository = new ChatRepositoryImpl(mongoTemplate);
        Page<Chat> dbChat = chatRepository.findByRoomIdWithPagingAndFiltering(savedChat.getRoomId(), 1,1);
        List<Chat> chatList = dbChat.getContent();
//        log.info("채팅 리스트 : " + dbChat);

        Assertions.assertEquals(savedChat.get_id(),chatList.get(0).get_id());
        chatRepository.deleteAll(chatList);
    }
}
