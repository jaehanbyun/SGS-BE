package pnu.cse.studyhub.chat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pnu.cse.studyhub.chat.dto.request.ChatRequest;
import pnu.cse.studyhub.chat.repository.ChatRepository;
import pnu.cse.studyhub.chat.repository.entity.Chat;

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
}
