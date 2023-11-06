package pnu.cse.studyhub.chat.config.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import pnu.cse.studyhub.chat.repository.entity.Chat;

import java.util.*;

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

    //파티션 1을 담당할 컨슈머 그룹 생성
    @Bean
    ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactoryGroup1() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactoryGroup1());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);

        return factory;
    }

    @Bean
    public ConsumerFactory<String, String> consumerFactoryGroup1(){
        JsonDeserializer<Chat> deserializer = new JsonDeserializer<>(Chat.class);
        deserializer.setRemoveTypeHeaders(false);
        deserializer.addTrustedPackages("*");
        deserializer.setUseTypeMapperForKey(true);

        return new DefaultKafkaConsumerFactory<>(
                consumerConfigurationsGroup1(), new StringDeserializer(), new StringDeserializer()
        );
    }

    @Bean
    public Map<String, Object> consumerConfigurationsGroup1() {
        Map<String, Object> configurations = new HashMap<>();
        configurations.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        configurations.put(ConsumerConfig.GROUP_ID_CONFIG, groupId+"-1");
        configurations.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, keyDeserializer);
        configurations.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,valueDeserializer);
        // "earliser" - 큐의 가장 앞부터 소비하기 시작, "latest" - 가장 최근, 가장 최근에 추가된 것부터 소비하기 시작
        configurations.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        configurations.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        return configurations;
    }
    //파티션 2을 담당할 컨슈머 그룹 생성
    @Bean
    ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactoryGroup2() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactoryGroup2());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);

        return factory;
    }

    @Bean
    public ConsumerFactory<String, String> consumerFactoryGroup2(){
        JsonDeserializer<Chat> deserializer = new JsonDeserializer<>(Chat.class);
        deserializer.setRemoveTypeHeaders(false);
        deserializer.addTrustedPackages("*");
        deserializer.setUseTypeMapperForKey(true);

        return new DefaultKafkaConsumerFactory<>(
                consumerConfigurationsGroup2(), new StringDeserializer(), new StringDeserializer()
        );
    }

    @Bean
    public Map<String, Object> consumerConfigurationsGroup2() {
        Map<String, Object> configurations = new HashMap<>();
        configurations.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        configurations.put(ConsumerConfig.GROUP_ID_CONFIG, groupId+"-2");
        configurations.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, keyDeserializer);
        configurations.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,valueDeserializer);
        // "earliser" - 큐의 가장 앞부터 소비하기 시작, "latest" - 가장 최근, 가장 최근에 추가된 것부터 소비하기 시작
        configurations.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        configurations.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        return configurations;
    }
    //파티션 3을 담당할 컨슈머 그룹 생성
    @Bean
    ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactoryGroup3() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactoryGroup3());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return factory;
    }

    @Bean
    public ConsumerFactory<String, String> consumerFactoryGroup3(){
        JsonDeserializer<Chat> deserializer = new JsonDeserializer<>(Chat.class);
        deserializer.setRemoveTypeHeaders(false);
        deserializer.addTrustedPackages("*");
        deserializer.setUseTypeMapperForKey(true);

        return new DefaultKafkaConsumerFactory<>(
                consumerConfigurationsGroup3(), new StringDeserializer(), new StringDeserializer()
        );
    }

    @Bean
    public Map<String, Object> consumerConfigurationsGroup3() {
        Map<String, Object> configurations = new HashMap<>();
        configurations.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        configurations.put(ConsumerConfig.GROUP_ID_CONFIG, groupId+"-3");
        configurations.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, keyDeserializer);
        configurations.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,valueDeserializer);
        // "earliser" - 큐의 가장 앞부터 소비하기 시작, "latest" - 가장 최근, 가장 최근에 추가된 것부터 소비하기 시작
        configurations.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        configurations.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        return configurations;
    }
}

