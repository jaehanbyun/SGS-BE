package pnu.cse.studyhub.chat.repository.entity;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document("chat")
public class Chat {
    private String _id;
    private Long roomId;
    private String senderId;
    private String messageType;
    private String content;
    private LocalDateTime createdAt;
}
