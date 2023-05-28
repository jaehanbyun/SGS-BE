package pnu.cse.studyhub.state.dto.request;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "server",
        visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = TCPChatRequest.class, name = "chat"),
        @JsonSubTypes.Type(value = TCPSignalingRequest.class, name = "signaling")
})
public abstract class TCPMessageRequest{
    private String server;

}
//public class TCPMessageRequest {
//    private TCPBaseRequest message;
//    @JsonTypeInfo(
//            use = JsonTypeInfo.Id.NAME,
//            include = JsonTypeInfo.As.PROPERTY,
//            property = "server")
//    @JsonSubTypes({
//            @JsonSubTypes.Type(value = TCPChatRequest.class, name = "chat"),
//            @JsonSubTypes.Type(value = TCPRoomRequest.class, name = "room")
//    })
//    @Override
//    public String toString(){
//        ObjectMapper mapper = new ObjectMapper();
//        try {
//            return mapper.writeValueAsString(this);
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException(e);
//        }
//    }
//}

