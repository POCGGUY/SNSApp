package ru.pocgg.SNSApp.services.DAO.interfaces;

import ru.pocgg.SNSApp.model.Post;
import org.hibernate.Session;

import java.util.List;

public interface PostServiceDAO {
    Post getPostById(int id);
    List<Post> getPostsByAuthorId(int authorId);
    void addPost(Post post);
    void updatePost(Post post);
    void removePost(Post post);
    List<Post> getAllPosts();
    void forceFlush();
    List<Post> getPostsByCommunityOwnerId(int ownerId);
    List<Post> getPostsByUserOwnerId(int ownerId);
}
