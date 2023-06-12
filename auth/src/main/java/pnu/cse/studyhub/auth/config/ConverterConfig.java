package pnu.cse.studyhub.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pnu.cse.studyhub.auth.util.ByteArrayToStringConverter;
import pnu.cse.studyhub.auth.util.JsonConverter;

@Configuration
public class ConverterConfig {
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
    @Bean
    public ByteArrayToStringConverter byteArrayToStringConverter() {
        return new ByteArrayToStringConverter();
    }
    @Bean
    public JsonConverter jsonConverter() {
        return new JsonConverter(new ObjectMapper());
    }
}