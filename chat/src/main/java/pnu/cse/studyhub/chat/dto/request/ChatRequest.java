package pnu.cse.studyhub.chat.dto.request;

import lombok.Data;
import pnu.cse.studyhub.chat.repository.entity.Chat;

import java.sql.Date;
import java.time.LocalDateTime;

@Data
public class ChatRequest {
    private String roomId;
    private String senderId;
    private String messageType;
    private String content;

    public Chat toEntity() {
        Chat chat = new Chat();
        chat.setRoomId(this.roomId);
        chat.setContent(this.content);
        chat.setCreatedAt(LocalDateTime.now());
        chat.setSenderId(this.senderId);
        chat.setMessageType(this.messageType);
        return chat;
    }
}
