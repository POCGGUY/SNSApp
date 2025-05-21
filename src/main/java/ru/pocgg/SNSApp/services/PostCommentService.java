package ru.pocgg.SNSApp.services;

import jakarta.transaction.Transactional;
import ru.pocgg.SNSApp.DTO.create.CreatePostCommentDTO;
import ru.pocgg.SNSApp.DTO.update.UpdatePostCommentDTO;
import ru.pocgg.SNSApp.model.Post;
import ru.pocgg.SNSApp.model.PostComment;
import ru.pocgg.SNSApp.model.User;
import ru.pocgg.SNSApp.model.exceptions.EntityNotFoundException;
import ru.pocgg.SNSApp.services.DAO.interfaces.PostCommentServiceDAO;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class PostCommentService extends TemplateService{
    private final PostCommentServiceDAO postCommentServiceDAO;
    private final PostService postService;
    private final UserService userService;

    public PostCommentService(PostCommentServiceDAO postCommentServiceDAO,
                              PostService postService,
                              UserService userService) {
        this.postCommentServiceDAO = postCommentServiceDAO;
        this.postService = postService;
        this.userService = userService;
    }

    public PostComment createComment(int postId,
                              int authorId,
                              CreatePostCommentDTO dto) {
        Post post = postService.getPostById(postId);
        User author = userService.getUserById(authorId);
        PostComment comment = PostComment.builder()
                .post(post)
                .author(author)
                .text(dto.getText())
                .creationDate(Instant.now())
                .deleted(false).build();
        postCommentServiceDAO.addComment(comment);
        postCommentServiceDAO.forceFlush();
        logger.info("created comment by author with id: {} under post with id: {}", authorId, postId);
        return comment;
    }


    public void setDeleted(int commentId, boolean value) {
        PostComment comment = getCommentByIdOrThrowException(commentId);
        comment.setDeleted(value);
        logger.info("comment with id: {} now has property deleted set to: {}", commentId, value);
    }

    public PostComment getCommentById(int commentId) {
        return getCommentByIdOrThrowException(commentId);
    }

    public void updateComment(int commentId, UpdatePostCommentDTO dto) {
        PostComment comment = getCommentByIdOrThrowException(commentId);
        updateText(comment, dto.getText());
        updateTime(comment);
    }

    public List<PostComment> getCommentsByPostId(int postId) {
        return postCommentServiceDAO.getCommentsByPostId(postId);
    }

    private void updateText(PostComment postComment, String text){
        if(text != null){
            postComment.setText(text);
            logger.info("post comment with id: {} has updated text", postComment.getId());
        }
    }

    private void updateTime(PostComment postComment){
        postComment.setUpdateDate(Instant.now());
    }


    private PostComment getCommentByIdOrThrowException(int id) {
        PostComment comment = postCommentServiceDAO.getCommentById(id);
        if (comment == null) {
            throw new EntityNotFoundException("comment with id " + id + " not found");
        }
        return comment;
    }
}
