package pnu.cse.studyhub.chat.repository.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("채팅 엔티티 테스트")
class ChatTest {

    // #repository#entity#Chat
    @Nested
    class 채팅_엔티티_동작 {
        @Test
        void 정상_생성자_및_게터_동작_확인() {
            // given
            LocalDateTime createdAt = LocalDateTime.now();
            Chat chat = new Chat("1", 123L, "sender1", "TEXT", "Hello, world!", createdAt);

            // when

            // then -> getter 테스트 + 생성자 테스트
            assertEquals("1", chat.get_id());
            assertEquals(123L, chat.getRoomId());
            assertEquals("sender1", chat.getSenderId());
            assertEquals("TEXT", chat.getMessageType());
            assertEquals("Hello, world!", chat.getContent());
            assertEquals(createdAt, chat.getCreatedAt());
        }

        @Test
        void 정상_세터_동작_확인() {
            // given
            LocalDateTime createdAt = LocalDateTime.now();
            Chat chat = new Chat("1", 123L, "sender1", "text", "Hello, world!", createdAt);

            // when
            chat.set_id("2");
            chat.setRoomId(456L);
            chat.setSenderId("sender2");
            chat.setMessageType("IMG");
            chat.setContent("Image file");
            LocalDateTime newTime = LocalDateTime.now();
            chat.setCreatedAt(newTime);

            // then
            assertEquals("2", chat.get_id());
            assertEquals(456L, chat.getRoomId());
            assertEquals("sender2", chat.getSenderId());
            assertEquals("IMG", chat.getMessageType());
            assertEquals("Image file", chat.getContent());
            assertEquals(newTime, chat.getCreatedAt());
        }
        @Test
        void 정상_등호_동작_확인() {
            // given
            LocalDateTime createdAt = LocalDateTime.now();
            Chat chat1 = new Chat("1", 123L, "sender1", "text", "Hello, world!", createdAt);
            Chat chat2 = new Chat("1", 123L, "sender1", "text", "Hello, world!", createdAt);
            Chat chat3 = new Chat("3", 456L, "sender2", "image", "Image file", LocalDateTime.now());

            // then
            assertEquals(chat1, chat2); // 동일한 값을 가진 두 객체가 같음을 확인
            assertNotEquals(chat1, chat3); // 값이 다른 경우 같지 않음을 확인
        }
    }

}

