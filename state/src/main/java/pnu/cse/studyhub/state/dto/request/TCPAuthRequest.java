package pnu.cse.studyhub.state.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonTypeName("auth")
public class TCPAuthRequest extends TCPMessageRequest{
    @JsonProperty("user_id")
    private List<String> userIds;
//    @JsonProperty("room_id")
//    private Long roomId;
//    @JsonProperty("session")
//    private String session;
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
