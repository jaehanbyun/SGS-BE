package pnu.cse.studyhub.state.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserStudyTime {
    @JsonProperty("user_id")
    private String userId;
    @JsonProperty("study_time")
    private String studyTime;
}
