package pnu.cse.studyhub.room.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserRoomId implements Serializable {
    private String userId;
    private Long roomId;
}


