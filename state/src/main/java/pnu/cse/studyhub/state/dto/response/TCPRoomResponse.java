package pnu.cse.studyhub.state.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;


@Builder
public class TCPRoomResponse {
    @JsonProperty("server")
    private String server;
    @JsonProperty("user_id")
    private String userId;
    @JsonProperty("room_id")
    private Long roomId;
//    @JsonProperty("type")
//    private String type;
}
