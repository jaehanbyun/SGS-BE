package pnu.cse.studyhub.chat.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import pnu.cse.studyhub.chat.repository.entity.Chat;

import java.time.LocalDateTime;

@Data
public class ChatFileRequest {
    private String roomId;
    private String senderId;
    private String messageType;
    private MultipartFile content;

    public Chat toEntity() {
        Chat chat = new Chat();
        chat.setRoomId(this.roomId);
        chat.setContent("");
        chat.setCreatedAt(LocalDateTime.now());
        chat.setSenderId(this.senderId);
        chat.setMessageType(this.messageType);
        return chat;
    }
}
