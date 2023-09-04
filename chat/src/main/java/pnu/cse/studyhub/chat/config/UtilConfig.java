package pnu.cse.studyhub.chat.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pnu.cse.studyhub.chat.util.ByteArrayToStringConverter;
import pnu.cse.studyhub.chat.util.JsonConverter;

@Configuration
public class UtilConfig {

    @Bean
    public ByteArrayToStringConverter byteArrayToStringConverter() {
        return new ByteArrayToStringConverter();
    }
    @Bean
    public JsonConverter jsonConverter() {
        return new JsonConverter(new ObjectMapper());
    }
}
