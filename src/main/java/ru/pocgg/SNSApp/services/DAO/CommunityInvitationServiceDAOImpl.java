package ru.pocgg.SNSApp.services.DAO;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import ru.pocgg.SNSApp.model.CommunityInvitation;
import ru.pocgg.SNSApp.model.CommunityInvitationId;
import ru.pocgg.SNSApp.services.DAO.interfaces.CommunityInvitationServiceDAO;
import ru.pocgg.SNSApp.services.DAO.requests.CommunityInvitationServiceDAORequests;
import org.hibernate.Session;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Scope("singleton")
public class CommunityInvitationServiceDAOImpl implements CommunityInvitationServiceDAO {

    @PersistenceContext
    private EntityManager entityManager;

    private Session getSession() {
        return entityManager.unwrap(Session.class);
    }

    public CommunityInvitation getInvitationById(CommunityInvitationId id) {
        try {
            return getSession()
                    .createQuery(CommunityInvitationServiceDAORequests.GET_BY_ID, CommunityInvitation.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<CommunityInvitation> getInvitationsBySenderId(int senderId) {
        try {
            return getSession()
                    .createQuery(CommunityInvitationServiceDAORequests.GET_BY_SENDER_ID, CommunityInvitation.class)
                    .setParameter("senderId", senderId)
                    .getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<CommunityInvitation> getInvitationsByReceiverId(int receiverId) {
        try {
            return getSession()
                    .createQuery(CommunityInvitationServiceDAORequests.GET_BY_RECEIVER_ID, CommunityInvitation.class)
                    .setParameter("receiverId", receiverId)
                    .getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<CommunityInvitation> getInvitationsByCommunityId(int communityId) {
        try {
            return getSession()
                    .createQuery(CommunityInvitationServiceDAORequests.GET_BY_COMMUNITY_ID, CommunityInvitation.class)
                    .setParameter("communityId", communityId)
                    .getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<CommunityInvitation> getInvitationsByReceiverAndCommunityId(int receiverId, int communityId) {
        try {
            return getSession()
                    .createQuery(CommunityInvitationServiceDAORequests
                            .GET_BY_COMMUNITY_AND_RECEIVER_ID, CommunityInvitation.class)
                    .setParameter("communityId", communityId)
                    .setParameter("receiverId", receiverId)
                    .getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<CommunityInvitation> getAllInvitations() {
        try {
            return getSession()
                    .createQuery(CommunityInvitationServiceDAORequests.GET_ALL, CommunityInvitation.class)
                    .getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public void removeBySenderId(int senderId) {
        getSession()
                .createMutationQuery(CommunityInvitationServiceDAORequests.DELETE_BY_SENDER_ID)
                .setParameter("senderId", senderId)
                .executeUpdate();
    }

    public void removeBySenderAndCommunity(int senderId, int communityId) {
        getSession()
                .createMutationQuery(CommunityInvitationServiceDAORequests.DELETE_BY_COMMUNITY_AND_SENDER)
                .setParameter("communityId", communityId)
                .setParameter("senderId", senderId)
                .executeUpdate();
    }

    public void removeByReceiverId(int receiverId) {
        getSession()
                .createMutationQuery(CommunityInvitationServiceDAORequests.DELETE_BY_RECEIVER_ID)
                .setParameter("receiverId", receiverId)
                .executeUpdate();
    }

    public void removeByCommunityId(int communityId) {
        getSession()
                .createMutationQuery(CommunityInvitationServiceDAORequests.DELETE_BY_COMMUNITY_ID)
                .setParameter("communityId", communityId)
                .executeUpdate();
    }

    public void addInvitation(CommunityInvitation invitation) {
        getSession().persist(invitation);
    }

    public void removeInvitation(CommunityInvitation invitation) {
        getSession().remove(invitation);
    }

    public void forceFlush() {
        getSession().flush();
    }
}
