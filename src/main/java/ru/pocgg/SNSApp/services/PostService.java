package ru.pocgg.SNSApp.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import ru.pocgg.SNSApp.model.*;
import ru.pocgg.SNSApp.DTO.create.CreatePostDTO;
import ru.pocgg.SNSApp.DTO.update.UpdatePostDTO;
import ru.pocgg.SNSApp.model.exceptions.EntityNotFoundException;
import ru.pocgg.SNSApp.services.DAO.interfaces.PostServiceDAO;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PostService extends TemplateService{
    private final PostServiceDAO postDAO;
    private final UserService userService;
    private final CommunityService communityService;

    public Post createUserPost(int ownerId, int authorId, CreatePostDTO dto) {
        User owner = userService.getUserById(ownerId);
        User author = userService.getUserById(authorId);
        Post post = Post.userPostBuilder()
                .ownerUser(owner)
                .author(author)
                .text(dto.getText())
                .creationDate(Instant.now())
                .deleted(false)
                .updateDate(null).build();
        postDAO.addPost(post);
        postDAO.forceFlush();
        logPostCreated(post.getId(), authorId, ownerId);
        return post;
    }

    public Post createCommunityPost(int ownerId, int authorId, CreatePostDTO dto) {
        Community owner = communityService.getCommunityById(ownerId);
        User author = userService.getUserById(authorId);
        Post post = Post.communityPostBuilder()
                .ownerCommunity(owner)
                .author(author)
                .text(dto.getText())
                .creationDate(Instant.now())
                .deleted(false)
                .updateDate(null).build();
        postDAO.addPost(post);
        postDAO.forceFlush();
        logPostCreated(post.getId(), authorId, ownerId);
        return post;
    }

    public void updatePost(int postId, UpdatePostDTO dto) {
        Post post = getPostByIdOrThrowException(postId);
        updateText(post, dto.getText());
        updateTime(post);
    }

    public Post getPostById(int postId) {
        return getPostByIdOrThrowException(postId);
    }

    public List<Post> getPostsByCommunityOwner(int ownerId) {
        return postDAO.getPostsByCommunityOwnerId(ownerId);
    }

    public List<Post> getPostsByUserOwner(int ownerId) {
        return postDAO.getPostsByUserOwnerId(ownerId);
    }

    public List<Post> getPostsByAuthor(int authorId) {
        return postDAO.getPostsByAuthorId(authorId);
    }

    public void updatePostText(int postId, String newText) {
        Post post = getPostByIdOrThrowException(postId);
        post.setUpdateDate(Instant.now());
        post.setText(newText);
        logger.info("updated text of post with id: {}", postId);
    }

    public void setIsDeleted(int postId, boolean value) {
        Post post = getPostByIdOrThrowException(postId);
        post.setDeleted(value);
        post.setUpdateDate(Instant.now());
        logger.info("post with id: {} now is deleted", postId);
    }

    public void removePost(int postId) {
        Post post = getPostByIdOrThrowException(postId);
        postDAO.removePost(post);
        logger.info("removed post with id: {}", postId);
    }

    private Post getPostByIdOrThrowException(int postId){
        Post post = postDAO.getPostById(postId);
        if (post == null) {
            throw new EntityNotFoundException("post with id: " + postId + " not found");
        }
        return post;
    }

    private void updateTime(Post post) {
        post.setUpdateDate(Instant.now());
    }

    private void updateText(Post post, String text) {
        if(text != null) {
            post.setText(text);
            logger.info("post with id: {} has updated text", post.getId());
        }
    }

    private void logPostCreated(int postId, int authorId, int ownerId) {
        logger.info("created post with id: {} by author with id: {} for owner with id: {}",
                postId, authorId, ownerId);
    }
}
