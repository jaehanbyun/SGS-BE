package pnu.cse.studyhub.chat.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import pnu.cse.studyhub.chat.ChatApplication;
import pnu.cse.studyhub.chat.dto.request.ChatFileRequest;
import pnu.cse.studyhub.chat.dto.request.ChatRequest;
import pnu.cse.studyhub.chat.repository.entity.Chat;
import pnu.cse.studyhub.chat.service.ChatService;
import pnu.cse.studyhub.chat.service.KafkaProducer;
import pnu.cse.studyhub.chat.util.JsonConverter;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    classes = ChatApplication.class)
@AutoConfigureMockMvc
@TestPropertySource("classpath:application-test.yml")
@DisplayName("HTTP 통신 API 통합 테스트")
public class HttpChatControllerIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JsonConverter jsonConverter;

    // #controller#ChatController#sendMessage
    @Nested
    class 채팅_전송{
        @Test
        void 정상_채팅_전송() throws Exception{
            //given
            ChatRequest chatRequest = new ChatRequest(1L, "sender1", "TEXT", "chat test");
            String requestJson = jsonConverter.convertToJson(chatRequest);

            //when
            MvcResult result = mvc.perform(MockMvcRequestBuilders.post("/chat/send")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
            //then
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andReturn();

            String content = result.getResponse().getContentAsString();
            System.out.println("result : " + content);
        }
    }

    // #controller#ChatController#sendFileMessage
    @Nested
    class 파일_채팅_전송{
        @Test
        void 정상_파일_채팅_전송() throws Exception{
            //given
            MockMultipartFile file = new MockMultipartFile(
                    "content",
                    "test.txt",
                    "text/plain",
                    "Hello, World!".getBytes()
            );
            ChatFileRequest chatFileRequest = new ChatFileRequest(1L, "sender1", "TEXT", file);
            MvcResult result = mvc.perform(MockMvcRequestBuilders.multipart("/chat/send")
                            .file((MockMultipartFile) chatFileRequest.getContent())  // Passing the MultipartFile from ChatFileRequest
                            .param("roomId", chatFileRequest.getRoomId().toString())  // Using the parameters from ChatFileRequest
                            .param("senderId", chatFileRequest.getSenderId())
                            .param("messageType", chatFileRequest.getMessageType()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andReturn();

            String content = result.getResponse().getContentAsString();
            System.out.println("result : " + content);
        }
    }

    @Nested
    class 특정_방의_과거_채팅_조회{
        @Test
        void 정상_과거_채팅_조회() throws Exception{
            Long roomId = 1L;

            // Perform the test
            MvcResult result = mvc.perform(MockMvcRequestBuilders.get("/chat/history/{roomId}", roomId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andReturn();

            String content = result.getResponse().getContentAsString();
            System.out.println("result : " + content);
        }
    }
    @Nested
    class 특정_방의_과거_채팅_페이지네이션{
        @Test
        void 정상_채팅_페이지네이션() throws Exception{
            Long roomId = 1L;
            int page = 1;
            int size = 10;

            MvcResult result = mvc.perform(MockMvcRequestBuilders.get("/chat/history")
                            .param("roomId", String.valueOf(roomId))
                            .param("page", String.valueOf(page))
                            .param("size", String.valueOf(size)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andReturn();

            String content = result.getResponse().getContentAsString();
            System.out.println("result : " + content);

        }
    }

}