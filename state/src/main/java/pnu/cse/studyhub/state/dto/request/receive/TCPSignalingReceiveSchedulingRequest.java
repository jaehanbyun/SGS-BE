package pnu.cse.studyhub.state.dto.request.receive;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pnu.cse.studyhub.state.dto.UserDto;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeName("signaling")
public class TCPSignalingReceiveSchedulingRequest extends TCPMessageReceiveRequest {
    private String server;
    private String type;
    private List<UserDto> users;
}
