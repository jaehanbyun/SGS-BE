package pnu.cse.studyhub.signaling.dao.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TimerRequest{
    private String id;
    private String userId;
    private boolean timerState;
    // TODO : 시간도 보내줘야 하나..?
}
