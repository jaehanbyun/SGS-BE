package pnu.cse.storyhub.chat.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pnu.cse.storyhub.chat.dto.MessageDto;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ChatController {
    private final KafkaTemplate<String, MessageDto> kafkaTemplate;

    @Value("${spring.kafka.topic}")
    private String TOPIC;

    @PostMapping("/send")
    public ResponseEntity<MessageDto> sendMessage(@RequestBody MessageDto messageDto) {
        messageDto.setTimeStamp(LocalDateTime.now());
        log.info("Produce message : " + messageDto.toString());
        try {
            kafkaTemplate.send(TOPIC,messageDto);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(messageDto);
    }

    @MessageMapping("/send")
    @SendTo("/topic/group")
    public MessageDto broadcastMessage(@Payload MessageDto messageDto){
        log.info("Consume message : " + messageDto.toString());
        return messageDto;
    }
}
