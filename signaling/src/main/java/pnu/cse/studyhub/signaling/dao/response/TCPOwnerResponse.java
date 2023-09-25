package pnu.cse.studyhub.signaling.dao.response;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TCPOwnerResponse {
    @JsonProperty("type")
    private String type;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("room_id")
    private int roomId;

}
