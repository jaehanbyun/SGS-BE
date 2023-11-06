package pnu.cse.studyhub.chat.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.*;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pnu.cse.studyhub.chat.repository.entity.Chat;

@DisplayName("채팅 레포지토리 테스트")
class ChatRepositoryTest {
    @InjectMocks
    private ChatRepositoryImpl chatRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        chatRepository = new ChatRepositoryImpl(mongoTemplate);
    }

    // #repository#ChatRepository#findByRoomId
    @Nested
    class 채팅방을_통한_채팅_조회{
        @Test
        void 정상_채팅_조회() {
            // given
            LocalDateTime createdAt = LocalDateTime.now();
            Chat chat = new Chat("1", 123L, "sender1", "text", "Hello, world!", createdAt);
            List<Chat> chatList = new ArrayList<>();
            chatList.add(chat);

            // when
            when(mongoTemplate.find(any(Query.class), eq(Chat.class))).thenReturn(chatList);
            List<Chat> actualFindByRoomIdResult = chatRepository.findByRoomId(123L);

            // then
            assertSame(chatList, actualFindByRoomIdResult);
            verify(mongoTemplate).find(any(Query.class), eq(Chat.class));
        }


        @Test
        void 정상_채팅방이_없는_경우_채팅_조회(){
            // given
            ArrayList<Chat> chatList = new ArrayList<>();

            // when
            when(mongoTemplate.find(any(Query.class), eq(Chat.class))).thenReturn(chatList);
            List<Chat> actualFindByRoomIdResult = chatRepository.findByRoomId(1L);

            // then
            assertSame(chatList, actualFindByRoomIdResult);
            assertTrue(actualFindByRoomIdResult.isEmpty());
            verify(mongoTemplate).find(any(Query.class), eq(Chat.class));

        }
    }

    // #repository#ChatRepository#save
    @Nested
    class 채팅_저장{
        @Test
        void 정상_채팅_저장(){
            // given
            LocalDateTime createdAt = LocalDateTime.now();
            Chat chat = new Chat("1", 123L, "sender1", "text", "Hello, world!", createdAt);
            // when
            when(mongoTemplate.save((Chat) any())).thenReturn(chat);

            // then
            assertSame(chat,chatRepository.save(chat));
            verify(mongoTemplate).save((Chat) any());
        }
        @Test
        void 정상_채팅_저장_및_id_자동_생성(){
            // given
            Chat chat = new Chat();
            chat.setRoomId(1L);
            chat.setSenderId("42");
            chat.setMessageType("TEXT");
            chat.setContent("Hello, world!");
            chat.setCreatedAt(LocalDateTime.now());
            // when
            when(mongoTemplate.save((Chat) any())).thenReturn(chat);

            // then
            assertSame(chat,chatRepository.save(chat));
            verify(mongoTemplate).save((Chat) any());
        }
    }

    // #repository#ChatRepository#deleteAll
    @Nested
    class 모든_채팅_삭제{
        @Test
        void 정상_모든_채팅_삭제() {
            // Given
            Chat chat1 = new Chat("1", 123L, "sender1", "text", "Hello, world!", LocalDateTime.now());
            Chat chat2 = new Chat("2", 123L, "sender1", "text", "Hello, world!", LocalDateTime.now());
            List<Chat> chatList = new ArrayList<>();
            chatList.add(chat1);
            chatList.add(chat2);

            // When
            when(mongoTemplate.remove(any(Chat.class))).thenReturn(null);
            List<Chat> result = chatRepository.deleteAll(chatList);

            // Then
            assertTrue(result.isEmpty());
            verify(mongoTemplate, times(2)).remove(any(Chat.class));
        }

        @Test
        void 런타임오류_채팅_비어있을때_모든_채팅_삭제(){
            // Given
            Chat chat1 = new Chat("1", 123L, "sender1", "text", "Hello, world!", LocalDateTime.now());
            Chat chat2 = new Chat("2", 123L, "sender1", "text", "Hello, world!", LocalDateTime.now());
            List<Chat> chatList = new ArrayList<>();
            chatList.add(chat1);
            chatList.add(chat2);

            doThrow(new RuntimeException()).when(mongoTemplate).remove(any(Chat.class));

            // Then
            assertThrows(RuntimeException.class, () -> chatRepository.deleteAll(chatList));
            verify(mongoTemplate).remove(any(Chat.class));
        }
    }

    // #repository#ChatRepository#getLastMessage
    @Nested
    class 최신_메시지_조회{
        @Test
        void 정상_채팅방의_최신_메시지_조회(){
            // given
            LocalDateTime createdAt = LocalDateTime.now();
            Chat chat = new Chat("1", 123L, "sender1", "text", "Hello, world!", createdAt);

            // when
            when(mongoTemplate.findOne(any(Query.class), eq(Chat.class))).thenReturn(chat);

            // then
            assertSame(chat,chatRepository.getLastMessage(123L));
            verify(mongoTemplate).findOne(any(Query.class), eq(Chat.class));

        }
    }

    // #repository#ChatRepository#findByRoomIdWithPagingAndFiltering
    @Nested
    class 페이지네이션 {
        @Test
        void 정상_페이지네이션() {
            //given
            Chat chat1 = new Chat("1", 1L, "sender1", "text", "Hello, world!", LocalDateTime.now());
            Chat chat2 = new Chat("2", 1L, "sender1", "text", "Hello, world!", LocalDateTime.now());
            List<Chat> chatList = new ArrayList<>();
            chatList.add(chat1);
            chatList.add(chat2);
            int page = 0;
            int size = 10;
            //when
            when(mongoTemplate.find(any(Query.class), eq(Chat.class))).thenReturn(chatList);

            //then
            assertEquals(chatRepository.findByRoomIdWithPagingAndFiltering(1L, page, size).toList().size(), chatList.size());
            verify(mongoTemplate).find(any(Query.class), eq(Chat.class));
        }

        @Test
        void 정상_빈_페이지네이션() {
            //given
            int page = 5;
            int size = 10;
            //when
            when(mongoTemplate.find(any(Query.class), eq(Chat.class))).thenReturn(new ArrayList<>());

            //then
            assertTrue(chatRepository.findByRoomIdWithPagingAndFiltering(1L, page, size).toList().isEmpty());
            verify(mongoTemplate).find(any(Query.class), eq(Chat.class));
        }
    }

}

