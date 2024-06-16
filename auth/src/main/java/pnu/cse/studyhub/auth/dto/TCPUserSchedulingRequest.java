package pnu.cse.studyhub.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import pnu.cse.studyhub.auth.model.User;

import java.util.List;

@Data
public class TCPUserSchedulingRequest {
    @JsonProperty("server")
    private String server;

    @JsonProperty("type")
    private String type;

    @JsonProperty("users")
    private List<UserInfoDto> users;
}
