package ru.pocgg.SNSApp.services.DAO.interfaces;

import ru.pocgg.SNSApp.model.Chat;
import ru.pocgg.SNSApp.model.ChatMember;
import ru.pocgg.SNSApp.model.ChatMemberId;

import java.util.List;

public interface ChatMemberServiceDAO {
    ChatMember getChatMemberById(ChatMemberId id);
    List<ChatMember> getAllChatMembers();
    void addChatMember(ChatMember chatMember);
    void updateChatMember(ChatMember chatMember);
    void removeChatMember(ChatMember chatMember);
    void forceFlush();
    List<ChatMember> getMembersByChatId(int chatId);
    List<Chat> getChatsByMemberId(int memberId);
}
