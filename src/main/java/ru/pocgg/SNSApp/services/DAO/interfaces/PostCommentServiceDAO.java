package ru.pocgg.SNSApp.services.DAO.interfaces;

import ru.pocgg.SNSApp.model.PostComment;
import org.hibernate.Session;

import java.util.List;

public interface PostCommentServiceDAO {
    PostComment getCommentById(int id);
    List<PostComment> getCommentsByPostId(int postId);
    void addComment(PostComment comment);
    void updateComment(PostComment comment);
    void removeComment(PostComment comment);
    List<PostComment> getAllComments();
    void forceFlush();
}
