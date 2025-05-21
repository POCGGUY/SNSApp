package ru.pocgg.SNSApp.services.DAO.interfaces;

import ru.pocgg.SNSApp.model.PrivateMessage;
import org.hibernate.Session;

import java.util.List;

public interface PrivateMessageDAO {
    PrivateMessage getById(int id);
    void add(PrivateMessage message);
    void remove(PrivateMessage message);
    List<PrivateMessage> getByReceiverId(int receiverId);
    void forceFlush();
    List<PrivateMessage> getAllBySenderAndReceiver(int senderId, int receiverId);
}
