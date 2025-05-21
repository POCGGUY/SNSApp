package ru.pocgg.SNSApp.services.DAO;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import ru.pocgg.SNSApp.model.PrivateMessage;
import ru.pocgg.SNSApp.services.DAO.interfaces.PrivateMessageDAO;
import ru.pocgg.SNSApp.services.DAO.requests.PrivateMessageDAORequests;
import org.hibernate.Session;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Scope("singleton")
public class PrivateMessageDAOImpl implements PrivateMessageDAO {

    @PersistenceContext
    private EntityManager entityManager;

    private Session getSession() {
        return entityManager.unwrap(Session.class);
    }

    public PrivateMessage getById(int id) {
        try {
            return getSession()
                    .createQuery(PrivateMessageDAORequests.GET_BY_ID, PrivateMessage.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public void add(PrivateMessage message) {
        getSession().persist(message);
    }

    public void remove(PrivateMessage message) {
        getSession().remove(message);
    }

    public List<PrivateMessage> getByReceiverId(int receiverId) {
        try {
            return getSession()
                    .createQuery(PrivateMessageDAORequests.GET_BY_RECEIVER_ID, PrivateMessage.class)
                    .setParameter("receiverId", receiverId)
                    .getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }
    public List<PrivateMessage> getAllBySenderAndReceiver(int senderId, int receiverId){
        try {
            return getSession()
                    .createQuery(PrivateMessageDAORequests.GET_BY_RECEIVER_AND_SENDER_ID, PrivateMessage.class)
                    .setParameter("receiverId", receiverId)
                    .setParameter("senderId", senderId)
                    .getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public void forceFlush() {
        getSession().flush();
    }
}
