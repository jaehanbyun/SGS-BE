package pnu.cse.studyhub.signaling.dao.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CandidateRequest {

    private String id;
    private String userId;
    private Candidate candidate;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Candidate {
        // ICE candidate를 포함한 SDP 문자열
        private String candidate;
        // ICE Candidate가 속한 미디어 스트림의 ID
        private String sdpMid;
        // ICE Candidate가 속 미디어 스트림의 인덱스
        private int sdpMLineIndex;
    }

}
