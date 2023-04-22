package pnu.cse.studyhub.signaling.dao.request;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JoinRequest {
    private String id;
    private String token;
    private String userId;
    private long roomId;
    private boolean video;
    private boolean audio;
}
