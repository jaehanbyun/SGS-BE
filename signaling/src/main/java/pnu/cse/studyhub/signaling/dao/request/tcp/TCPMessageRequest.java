package pnu.cse.studyhub.signaling.dao.request.tcp;

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
//        @JsonSubTypes.Type(value = TCPStateRequest.class, name = "state"),
})
public abstract class TCPMessageRequest{
    private String server;

}