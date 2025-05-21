package ru.pocgg.SNSApp.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import ru.pocgg.SNSApp.model.ChatMember;
import ru.pocgg.SNSApp.model.ChatMemberId;
import ru.pocgg.SNSApp.model.exceptions.EntityNotFoundException;
import ru.pocgg.SNSApp.services.DAO.interfaces.ChatMemberServiceDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatMemberService extends TemplateService{
    private final ChatMemberServiceDAO chatMemberServiceDAO;
    private final ChatService chatService;
    private final UserService userService;

    public ChatMember createChatMember(int chatId, int memberId, Instant entryDate) {
        ChatMember chatMember = ChatMember.builder()
                .chat(chatService.getChatById(chatId))
                .member(userService.getUserById(memberId))
                .entryDate(entryDate).build();
        chatMemberServiceDAO.addChatMember(chatMember);
        chatMemberServiceDAO.forceFlush();
        logger.info("added chat member to chat id: {}, member id: {}", chatId, memberId);
        return chatMember;
    }

    public ChatMember getChatMemberById(ChatMemberId id) {
        return getChatMemberByIdOrThrowException(id);
    }

    public Boolean isChatMemberExist(ChatMemberId id) {
        return chatMemberServiceDAO.getChatMemberById(id) != null;
    }

    public List<ChatMember> getAllChatMembers() {
        return chatMemberServiceDAO.getAllChatMembers();
    }

    public List<ChatMember> getChatMembersByChatId(int chatId) {
        return chatMemberServiceDAO.getMembersByChatId(chatId);
    }

    public void deleteChatMember(ChatMemberId id) {
        ChatMember chatMember = getChatMemberByIdOrThrowException(id);
        chatMemberServiceDAO.removeChatMember(chatMember);
        logger.info("deleted chat member with id: {} in chat with id: {}", id.getMemberId(), id.getChatId());
    }

    private ChatMember getChatMemberByIdOrThrowException(ChatMemberId id) {
        ChatMember chatMember = chatMemberServiceDAO.getChatMemberById(id);
        if (chatMember == null) {
            throw new EntityNotFoundException("member with id: " + id.getMemberId() + " in chat with id: "
                    + id.getChatId() + " not found");
        }
        return chatMember;
    }
}
