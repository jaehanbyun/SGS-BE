package pnu.cse.studyhub.signaling.dao.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TimerRequest{
    private String id;
    private String userId;
    private boolean timerState;
    private LocalTime time;
}
