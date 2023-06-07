package pnu.cse.studyhub.room.dto.request.tcp;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TCPAlertRequest {

    @JsonProperty("server")
    private String server;
    @JsonProperty("type")
    private String type;
    @JsonProperty("user_id")
    private String userId;
    @JsonProperty("alert_count")
    private int alertCount;

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
