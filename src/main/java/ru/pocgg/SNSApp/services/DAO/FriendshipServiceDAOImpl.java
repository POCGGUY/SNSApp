package ru.pocgg.SNSApp.services.DAO;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import ru.pocgg.SNSApp.model.Friendship;
import ru.pocgg.SNSApp.model.FriendshipId;
import ru.pocgg.SNSApp.services.DAO.interfaces.FriendshipServiceDAO;
import ru.pocgg.SNSApp.services.DAO.requests.FriendshipServiceDAORequests;
import org.hibernate.Session;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Scope("singleton")
public class FriendshipServiceDAOImpl implements FriendshipServiceDAO {

    @PersistenceContext
    private EntityManager entityManager;

    private Session getSession(){
        return entityManager.unwrap(Session.class);
    }

    public Friendship getFriendshipByEmbeddedId(FriendshipId id) {
        try {
            return getSession()
                    .createQuery(FriendshipServiceDAORequests.GET_BY_ID, Friendship.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<Friendship> getFriendshipsByUserId(int userId){
        return getSession().createQuery(FriendshipServiceDAORequests.GET_BY_USER_ID, Friendship.class)
                .setParameter("userId", userId).getResultList();
    }

    public List<Friendship> getFriendshipsByFriendId(int friendId){
        return getSession().createQuery(FriendshipServiceDAORequests.GET_BY_FRIEND_ID, Friendship.class)
                .setParameter("friendId", friendId).getResultList();
    }

    public void addFriendship(Friendship friendship){
        getSession().persist(friendship);
    }

    public void updateFriendship(Friendship friendship){
        getSession().merge(friendship);
    }

    public void removeFriendship(Friendship friendship){
        getSession().remove(friendship);
    }

    public List<Friendship> getAllFriendships(){
        return getSession()
                .createQuery(FriendshipServiceDAORequests.GET_ALL, Friendship.class)
                .getResultList();
    }

    public void forceFlush(){
        getSession().flush();
    }
}
