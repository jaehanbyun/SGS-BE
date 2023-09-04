package pnu.cse.studyhub.state.dto.request.send;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;


@Builder
public class TCPRoomSendRequest {
    @JsonProperty("server")
    private String server;
    @JsonProperty("user_id")
    private String userId;
    @JsonProperty("room_id")
    private Long roomId;
//    @JsonProperty("type")
//    private String type;
}
