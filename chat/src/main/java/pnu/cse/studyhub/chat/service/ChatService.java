package pnu.cse.studyhub.chat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import pnu.cse.studyhub.chat.dto.request.ChatFileRequest;
import pnu.cse.studyhub.chat.dto.request.ChatRequest;
import pnu.cse.studyhub.chat.exception.FileConversionException;
import pnu.cse.studyhub.chat.repository.ChatRepository;
import pnu.cse.studyhub.chat.repository.entity.Chat;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final S3Uploader s3Uploader;
    private final JwtTokenProvider jwtTokenProvider;

    public Chat saveChat(String jwtToken, ChatRequest chatRequest){
        Chat chatEntity = chatRequest.toEntity();
        log.error(chatEntity.toString());
        String senderId = jwtTokenProvider.getUserInfo(jwtToken);
        chatEntity.setSenderId(senderId);
        Chat savedChat = chatRepository.save(chatEntity);
        log.error(savedChat.toString());
        return savedChat;
    }
    public Chat saveFileChat(ChatFileRequest chatFileRequest){
        Chat chatEntity = chatFileRequest.toEntity();
        try {
            String uri = s3Uploader.multipartFileUpload(chatFileRequest.getContent(), chatFileRequest.getRoomId());
            chatEntity.setContent(uri);
        } catch (IOException e) { // 파일 업로드 실패 혹은 변환 실패
            log.warn(e.getMessage());
            throw new FileConversionException("파일 변환에 실패했습니다.");
        }
        Chat savedChat = chatRepository.save(chatEntity);
        return savedChat;
    }
    public List<Chat> getChatsInRoom(String roomId) {
        List<Chat> chatList = chatRepository.findByRoomId(roomId);
        if (chatList == null || chatList.isEmpty()) {
            return Collections.emptyList();
        }
        return chatList;
    }
    public List<Chat> getChatsInRoomWithPaging(String roomId, int page, int size) {
        Page<Chat> chatPage = chatRepository.findByRoomIdWithPagingAndFiltering(roomId,page,size);
        if (chatPage == null || chatPage.isEmpty()) {
            return Collections.emptyList();
        }
        List<Chat> chatList  = chatPage.getContent();
        return chatList;
    }
}
