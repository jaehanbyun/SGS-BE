package pnu.cse.studyhub.chat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import pnu.cse.studyhub.chat.repository.entity.Chat;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void send(String topic, Chat chat) {
        log.info("producer_topic : " + topic + ", topic : " + topic+"-"+(chat.getRoomId()%3+1));
        log.info("producer_content : " + chat.getContent());
        String type = chat.getMessageType();
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        try {
            String content = objectMapper.writeValueAsString(chat);
//            kafkaTemplate.send(topic,  content);
            kafkaTemplate.send(topic+"-"+(chat.getRoomId()%3+1), Integer.valueOf((int)(chat.getRoomId()%3)),chat.get_id(), content);
        } catch (JsonProcessingException e) { //json 파싱 실패
            e.printStackTrace();
        }
    }
}
