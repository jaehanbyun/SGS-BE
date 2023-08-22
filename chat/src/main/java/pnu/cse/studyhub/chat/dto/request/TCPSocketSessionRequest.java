package pnu.cse.studyhub.chat.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TCPSocketSessionRequest {
    @JsonProperty("server")
    private String server;
    @JsonProperty("user_id")
    private String userId;
    @JsonProperty("room_id")
    private Long roomId; // interceptor에서 가져오는 destination 값이 "/topic/0" 이런 식이기 때문
    @JsonProperty("session")
    private String session;
    @JsonProperty("type")
    private String type;
    @Override
    public String toString(){
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
