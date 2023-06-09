package pnu.cse.studyhub.chat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import pnu.cse.studyhub.chat.dto.request.ChatRequest;
import pnu.cse.studyhub.chat.exception.ChatNotFoundException;
import pnu.cse.studyhub.chat.repository.ChatRepository;
import pnu.cse.studyhub.chat.repository.entity.Chat;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;

    public Chat saveChat(ChatRequest chatRequest){
        Chat chatEntity = chatRequest.toEntity();
        Chat savedChat = chatRepository.save(chatEntity);
        return savedChat;
    }
    public List<Chat> getChatsInRoom(String roomId) {
        List<Chat> chatList = chatRepository.findByRoomId(roomId);
        if (chatList == null || chatList.isEmpty() ) throw new ChatNotFoundException("해당 방에 존재하는 채팅이 없음.");
        log.info(String.valueOf(chatList));
        return chatList;
    }
    public List<Chat> getChatsInRoomWithPaging(String roomId, int page, int size) {
        Page<Chat> chatPage = chatRepository.findByRoomIdWithPagingAndFiltering(roomId,page,size);
        List<Chat> chatList = new ArrayList<>();
        if (chatPage == null || chatPage.isEmpty()) {
            throw new ChatNotFoundException("페이지에 해당하는 채팅이 없음.");
        }
        chatList = chatPage.getContent();
        return chatList;
    }
}
