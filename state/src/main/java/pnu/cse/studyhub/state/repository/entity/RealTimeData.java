package pnu.cse.studyhub.state.repository.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;
import pnu.cse.studyhub.state.dto.UserDto;

@NoArgsConstructor
@Data
@RedisHash("realTimeData")
public class RealTimeData {
    @Id
    @Indexed
    private String userId;
    private Long roomId;
    private String sessionId;
    private String studyTime;
//    // 현재시각 - 타이머 시작 시간
//    private LocalDateTime studyStartTime;
//    // 이전까지 기록된 총 공부 시간
//    private Duration recordTime;
    public UserDto toUserStudyTime() {
        return UserDto.builder()
                .userId(this.userId)
                .studyTime(this.studyTime)
                .build();
    }
}
