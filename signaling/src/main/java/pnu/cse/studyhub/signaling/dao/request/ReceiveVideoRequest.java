package pnu.cse.studyhub.signaling.dao.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReceiveVideoRequest {
    private String id;
    private String userId;
    private String sdpOffer;
}
