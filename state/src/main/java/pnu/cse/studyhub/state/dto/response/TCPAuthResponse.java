package pnu.cse.studyhub.state.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import pnu.cse.studyhub.state.dto.UserStudyTime;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TCPAuthResponse {
    @JsonProperty("user_id")
    private String userId;
    @JsonProperty("study_time")
    private String studyTime;
}
