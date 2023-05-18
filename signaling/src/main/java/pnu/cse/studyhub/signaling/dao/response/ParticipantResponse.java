package pnu.cse.studyhub.signaling.dao.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.time.LocalTime;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantResponse {
    private String userId;
    private boolean video;
    private boolean audio;
    private boolean timer;
    private LocalTime studyTime;
    private LocalTime onTime;
}
