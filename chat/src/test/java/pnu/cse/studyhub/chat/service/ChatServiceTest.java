package pnu.cse.studyhub.chat.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.multipart.MultipartFile;
import pnu.cse.studyhub.chat.dto.request.ChatFileRequest;
import pnu.cse.studyhub.chat.dto.request.ChatRequest;
import pnu.cse.studyhub.chat.exception.FileConversionException;
import pnu.cse.studyhub.chat.repository.ChatRepository;
import pnu.cse.studyhub.chat.repository.entity.Chat;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("채팅 서비스 테스트")
public class ChatServiceTest {

    @Mock
    protected S3Uploader s3Uploader;

    @InjectMocks
    protected ChatService chatService;

    @Mock
    protected ChatRepository chatRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        chatService = new ChatService(chatRepository, s3Uploader);
    }
    // #service#ChatService#saveChat
    @Nested
    class 채팅_저장 {
        @Test
        void 정상_채팅_저장() {
            // given
            ChatRequest chatRequest = new ChatRequest(1L, "user123", "TEXT", "test123");
            Chat chat = new Chat("1", 1L, "user123", "TEXT", "test123", LocalDateTime.now());

            // when
            when(chatRepository.save(any())).thenReturn(chat); // 반환할 적절한 Chat 객체 설정

            // then
            assertEquals(chat, chatService.saveChat(chatRequest));
            verify(chatRepository).save(any());
        }
    }

    // #service#ChatService#saveFileChat
    @Nested
    class 파일채팅_저장 {
        @Test
        void 정상_파일채팅_저장() throws IOException {
            //given
            MultipartFile multipartFile = new MockMultipartFile(
                    "file",      // 파라미터 이름
                    "flower.png", // 파일 이름
                    "image/png", // 파일 타입
                    "flower".getBytes() // 파일 내용
            );
            ChatFileRequest chatFileRequest = new ChatFileRequest(1L, "user123", "FILE", multipartFile);
            Chat chat = new Chat("1", 1L, "user123", "FILE", "test123", LocalDateTime.now());

            //when
            when(chatRepository.save(any())).thenReturn(chat);
            when(s3Uploader.multipartFileUpload(any(), anyLong())).thenReturn("filename");

            //then
            assertSame(chat, chatService.saveFileChat(chatFileRequest));
            verify(chatRepository).save(any());
            verify(s3Uploader).multipartFileUpload(any(), anyLong());

        }

        @Test
        void 파일변환오류_파일채팅_저장() throws IOException {
            //given
            MultipartFile multipartFile = new MockMultipartFile(
                    "file",      // 파라미터 이름
                    "flower.png", // 파일 이름
                    "image/png", // 파일 타입
                    "!@$^!#%^#$%$5".getBytes() // 파일 내용
            );
            ChatFileRequest chatFileRequest = new ChatFileRequest(1L, "user123", "FILE", multipartFile);
            Chat chat = new Chat("1", 1L, "user123", "FILE", "test123", LocalDateTime.now());

            //when
            when(s3Uploader.multipartFileUpload(any(), anyLong())).thenThrow(new IOException());

            //then
            assertThrows(FileConversionException.class, () -> chatService.saveFileChat(chatFileRequest));
            verify(s3Uploader).multipartFileUpload(any(), anyLong());
            verify(chatRepository, never()).save(any());
        }
    }

    // #service#ChatService#getChatsInRoom
    @Nested
    class 채팅_조회 {

        @Test
        void 정상_채팅없는방_조회() {
            //given
            List<Chat> chatsInRoom = new ArrayList<>();

            //when
            when(chatRepository.findByRoomId(anyLong())).thenReturn(chatsInRoom);

            //then
            assertTrue(chatService.getChatsInRoom(1L).isEmpty());
            verify(chatRepository).findByRoomId(anyLong());
        }

        @Test
        void 정상_채팅1개있는방_조회() {
            //given
            List<Chat> chatList = new ArrayList<>();
            chatList.add(new Chat("_id", 1L, "user123", "TEXT", "test123",
                    LocalDateTime.of(1, 1, 1, 1, 1)));

            //when
            when(chatRepository.findByRoomId(anyLong())).thenReturn(chatList);
            List<Chat> actualChatsInRoom = chatService.getChatsInRoom(1L);

            //then
            assertSame(chatList, actualChatsInRoom);
            assertEquals(1, actualChatsInRoom.size());
            verify(chatRepository).findByRoomId(anyLong());
        }
    }

    // #service#ChatService#getChatsInRoomWithPaging
    @Nested
    class 채팅_페이지네이션 {
        @Test
        void 정상_빈채팅방_페이지네이션() {
            //given

            //when
            when(chatRepository.findByRoomIdWithPagingAndFiltering(anyLong(), anyInt(), anyInt()))
                    .thenReturn(new PageImpl<>(new ArrayList<>()));
            //then
            assertTrue(chatService.getChatsInRoomWithPaging(1L, 1, 3).isEmpty());
            verify(chatRepository).findByRoomIdWithPagingAndFiltering(anyLong(), anyInt(), anyInt());
        }

        @Test
        void 정상_채팅1개_페이지네이션() {
            //given
            Chat chat = new Chat("_id", 1L, "user123", "TEXT", "test123", LocalDateTime.of(1, 1, 1, 1, 1));
            List<Chat> chatList = new ArrayList<>();
            chatList.add(chat);

            //when
            Page<Chat> chatPage = new PageImpl<>(chatList);
            when(chatRepository.findByRoomIdWithPagingAndFiltering(anyLong(), anyInt(), anyInt())).thenReturn(chatPage);

            //then
            assertEquals(1, chatService.getChatsInRoomWithPaging(1L, 1, 3).size());
            verify(chatRepository).findByRoomIdWithPagingAndFiltering(anyLong(), anyInt(), anyInt());
        }

        @Test
        void 정상_각_3개_1개_페이지네이션() {
            //given
            List<Chat> chatList = new ArrayList<>();
            chatList.add(new Chat("_id1", 1L, "user1", "TEXT", "test123",
                    LocalDateTime.of(1, 1, 1, 1, 1)));
            chatList.add(new Chat("_id2", 2L, "user2", "TEXT", "test123",
                    LocalDateTime.of(1, 1, 1, 1, 1)));
            chatList.add(new Chat("_id3", 3L, "user3", "TEXT", "test123",
                    LocalDateTime.of(1, 1, 1, 1, 1)));
            chatList.add(new Chat("_id4", 4L, "user4", "TEXT", "test123",
                    LocalDateTime.of(1, 1, 1, 1, 1)));
            Page<Chat> chatFirstPage = new PageImpl<>(chatList.subList(0, 3));
            Page<Chat> chatSecondPage = new PageImpl<>(chatList.subList(3, 4));

            //when
            when(chatRepository.findByRoomIdWithPagingAndFiltering(anyLong(), eq(1), anyInt())).thenReturn(chatFirstPage);
            when(chatRepository.findByRoomIdWithPagingAndFiltering(anyLong(), eq(2), anyInt())).thenReturn(chatSecondPage);

            //then
            assertEquals(3, chatService.getChatsInRoomWithPaging(1L, 1, 3).size());
            assertEquals(1, chatService.getChatsInRoomWithPaging(1L, 2, 3).size());
            verify(chatRepository).findByRoomIdWithPagingAndFiltering(anyLong(), eq(1), anyInt());
            verify(chatRepository).findByRoomIdWithPagingAndFiltering(anyLong(), eq(2), anyInt());
        }
    }

}
