package ru.pocgg.SNSApp.services.DAO;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import ru.pocgg.SNSApp.model.Notification;
import ru.pocgg.SNSApp.services.DAO.interfaces.NotificationServiceDAO;
import ru.pocgg.SNSApp.services.DAO.requests.NotificationServiceDAORequests;
import org.hibernate.Session;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Scope("singleton")
public class NotificationServiceDAOImpl implements NotificationServiceDAO {

    @PersistenceContext
    private EntityManager entityManager;

    private Session getSession() {
        return entityManager.unwrap(Session.class);
    }

    public Notification getNotificationById(int id) {
        try {
            return getSession()
                    .createQuery(NotificationServiceDAORequests.GET_BY_ID, Notification.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<Notification> getAllNotifications() {
        return getSession()
                .createQuery(NotificationServiceDAORequests.GET_ALL, Notification.class)
                .getResultList();
    }

    public List<Notification> getNotificationsByReceiverId(int receiverId) {
        return getSession()
                .createQuery(NotificationServiceDAORequests.GET_BY_RECEIVER_ID, Notification.class)
                .setParameter("receiverId", receiverId)
                .getResultList();
    }

    public void addNotification(Notification notification) {
        getSession().persist(notification);
    }

    public void removeNotification(Notification notification) {
        getSession().remove(notification);
    }

    public void forceFlush() {
        getSession().flush();
    }
}
