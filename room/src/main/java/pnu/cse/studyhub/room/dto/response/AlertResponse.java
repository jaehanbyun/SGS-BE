package pnu.cse.studyhub.room.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AlertResponse {
    private Long roomId;
    private String targetId;
    private Integer alert;
}
