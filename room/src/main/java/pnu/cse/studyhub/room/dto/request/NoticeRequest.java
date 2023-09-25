package pnu.cse.studyhub.room.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NoticeRequest {

    private Boolean roomType;
    private long roomId;
    private String roomNotice;


}
