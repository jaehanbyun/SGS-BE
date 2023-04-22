package pnu.cse.studyhub.signaling.dao.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantResponse {
    private String userId;
    private boolean video;
    private boolean audio;
}
