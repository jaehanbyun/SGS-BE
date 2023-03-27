package pnu.cse.storyhub.chat.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import pnu.cse.storyhub.chat.dto.MessageDto;

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
    public void consume(MessageDto message) throws IOException {
        log.info("Consumed Message : " + message.getContent());
        HashMap<String, String> msg = new HashMap<>();
        msg.put("roomId", message.getRoomId());
        msg.put("content", message.getContent());
        msg.put("senderId", message.getSenderId());
        msg.put("messageType", message.getMessageType());

        ObjectMapper mapper = new ObjectMapper();
        template.convertAndSend("/topic/group", mapper.writeValueAsString(msg));
    }
}
