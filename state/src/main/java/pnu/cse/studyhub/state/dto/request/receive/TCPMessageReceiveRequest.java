package pnu.cse.studyhub.state.dto.request.receive;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "server",
        visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = TCPChatReceiveRequest.class, name = "chat"),
        @JsonSubTypes.Type(value = TCPSignalingReceiveRequest.class, name = "signaling"),
        @JsonSubTypes.Type(value = TCPSignalingReceiveSchedulingRequest.class, name = "signaling_scheduling"),
        @JsonSubTypes.Type(value = TCPAuthReceiveRequest.class, name = "auth"),
        @JsonSubTypes.Type(value = TCPRoomReceiveRequest.class, name = "room")
})
@SuperBuilder
public abstract class TCPMessageReceiveRequest {
    private String server;
    private String type;

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

