package ru.pocgg.SNSApp.services.DAO;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import ru.pocgg.SNSApp.model.Chat;
import ru.pocgg.SNSApp.services.DAO.interfaces.ChatServiceDAO;
import ru.pocgg.SNSApp.services.DAO.requests.ChatServiceDAORequests;
import org.hibernate.Session;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Scope("singleton")
public class ChatServiceDAOImpl implements ChatServiceDAO {

    @PersistenceContext
    private EntityManager entityManager;

    private Session getSession() {
        return entityManager.unwrap(Session.class);
    }

    public Chat getChatById(int id) {
        try {
            return getSession()
                    .createQuery(ChatServiceDAORequests.GET_BY_ID, Chat.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<Chat> getAllChats() {
        return getSession()
                .createQuery(ChatServiceDAORequests.GET_ALL, Chat.class)
                .getResultList();
    }

    public void addChat(Chat chat) {
        getSession().persist(chat);
    }

    public void updateChat(Chat chat) {
        getSession().merge(chat);
    }

    public void removeChat(Chat chat) {
        getSession().remove(chat);
    }

    public void forceFlush() {
        getSession().flush();
    }
}
