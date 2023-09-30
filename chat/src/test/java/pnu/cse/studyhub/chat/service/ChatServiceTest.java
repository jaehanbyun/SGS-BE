package pnu.cse.studyhub.chat.service;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.multipart.MultipartFile;
import pnu.cse.studyhub.chat.dto.request.ChatFileRequest;
import pnu.cse.studyhub.chat.dto.request.ChatRequest;
import pnu.cse.studyhub.chat.exception.FileConversionException;
import pnu.cse.studyhub.chat.repository.ChatRepository;
import pnu.cse.studyhub.chat.repository.entity.Chat;

import java.time.LocalDateTime;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {ChatService.class})
@ExtendWith(MockitoExtension.class)
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
        void 일반_채팅_저장() {
            // given
            ChatRequest chatRequest = new ChatRequest(1L, "user123", "TEXT", "test123");
            Chat chat = new Chat("1", 1L, "user123", "TEXT", "test123", LocalDateTime.now());

            //when
            when(chatRepository.save(any())).thenReturn(chat); // 반환할 적절한 Chat 객체 설정

            // Assert
            assertEquals(chat, chatService.saveChat(chatRequest));
            verify(chatRepository).save(any());
        }
    }

    // #service#ChatService#saveFileChat
    @Nested
    class 파일채팅_저장 {
        @Test
        void 일반_파일채팅_저장() throws IOException {
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
            when(s3Uploader.multipartFileUpload(any(), any())).thenReturn("filename");

            //then
            assertSame(chat, chatService.saveFileChat(chatFileRequest));
            verify(chatRepository).save(any());
            verify(s3Uploader).multipartFileUpload(any(), any());

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
            when(s3Uploader.multipartFileUpload(any(), any())).thenThrow(new IOException());

            //then
            assertThrows(FileConversionException.class, () -> chatService.saveFileChat(chatFileRequest));
            verify(s3Uploader).multipartFileUpload(any(), any());
            verify(chatRepository, never()).save(any());
        }
    }

}
