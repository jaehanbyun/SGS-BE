package pnu.cse.studyhub.state.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pnu.cse.studyhub.state.util.ByteArrayToStringConverter;
import pnu.cse.studyhub.state.util.JsonConverter;

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
