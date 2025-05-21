package ru.pocgg.SNSApp.services.DAO.interfaces;

import ru.pocgg.SNSApp.model.Chat;

import java.util.List;

public interface ChatServiceDAO {
    Chat getChatById(int id);
    void addChat(Chat chat);
    void updateChat(Chat chat);
    void removeChat(Chat chat);
    List<Chat> getAllChats();
    void forceFlush();
}
