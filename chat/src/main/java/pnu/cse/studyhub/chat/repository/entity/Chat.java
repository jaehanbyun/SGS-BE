package pnu.cse.studyhub.chat.repository.entity;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Document("chat")
public class Chat {
    private String _id;
    private String roomId;
    private String senderId;
    private String messageType;
    private String content;
    private LocalDateTime createdAt;
}
