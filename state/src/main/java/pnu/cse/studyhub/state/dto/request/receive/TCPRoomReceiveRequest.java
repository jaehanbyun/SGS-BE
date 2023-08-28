package pnu.cse.studyhub.state.dto.request.receive;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import pnu.cse.studyhub.state.dto.request.send.TCPSignalingSendAlertRequest;
import pnu.cse.studyhub.state.dto.request.send.TCPSignalingSendRequest;


@AllArgsConstructor
@NoArgsConstructor
@Data
@SuperBuilder
@JsonTypeName("room")
public class TCPRoomReceiveRequest extends TCPMessageReceiveRequest {
    @JsonProperty("user_id")
    private String userId;
    @JsonProperty("room_id")
    private Long roomId;
    @JsonProperty("alert_count")
    private Long alertCount;

    public TCPSignalingSendAlertRequest toTCPSignalingSendAlertRequest(String type) {
        return TCPSignalingSendAlertRequest.builder()
                .server("state")
                .type(type)
                .userId(this.userId)
                .roomId(this.roomId)
                .alertCount(this.alertCount)
                .build();
    }
    public TCPSignalingSendRequest toTCPSignalingSendRequest(String type) {
        return TCPSignalingSendRequest.builder()
                .server("state")
                .type(type)
                .userId(this.userId)
                .roomId(this.roomId)
                .build();
    }
}
