package pnu.cse.studyhub.chat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import pnu.cse.studyhub.chat.repository.entity.Chat;

@Service
@RequiredArgsConstructor // same as autowired
@Slf4j
public class KafkaProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void send(String topic, Chat chat) {
        log.info("topic : " + topic);
        log.info("topic : " + chat.getContent());
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String stringChat = objectMapper.writeValueAsString(chat);
            log.info(stringChat);
            kafkaTemplate.send(topic, stringChat);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
