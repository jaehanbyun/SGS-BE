package pnu.cse.studyhub.chat.kafka;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import pnu.cse.studyhub.chat.repository.entity.Chat;
import pnu.cse.studyhub.chat.service.KafkaConsumer;
import pnu.cse.studyhub.chat.service.KafkaProducer;

import java.time.LocalDate;
import java.time.LocalDateTime;

@DisplayName("카프카 순서 보장 테스트")
@SpringBootTest
@EmbeddedKafka(partitions = 3)
public class KafkaOrderTest {

    @Autowired
    private KafkaProducer kafkaProducer;

    @Autowired
    private KafkaConsumer kafkaConsumer;

    @Test
    public void testMessageOrder() throws InterruptedException {
        String topic = "studyhub";

        for(int i=0; i<10; i++) {
            kafkaProducer.send(topic,new Chat(
                    String.valueOf(i),
                    (long) i%3,
                    "test1",
                    "TEXT",
                    "test message",
                    LocalDateTime.now()));
        }
        Thread.sleep(5000); // consume하기 위한 대기시간
    }
}
