package ru.pocgg.SNSApp.services.DAO;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import ru.pocgg.SNSApp.model.FriendshipRequest;
import ru.pocgg.SNSApp.model.FriendshipRequestId;
import ru.pocgg.SNSApp.services.DAO.interfaces.FriendshipRequestServiceDAO;
import ru.pocgg.SNSApp.services.DAO.requests.FriendshipRequestServiceDAORequests;
import org.hibernate.Session;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Scope("singleton")
public class FriendshipRequestServiceDAOImpl implements FriendshipRequestServiceDAO {

    @PersistenceContext
    private EntityManager entityManager;

    private Session getSession() {
        return entityManager.unwrap(Session.class);
    }

    public FriendshipRequest getRequestById(FriendshipRequestId id) {
        try {
            return getSession()
                    .createQuery(FriendshipRequestServiceDAORequests.GET_BY_ID, FriendshipRequest.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<FriendshipRequest> getRequestsBySenderId(int senderId) {
        return getSession()
                .createQuery(FriendshipRequestServiceDAORequests.GET_BY_SENDER_ID, FriendshipRequest.class)
                .setParameter("senderId", senderId)
                .getResultList();
    }

    public List<FriendshipRequest> getRequestsByReceiverId(int receiverId) {
        return getSession()
                .createQuery(FriendshipRequestServiceDAORequests.GET_BY_RECEIVER_ID, FriendshipRequest.class)
                .setParameter("receiverId", receiverId)
                .getResultList();
    }

    public List<FriendshipRequest> getAllRequests() {
        return getSession()
                .createQuery(FriendshipRequestServiceDAORequests.GET_ALL, FriendshipRequest.class)
                .getResultList();
    }

    public void removeBySenderId(int senderId) {
        getSession()
                .createMutationQuery(FriendshipRequestServiceDAORequests.DELETE_BY_SENDER_ID)
                .setParameter("senderId", senderId)
                .executeUpdate();
    }
    public void removeByReceiverId(int receiverId) {
        getSession()
                .createMutationQuery(FriendshipRequestServiceDAORequests.DELETE_BY_RECEIVER_ID)
                .setParameter("receiverId", receiverId)
                .executeUpdate();
    }

    public void addRequest(FriendshipRequest request) {
        getSession().persist(request);
    }

    public void removeRequest(FriendshipRequest request) {
        getSession().remove(request);
    }

    public void forceFlush() {
        getSession().flush();
    }
}
