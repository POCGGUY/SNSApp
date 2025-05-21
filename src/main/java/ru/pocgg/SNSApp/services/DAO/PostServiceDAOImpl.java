package ru.pocgg.SNSApp.services.DAO;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import ru.pocgg.SNSApp.model.Post;
import ru.pocgg.SNSApp.services.DAO.interfaces.PostServiceDAO;
import ru.pocgg.SNSApp.services.DAO.requests.PostServiceDAORequests;
import org.hibernate.Session;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Scope("singleton")
public class PostServiceDAOImpl implements PostServiceDAO {

    @PersistenceContext
    private EntityManager entityManager;

    private Session getSession() {
        return entityManager.unwrap(Session.class);
    }

    public Post getPostById(int id) {
        try {
            return getSession()
                    .createQuery(PostServiceDAORequests.GET_BY_ID, Post.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<Post> getPostsByCommunityOwnerId(int ownerId) {
        try {
            return getSession()
                    .createQuery(PostServiceDAORequests.GET_BY_COMMUNITY_OWNER_ID, Post.class)
                    .setParameter("ownerId", ownerId)
                    .getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<Post> getPostsByUserOwnerId(int ownerId) {
        try {
            return getSession()
                    .createQuery(PostServiceDAORequests.GET_BY_USER_OWNER_ID, Post.class)
                    .setParameter("ownerId", ownerId)
                    .getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<Post> getPostsByAuthorId(int authorId) {
        try {
            return getSession()
                    .createQuery(PostServiceDAORequests.GET_BY_AUTHOR_ID, Post.class)
                    .setParameter("authorId", authorId)
                    .getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public void addPost(Post post) {
        getSession().persist(post);
    }

    public void updatePost(Post post) {
        getSession().merge(post);
    }

    public void removePost(Post post) {
        getSession().remove(post);
    }

    public List<Post> getAllPosts() {
        try {
            return getSession()
                    .createQuery(PostServiceDAORequests.GET_ALL, Post.class)
                    .getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public void forceFlush() {
        getSession().flush();
    }
}
