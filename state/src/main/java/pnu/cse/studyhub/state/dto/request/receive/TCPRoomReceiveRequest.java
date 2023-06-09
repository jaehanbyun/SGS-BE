package pnu.cse.studyhub.state.dto.request.receive;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pnu.cse.studyhub.state.dto.request.send.TCPSignalingSendRequest;


@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonTypeName("room")
public class TCPRoomReceiveRequest extends TCPMessageReceiveRequest {
    @JsonProperty("server")
    private String server;
    @JsonProperty("type")
    private String type;
    @JsonProperty("user_id")
    private String userId;
    @JsonProperty("room_id")
    private Long roomId;
    @JsonProperty("alert_count")
    private Long alertCount;

    public TCPSignalingSendRequest toTCPSignalingSendRequest() {
        return TCPSignalingSendRequest.builder()
                .server("state")
                .type(this.type)
                .userId(this.userId)
                .roomId(this.roomId)
                .alertCount(this.alertCount)
                .build();
    }
}
