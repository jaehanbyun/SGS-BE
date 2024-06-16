package pnu.cse.studyhub.chat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import pnu.cse.studyhub.chat.repository.entity.Chat;

import java.io.IOException;
import java.util.HashMap;

@Service
@RequiredArgsConstructor // same as autowired
@Slf4j
public class KafkaConsumer {

    @Value("${spring.kafka.consumer.group-id}")
    private final static String GROUPID = "pnu-cse";

    @Value("${spring.kafka.topic}")
    private final static String TOPICS = "studyhub";

    private final SimpMessagingTemplate template;


    @KafkaListener(topics = TOPICS, containerGroup = GROUPID)
    public void consumeGroup(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        Chat chat = objectMapper.readValue(record.value(), Chat.class);
        log.info("Consumed Message : " + record.value() + " from Partition : " + record.partition() + " with offset : " + record.offset()+ " in group 0");
        template.convertAndSend("/topic/"+chat.getRoomId(), record.value());
        acknowledgment.acknowledge();
    }

    @KafkaListener(topics = TOPICS+"-1", containerGroup = GROUPID+"-1")
    public void consumeGroup1(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        Chat chat = objectMapper.readValue(record.value(), Chat.class);
        log.info("Consumed Message : " + record.value() + " from Partition : " + record.partition() + " with offset : " + record.offset()+ " in group 1");
        template.convertAndSend("/topic/"+chat.getRoomId(), record.value());
        acknowledgment.acknowledge();
    }
    @KafkaListener(topics = TOPICS+"-2", containerGroup = GROUPID+"-2")
    public void consumeGroup2(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        Chat chat = objectMapper.readValue(record.value(), Chat.class);
        log.info("Consumed Message : " + record.value() + " from Partition : " + record.partition() + " with offset : " + record.offset()+ " in group 2");
        template.convertAndSend("/topic/"+chat.getRoomId(), record.value());
        acknowledgment.acknowledge();
    }
    @KafkaListener(topics = TOPICS+"-3", containerGroup = GROUPID+"-3")
    public void consumeGroup3(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        Chat chat = objectMapper.readValue(record.value(), Chat.class);
        log.info("Consumed Message : " + record.value() + " from Partition : " + record.partition() + " with offset : " + record.offset() + " in group 3");
        template.convertAndSend("/topic/"+chat.getRoomId(), record.value());
        acknowledgment.acknowledge();
    }
}
