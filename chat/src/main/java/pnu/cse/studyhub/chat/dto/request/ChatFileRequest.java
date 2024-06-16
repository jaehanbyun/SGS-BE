package pnu.cse.studyhub.chat.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import pnu.cse.studyhub.chat.repository.entity.Chat;

import java.time.LocalDateTime;

@AllArgsConstructor
@Data
public class ChatFileRequest {
    private Long roomId;
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
