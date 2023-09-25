package pnu.cse.studyhub.chat.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pnu.cse.studyhub.chat.dto.request.ChatRequest;
import pnu.cse.studyhub.chat.repository.ChatRepository;
import pnu.cse.studyhub.chat.repository.entity.Chat;

import java.time.LocalDateTime;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class ChatServiceTest {

    private ChatService chatService;

    @Mock
    private ChatRepository chatRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        chatService = new ChatService(chatRepository, null, jwtTokenProvider);
    }

    @Test
    public void 채팅_저장() {
        // given
        String jwtToken = "example_token";
        ChatRequest mockChatRequest = new ChatRequest( 1L, "user123","TEXT", "test123");
        Chat mockChat = new Chat("1", 1L, "user123", "TEXT", "test123", LocalDateTime.now());

        //when
        when(jwtTokenProvider.getUserInfo(jwtToken)).thenReturn("user123");
        when(chatRepository.save(any())).thenReturn(mockChat); // 반환할 적절한 Chat 객체 설정

        Chat savedChat = chatService.saveChat(jwtToken, mockChatRequest);

        // Assert
        assertNotNull(savedChat);
        assertEquals("user123", savedChat.getSenderId());
        assertEquals(mockChat, savedChat);
    }
}
