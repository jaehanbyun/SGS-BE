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
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = TCPAlertRequest.class, name = "ALERT"),
        @JsonSubTypes.Type(value = TCPTypeRequest.class, name = "KICK_OUT"),
        @JsonSubTypes.Type(value = TCPTypeRequest.class, name = "KICK_OUT_BY_ALERT"),
        @JsonSubTypes.Type(value = TCPTypeRequest.class, name = "DELEGATE")
})
public abstract class TCPMessageRequest{
    private String server;
    private String type;
}