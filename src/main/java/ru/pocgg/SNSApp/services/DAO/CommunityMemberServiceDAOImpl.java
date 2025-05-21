package ru.pocgg.SNSApp.services.DAO;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import ru.pocgg.SNSApp.model.Community;
import ru.pocgg.SNSApp.model.CommunityMember;
import ru.pocgg.SNSApp.model.CommunityMemberId;
import ru.pocgg.SNSApp.services.DAO.interfaces.CommunityMemberServiceDAO;
import ru.pocgg.SNSApp.services.DAO.requests.CommunityMemberServiceDAORequests;
import org.hibernate.Session;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Scope("singleton")
public class CommunityMemberServiceDAOImpl implements CommunityMemberServiceDAO {

    @PersistenceContext
    private EntityManager entityManager;

    private Session getSession() {
        return entityManager.unwrap(Session.class);
    }

    public CommunityMember getMemberById(CommunityMemberId id) {
        try {
            return getSession()
                    .createQuery(CommunityMemberServiceDAORequests.GET_BY_ID, CommunityMember.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<CommunityMember> getMembersByCommunityId(int communityId) {
        return getSession()
                .createQuery(CommunityMemberServiceDAORequests.GET_BY_COMMUNITY_ID, CommunityMember.class)
                .setParameter("communityId", communityId)
                .getResultList();
    }

    public List<Community> getCommunitiesByMemberId(int memberId) {
        return getSession()
                .createQuery(CommunityMemberServiceDAORequests.GET_BY_MEMBER_ID, Community.class)
                .setParameter("memberId", memberId)
                .getResultList();
    }

    public List<CommunityMember> getAllMembers() {
        return getSession()
                .createQuery(CommunityMemberServiceDAORequests.GET_ALL, CommunityMember.class)
                .getResultList();
    }

    public void addMember(CommunityMember member) {
        getSession().persist(member);
    }

    public void removeMember(CommunityMember member) {
        getSession().remove(member);
    }

    public void forceFlush() {
        getSession().flush();
    }
}
