package ru.pocgg.SNSApp.services.DAO;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import ru.pocgg.SNSApp.model.PostComment;
import ru.pocgg.SNSApp.services.DAO.interfaces.PostCommentServiceDAO;
import ru.pocgg.SNSApp.services.DAO.requests.PostCommentServiceDAORequests;
import org.hibernate.Session;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Scope("singleton")
public class PostCommentServiceDAOImpl implements PostCommentServiceDAO {
    @PersistenceContext
    private EntityManager entityManager;

    private Session getSession() {
        return entityManager.unwrap(Session.class);
    }

    public PostComment getCommentById(int id) {
        try {
            return getSession()
                    .createQuery(PostCommentServiceDAORequests.GET_BY_ID, PostComment.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<PostComment> getCommentsByPostId(int postId) {
        return getSession()
                .createQuery(PostCommentServiceDAORequests.GET_BY_POST_ID, PostComment.class)
                .setParameter("postId", postId)
                .getResultList();
    }

    public void addComment(PostComment comment) {
        getSession().persist(comment);
    }

    public void updateComment(PostComment comment) {
        getSession().merge(comment);
    }

    public void removeComment(PostComment comment) {
        getSession().remove(comment);
    }

    public List<PostComment> getAllComments() {
        return getSession()
                .createQuery(PostCommentServiceDAORequests.GET_ALL, PostComment.class)
                .getResultList();
    }

    public void forceFlush(){
        getSession().flush();
    }
}
