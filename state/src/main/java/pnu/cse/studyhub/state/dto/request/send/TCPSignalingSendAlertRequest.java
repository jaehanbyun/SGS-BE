package pnu.cse.studyhub.state.dto.request.send;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TCPSignalingSendAlertRequest {
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
}

