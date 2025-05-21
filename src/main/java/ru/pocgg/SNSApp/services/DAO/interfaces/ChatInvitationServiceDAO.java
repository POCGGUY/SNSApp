package ru.pocgg.SNSApp.services.DAO.interfaces;

import ru.pocgg.SNSApp.model.ChatInvitation;
import ru.pocgg.SNSApp.model.ChatInvitationId;

import java.util.List;

public interface ChatInvitationServiceDAO {
    List<ChatInvitation> getAllChatInvitations();
    List<ChatInvitation> getByReceiverId(int receiverId);
    List<ChatInvitation> getBySenderId(int senderId);
    List<ChatInvitation> getByChatId(int chatId);
    void addChatInvitation(ChatInvitation chatInvitation);
    void updateChatInvitation(ChatInvitation chatInvitation);
    void removeChatInvitation(ChatInvitation chatInvitation);
    void removeBySenderId(int senderId);
    void removeByChatId(int chatId);
    void removeByReceiverId(int receiverId);
    void forceFlush();
    ChatInvitation getChatInvitationById(ChatInvitationId id);
}
