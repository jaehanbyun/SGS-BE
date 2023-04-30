package pnu.cse.studyhub.chat.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import pnu.cse.studyhub.chat.repository.entity.Chat;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaListenerConfig {

    @Value("${spring.kafka.consumer.bootstrap-servers}")
    private String bootstrapServer;

    @Value("${spring.kafka.consumer.key-serializer}")
    private String keyDeserializer;

    @Value("${spring.kafka.consumer.value-serializer}")
    private String valueDeserializer;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, String> consumerFactory(){
        JsonDeserializer<Chat> deserializer = new JsonDeserializer<>(Chat.class);
        deserializer.setRemoveTypeHeaders(false);
        deserializer.addTrustedPackages("*");
        deserializer.setUseTypeMapperForKey(true);

        return new DefaultKafkaConsumerFactory<>(
                consumerConfigurations(), new StringDeserializer(), new StringDeserializer()
        );
    }

    @Bean
    public Map<String, Object> consumerConfigurations() {
        Map<String, Object> configurations = new HashMap<>();
        configurations.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        configurations.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        configurations.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, keyDeserializer);
        configurations.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,valueDeserializer);
        // "earliser" - 큐의 가장 앞부터 소비하기 시작, "latest" - 가장 최근, 가장 최근에 추가된 것부터 소비하기 시작
        configurations.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return configurations;
    }
}
