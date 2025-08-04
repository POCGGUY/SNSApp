package controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import ru.pocgg.SNSApp.DTO.create.CreatePostCommentDTO;
import ru.pocgg.SNSApp.DTO.display.PostCommentDisplayDTO;
import ru.pocgg.SNSApp.DTO.update.UpdatePostCommentDTO;
import ru.pocgg.SNSApp.DTO.mappers.display.PostCommentDisplayMapper;
import ru.pocgg.SNSApp.controller.rest.PostCommentRestController;
import ru.pocgg.SNSApp.model.Post;
import ru.pocgg.SNSApp.model.PostComment;
import ru.pocgg.SNSApp.model.User;
import ru.pocgg.SNSApp.services.PostCommentService;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostCommentRestControllerTest {

    @Mock
    private PostCommentService postCommentService;

    @Mock
    private PermissionCheckService permissionCheckService;

    @Mock
    private PostCommentDisplayMapper commentDisplayMapper;

    @InjectMocks
    private PostCommentRestController controller;

    private int postId;
    private int userId;
    private User ownerUser;
    private int commentId;
    private Instant creationDate;
    private CreatePostCommentDTO createDto;
    private UpdatePostCommentDTO updateDto;
    private PostComment comment;
    private PostCommentDisplayDTO commentDto;
    private PostComment comment1;
    private PostComment comment2;
    private PostCommentDisplayDTO commentDto1;
    private PostCommentDisplayDTO commentDto2;
    private List<PostComment> commentList;
    private List<PostCommentDisplayDTO> dtoList;

    @BeforeEach
    void setUp() {
        postId = 0;
        userId = 1;
        commentId = 2;
        creationDate = Instant.now();

        createDto = CreatePostCommentDTO.builder()
                .text("New comment")
                .build();

        updateDto = UpdatePostCommentDTO.builder()
                .text("Edited comment")
                .build();

        ownerUser = User.builder().userName("r").creationDate(Instant.now())
                .birthDate(LocalDate.now().minusYears(30))
                .password("p").email("r@e").firstName("R").secondName("R")
                .gender(null).systemRole(null)
                .deleted(false).acceptingPrivateMsgs(true)
                .postsPublic(true).banned(false)
                .build();
        ownerUser.setId(userId);

        Post post = Post.userPostBuilder()
                .ownerUser(ownerUser)
                .text("TEEEST")
                .creationDate(creationDate)
                .deleted(false)
                .build();

        comment = PostComment.builder()
                .post(post)
                .author(ownerUser)
                .text(createDto.getText())
                .creationDate(creationDate)
                .build();
        comment.setId(commentId);

        commentDto = PostCommentDisplayDTO.builder()
                .id(commentId)
                .postId(postId)
                .authorId(userId)
                .text(createDto.getText())
                .creationDate(creationDate.toString())
                .build();

        comment1 = PostComment.builder()
                .post(post)
                .author(ownerUser)
                .text("First")
                .creationDate(creationDate.plusSeconds(10))
                .build();
        comment1.setId(100);

        comment2 = PostComment.builder()
                .post(post)
                .author(ownerUser)
                .text("Second")
                .creationDate(creationDate)
                .build();
        comment2.setId(100);

        commentDto1 = PostCommentDisplayDTO.builder()
                .id(100)
                .postId(postId)
                .authorId(userId)
                .text("First")
                .creationDate(creationDate.plusSeconds(10).toString())
                .build();

        commentDto2 = PostCommentDisplayDTO.builder()
                .id(101)
                .postId(postId)
                .authorId(userId)
                .text("Second")
                .creationDate(creationDate.toString())
                .build();

        commentList = Arrays.asList(comment1, comment2);
        dtoList = Arrays.asList(commentDto1, commentDto2);
    }

    @Test
    void createComment_positive() {
        when(permissionCheckService.canUserViewPost(userId, postId)).thenReturn(true);

        when(postCommentService.createComment(postId, userId, createDto))
                .thenReturn(comment);

        when(commentDisplayMapper.toDTO(comment))
                .thenReturn(commentDto);

        ResponseEntity<PostCommentDisplayDTO> resp =
                controller.createComment(postId, userId, createDto);

        assertEquals(201, resp.getStatusCodeValue());
        assertEquals(commentDto, resp.getBody());
    }

    @Test
    void createComment_negative() {
        when(permissionCheckService.canUserViewPost(userId, postId)).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> controller.createComment(postId, userId, createDto));
    }

    @Test
    void listComments_positive() {
        when(permissionCheckService.canUserViewPost(userId, postId)).thenReturn(true);

        when(postCommentService.getCommentsByPostId(postId))
                .thenReturn(commentList);

        when(commentDisplayMapper.toDTO(comment1))
                .thenReturn(commentDto1);
        when(commentDisplayMapper.toDTO(comment2))
                .thenReturn(commentDto2);

        ResponseEntity<List<PostCommentDisplayDTO>> resp =
                controller.listComments(postId);

        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(dtoList, resp.getBody());
    }

    @Test
    void listComments_negative() {
        when(permissionCheckService.canUserViewPost(userId, postId)).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> controller.listComments(postId));
    }

    @Test
    void getComment_positive() {
        Post parentPost = Post.userPostBuilder().ownerUser(ownerUser).build();
        parentPost.setId(postId);

        when(postCommentService.getCommentById(commentId))
                .thenReturn(comment);

        when(permissionCheckService.canUserViewPost(userId, postId))
                .thenReturn(true);

        when(commentDisplayMapper.toDTO(comment))
                .thenReturn(commentDto);

        ResponseEntity<PostCommentDisplayDTO> resp =
                controller.getComment(commentId);

        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(commentDto, resp.getBody());
    }

    @Test
    void getComment_negative() {
        when(postCommentService.getCommentById(commentId))
                .thenReturn(comment);

        when(permissionCheckService.canUserViewPost(userId, postId))
                .thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> controller.getComment(commentId));
    }

    @Test
    void updateComment_positive() {
        when(permissionCheckService.canUserModifyPostComment(userId, commentId))
                .thenReturn(true);

        ResponseEntity<Void> resp =
                controller.updateComment(commentId, updateDto);

        assertEquals(204, resp.getStatusCodeValue());
    }

    @Test
    void updateComment_negative() {
        when(permissionCheckService.canUserModifyPostComment(userId, commentId))
                .thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> controller.updateComment(commentId, updateDto));
    }

    @Test
    void deleteComment_positive() {
        when(permissionCheckService.canUserDeletePostComment(userId, commentId))
                .thenReturn(true);

        ResponseEntity<Void> resp =
                controller.deleteComment(commentId);

        assertEquals(204, resp.getStatusCodeValue());
    }

    @Test
    void deleteComment_negative() {
        when(permissionCheckService.canUserDeletePostComment(userId, commentId))
                .thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> controller.deleteComment(commentId));
    }
}
