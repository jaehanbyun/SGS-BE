<<<<<<<< HEAD:state/src/main/java/pnu/cse/studyhub/state/dto/request/receive/TCPChatReceiveRequest.java
package pnu.cse.studyhub.state.dto.request.receive;
========
package pnu.cse.studyhub.auth.dto;
>>>>>>>> main:auth/src/main/java/pnu/cse/studyhub/auth/dto/TCPUserRequest.java

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
<<<<<<<< HEAD:state/src/main/java/pnu/cse/studyhub/state/dto/request/receive/TCPChatReceiveRequest.java
@Data
@JsonTypeName("chat")
public class TCPChatReceiveRequest extends TCPMessageReceiveRequest {
    @JsonProperty("user_id")
    private String userId;
    @JsonProperty("room_id")
    private Long roomId;
    @JsonProperty("session")
    private String session;
========
@Builder
public class TCPUserRequest {
    @JsonProperty("server")
    private String server;
    @JsonProperty("type")
    private String type;
    @JsonProperty("user_id")
    private String userId;

>>>>>>>> main:auth/src/main/java/pnu/cse/studyhub/auth/dto/TCPUserRequest.java
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
