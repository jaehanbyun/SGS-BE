package pnu.cse.studyhub.chat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;

import pnu.cse.studyhub.chat.dto.request.ChatFileRequest;
import pnu.cse.studyhub.chat.service.KafkaProducer;
import pnu.cse.studyhub.chat.dto.request.ChatRequest;
import pnu.cse.studyhub.chat.dto.response.FailedResponse;
import pnu.cse.studyhub.chat.dto.response.SuccessResponse;
import pnu.cse.studyhub.chat.repository.entity.Chat;
import pnu.cse.studyhub.chat.service.ChatService;

import java.util.List;

@Tag(name = "chat", description = "채팅 API")
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
@Slf4j
public class HttpChatController {
    private final KafkaProducer kafkaProducer;
    private final ChatService chatService;

    @Value("${spring.kafka.topic}")
    private String TOPIC;

    @Operation(summary = "채팅 보내기", description = "각 스터디 방에 채팅 보내기")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content( schema = @Schema(implementation = SuccessResponse.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST", content = @Content(schema = @Schema(implementation = FailedResponse.class))),
            @ApiResponse(responseCode = "404", description = "NOT FOUND", content = @Content(schema = @Schema(implementation = FailedResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR", content = @Content(schema = @Schema(implementation = FailedResponse.class))),
    })
    @Parameters({
            @Parameter(name = "roomId", description = "그룹 채팅방 ID", example = "abcd"),
            @Parameter(name = "senderId", description = "채팅 송신자", example = "aaaa"),
            @Parameter(name = "messageType", description = "채팅 타입", example = "TEXT"),
            @Parameter(name = "content", description = "채팅 내용", example = "chat test"),
    })
    @PostMapping(value = "/send", consumes = "application/json")
    public ResponseEntity<SuccessResponse> sendMessage(@RequestHeader("Authorization") String authorization, @RequestBody ChatRequest chat) {
        String accessToken = authorization.replace("Bearer ","");
        Chat savedChat = chatService.saveChat(accessToken, chat );
        try {
            kafkaProducer.send(TOPIC,savedChat);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        SuccessResponse response = new SuccessResponse("SUCCESS", "send chat successfully", savedChat);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @PostMapping(value = "/send", consumes = "multipart/form-data")
    public ResponseEntity<SuccessResponse> sendFileMessage(ChatFileRequest chat) {
        Chat savedChat = chatService.saveFileChat(chat);
        try {
            kafkaProducer.send(TOPIC,savedChat);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        SuccessResponse response = new SuccessResponse("SUCCESS", "send chat successfully", savedChat);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/history/{roomId}")
    public ResponseEntity<SuccessResponse> getChatListInRoom(@PathVariable("roomId") Long roomId) {
        List<Chat> chatList= chatService.getChatsInRoom(roomId);
        SuccessResponse response = new SuccessResponse("SUCCESS", "get chats in room successfully", chatList);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    @GetMapping("/history")
    public ResponseEntity<SuccessResponse> getChatListInRoomWithPaging(
            @RequestParam("roomId") Long roomId,
            @RequestParam("page") int page,
            @RequestParam("size") int size)
    {
        List<Chat> chatList= chatService.getChatsInRoomWithPaging(roomId,page,size);
        SuccessResponse response = new SuccessResponse("SUCCESS", "get chats in room with paging successfully", chatList);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
