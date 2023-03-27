package pnu.cse.storyhub.chat.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageDto {
    private String roomId;
    private String senderId;
    private String messageType;
    private String content;
    private LocalDateTime timeStamp;
}
