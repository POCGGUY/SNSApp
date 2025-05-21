package ru.pocgg.SNSApp.services.DAO;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import ru.pocgg.SNSApp.model.ChatInvitation;
import ru.pocgg.SNSApp.model.ChatInvitationId;
import ru.pocgg.SNSApp.services.DAO.interfaces.ChatInvitationServiceDAO;
import ru.pocgg.SNSApp.services.DAO.requests.ChatInvitationServiceDAORequests;

import java.util.List;

@Repository
@Scope("singleton")
public class ChatInvitationServiceDAOImpl implements ChatInvitationServiceDAO {

    @PersistenceContext
    private EntityManager entityManager;

    private Session getSession() {
        return entityManager.unwrap(Session.class);
    }

    public ChatInvitation getChatInvitationById(ChatInvitationId id) {
        try {
            return getSession()
                    .createQuery(ChatInvitationServiceDAORequests.GET_BY_ID, ChatInvitation.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<ChatInvitation> getAllChatInvitations() {
        return getSession()
                .createQuery(ChatInvitationServiceDAORequests.GET_ALL, ChatInvitation.class)
                .getResultList();
    }

    public List<ChatInvitation> getByReceiverId(int receiverId) {
        return getSession()
                .createQuery(ChatInvitationServiceDAORequests.GET_BY_RECEIVER, ChatInvitation.class)
                .setParameter("receiverId", receiverId)
                .getResultList();
    }

    public List<ChatInvitation> getBySenderId(int senderId) {
        return getSession()
                .createQuery(ChatInvitationServiceDAORequests.GET_BY_SENDER, ChatInvitation.class)
                .setParameter("senderId", senderId)
                .getResultList();
    }

    public List<ChatInvitation> getByChatId(int chatId) {
        return getSession()
                .createQuery(ChatInvitationServiceDAORequests.GET_BY_CHAT, ChatInvitation.class)
                .setParameter("chatId", chatId)
                .getResultList();
    }

    public void removeBySenderId(int senderId) {
        getSession().createMutationQuery(ChatInvitationServiceDAORequests.DELETE_BY_SENDER)
                .setParameter("senderId", senderId)
                .executeUpdate();
    }

    public void removeByReceiverId(int receiverId) {
        getSession().createMutationQuery(ChatInvitationServiceDAORequests.DELETE_BY_RECEIVER)
                .setParameter("receiverId", receiverId)
                .executeUpdate();
    }

    public void removeByChatId(int chatId) {
        getSession().createMutationQuery(ChatInvitationServiceDAORequests.DELETE_BY_CHAT)
                .setParameter("chatId", chatId)
                .executeUpdate();
    }

    public void addChatInvitation(ChatInvitation chatInvitation) {
        getSession().persist(chatInvitation);
    }

    public void updateChatInvitation(ChatInvitation chatInvitation) {
        getSession().merge(chatInvitation);
    }

    public void removeChatInvitation(ChatInvitation chatInvitation) {
        getSession().remove(chatInvitation);
    }

    public void forceFlush() {
        getSession().flush();
    }
}
