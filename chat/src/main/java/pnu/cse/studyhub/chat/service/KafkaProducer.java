package pnu.cse.studyhub.chat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import pnu.cse.studyhub.chat.repository.entity.Chat;

import java.io.IOException;

@Service
@RequiredArgsConstructor // same as autowired
@Slf4j
public class KafkaProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final S3Uploader s3Uploader;

    public void send(String topic, Chat chat) {
        log.info("producer_topic : " + topic);
        log.info("producer_content : " + chat.getContent());
        String type = chat.getMessageType();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String content = objectMapper.writeValueAsString(chat);
            kafkaTemplate.send(topic, content);
        } catch (JsonProcessingException e) { //json 파싱 실패
            e.printStackTrace();
        } catch (IOException e) { // 파일 변환 실패
            throw new RuntimeException(e);
        }
    }
}
