package pnu.cse.studyhub.state.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TCPSignalingResponse {
    @JsonProperty("user_id")
    private String userId;
    @JsonProperty("study_time")
    private String studyTime;
}
