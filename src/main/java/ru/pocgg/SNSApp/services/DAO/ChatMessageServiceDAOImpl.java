package ru.pocgg.SNSApp.services.DAO;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import ru.pocgg.SNSApp.model.ChatMessage;
import ru.pocgg.SNSApp.services.DAO.interfaces.ChatMessageServiceDAO;
import ru.pocgg.SNSApp.services.DAO.requests.ChatMessageServiceDAORequests;
import org.hibernate.Session;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Scope("singleton")
public class ChatMessageServiceDAOImpl implements ChatMessageServiceDAO {

    @PersistenceContext
    private EntityManager entityManager;

    private Session getSession() {
        return entityManager.unwrap(Session.class);
    }

    public void forceFlush() {
        getSession().flush();
    }

    public ChatMessage getChatMessageById(int id) {
        try {
            return getSession()
                    .createQuery(ChatMessageServiceDAORequests.GET_BY_ID, ChatMessage.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<ChatMessage> getMessagesByChatId(int chatId) {
        return getSession()
                .createQuery(ChatMessageServiceDAORequests.GET_BY_CHAT_ID, ChatMessage.class)
                .setParameter("chatId", chatId)
                .getResultList();
    }

    public List<ChatMessage> getMessagesBySenderId(int senderId) {
        return getSession()
                .createQuery(ChatMessageServiceDAORequests.GET_BY_SENDER_ID, ChatMessage.class)
                .setParameter("senderId", senderId)
                .getResultList();
    }

    public List<ChatMessage> getMessagesByChatIdAndSenderId(int chatId, int senderId) {
        return getSession()
                .createQuery(ChatMessageServiceDAORequests.GET_BY_CHAT_ID_AND_SENDER_ID, ChatMessage.class)
                .setParameter("chatId", chatId).setParameter("senderId", senderId)
                .getResultList();
    }

    public void addChatMessage(ChatMessage chatMessage) {
        getSession().persist(chatMessage);
    }

    public void updateChatMessage(ChatMessage chatMessage) {
        getSession().merge(chatMessage);
    }

    public void removeChatMessage(ChatMessage chatMessage) {
        getSession().remove(chatMessage);
    }

    public List<ChatMessage> getAllMessages() {
        return getSession()
                .createQuery(ChatMessageServiceDAORequests.GET_ALL, ChatMessage.class)
                .getResultList();
    }
}
