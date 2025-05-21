package ru.pocgg.SNSApp.services.DAO;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import ru.pocgg.SNSApp.model.Community;
import ru.pocgg.SNSApp.model.Gender;
import ru.pocgg.SNSApp.model.User;
import ru.pocgg.SNSApp.services.DAO.interfaces.CommunityServiceDAO;
import ru.pocgg.SNSApp.services.DAO.requests.CommunityServiceDAORequests;
import org.hibernate.Session;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
@Scope("singleton")
public class
CommunityServiceDAOImpl implements CommunityServiceDAO {

    @PersistenceContext
    private EntityManager entityManager;

    private Session getSession(){
        return entityManager.unwrap(Session.class);
    }

    public void addCommunity(Community community){
        getSession().persist(community);
    }

    public void updateCommunity(Community community){
        getSession().merge(community);
    }

    public void deleteCommunity(Community community){
        getSession().remove(community);
    }

    public Community getCommunityById(int id) {
        try {
            return getSession()
                    .createQuery(CommunityServiceDAORequests.GET_BY_ID, Community.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<Community> getAllCommunities(){
        return getSession().createQuery(CommunityServiceDAORequests.GET_ALL, Community.class).getResultList();
    }

    public List<Community> searchCommunities(String name) {
        try {
            CriteriaBuilder criteriaBuilder = getSession().getCriteriaBuilder();
            CriteriaQuery<Community> criteriaQuery = criteriaBuilder.createQuery(Community.class);
            Root<Community> communityRoot = criteriaQuery.from(Community.class);

            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.isFalse(communityRoot.get("deleted")));
            predicates.add(criteriaBuilder.isFalse(communityRoot.get("banned")));
            predicates.add(criteriaBuilder.isFalse(communityRoot.get("isPrivate")));


            if (name != null && !name.isBlank()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(communityRoot.get("communityName")),
                        "%" + name.toLowerCase() + "%"
                ));
            }

            criteriaQuery.where(predicates.toArray(new Predicate[0]));
            return getSession().createQuery(criteriaQuery).getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public void forceFlush(){
        getSession().flush();
    }
}
