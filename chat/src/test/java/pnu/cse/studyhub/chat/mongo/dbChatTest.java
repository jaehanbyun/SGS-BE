package pnu.cse.studyhub.chat.mongo;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.event.annotation.BeforeTestClass;
import pnu.cse.studyhub.chat.dto.request.ChatRequest;
import pnu.cse.studyhub.chat.repository.ChatRepository;
import pnu.cse.studyhub.chat.repository.entity.Chat;

import java.sql.Timestamp;
import java.util.ArrayList;

@DataMongoTest
@Slf4j
public class dbChatTest {

    @Autowired
    private ChatRepository chatRepository;

    private static ChatRequest chatRequest;
    private static Chat savedChat;

    @BeforeAll
    public static void initChat(){
        chatRequest = new ChatRequest();
        chatRequest.setRoomId("test");
        chatRequest.setSenderId("test-user1");
        chatRequest.setMessageType("TEXT");
        chatRequest.setContent("테스트 메시지 입니다.");
    }

//    @AfterAll
//    public static void afterAll(){
//        chatRepository.delete(savedChat);
//    }
    @Test
    @DisplayName("채팅 저장 테스트")
    @Order(1)
    void saveChat() {
        Chat tempChat = chatRequest.toEntity();
        savedChat = chatRepository.save(tempChat);
        log.info("채팅 저장 : " + savedChat);
    }

    @Test
    @DisplayName("Room Id를 통해 채팅 가져오기")
    @Order(2)
    void findChatByRoomId() {
        ArrayList<Chat> dbChat = chatRepository.findByRoomId(savedChat.getRoomId()).orElse(null);

        log.info("채팅 리스트 : " + dbChat);
        Assertions.assertEquals(savedChat.get_id(),dbChat.get(0).get_id());
        chatRepository.deleteAll(dbChat);
    }
}
