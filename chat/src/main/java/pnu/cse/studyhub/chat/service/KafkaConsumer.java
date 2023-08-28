package pnu.cse.studyhub.chat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import pnu.cse.studyhub.chat.repository.entity.Chat;

import java.io.IOException;
import java.util.HashMap;

@Service
@RequiredArgsConstructor // same as autowired
@Slf4j
public class KafkaConsumer {
    private final static String GROUPID = "${spring.kafka.consumer.group-id}";

    private final static String TOPICS = "${spring.kafka.topic}";

    private final SimpMessagingTemplate template;

    @KafkaListener(topics = TOPICS, groupId = GROUPID)
    public void consume(String stringChat) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        Chat chat = objectMapper.readValue(stringChat, Chat.class);
        log.info("Consumed Message : " + stringChat);
        template.convertAndSend("/topic/"+chat.getRoomId(), stringChat);
    }
}
