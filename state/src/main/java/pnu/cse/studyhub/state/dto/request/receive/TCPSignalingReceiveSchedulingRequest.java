package pnu.cse.studyhub.state.dto.request.receive;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pnu.cse.studyhub.state.dto.UserDto;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeName("signaling_scheduling")
public class TCPSignalingReceiveSchedulingRequest extends TCPMessageReceiveRequest {
    @JsonProperty("users")
    private List<UserDto> users;
}
