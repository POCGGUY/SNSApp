package ru.pocgg.SNSApp.services.DAO.interfaces;

import ru.pocgg.SNSApp.model.ChatMessage;

import java.util.List;

public interface ChatMessageServiceDAO {
    ChatMessage getChatMessageById(int id);
    List<ChatMessage> getMessagesByChatId(int chatId);
    List<ChatMessage> getMessagesBySenderId(int senderId);
    List<ChatMessage> getMessagesByChatIdAndSenderId(int chatId, int senderId);
    void addChatMessage(ChatMessage chatMessage);
    void updateChatMessage(ChatMessage chatMessage);
    void removeChatMessage(ChatMessage chatMessage);
    List<ChatMessage> getAllMessages();
    void forceFlush();
}
