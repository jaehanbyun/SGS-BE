package pnu.cse.storyhub.chat.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import pnu.cse.storyhub.chat.dto.MessageDto;

@Service
@RequiredArgsConstructor // same as autowired
@Slf4j
public class KafkaProducer {
    private final KafkaTemplate<String, MessageDto> kafkaTemplate;

    public void send(String topic, MessageDto messageDto) {
        log.info("topic : " + topic);
        log.info("topic : " + messageDto.getContent());
        kafkaTemplate.send(topic, messageDto);
    }
}
