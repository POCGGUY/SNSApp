package services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.pocgg.SNSApp.DTO.create.CreatePostCommentDTO;
import ru.pocgg.SNSApp.DTO.update.UpdatePostCommentDTO;
import ru.pocgg.SNSApp.model.*;
import ru.pocgg.SNSApp.model.exceptions.EntityNotFoundException;
import ru.pocgg.SNSApp.services.DAO.interfaces.PostCommentServiceDAO;
import ru.pocgg.SNSApp.services.PostCommentService;
import ru.pocgg.SNSApp.services.PostService;
import ru.pocgg.SNSApp.services.UserService;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostCommentServiceTest {

    @Mock
    PostCommentServiceDAO dao;
    @Mock
    PostService postService;
    @Mock
    UserService userService;
    @InjectMocks
    PostCommentService service;

    CreatePostCommentDTO createDto;
    UpdatePostCommentDTO updateDto;
    Post post;
    User author;
    PostComment comment;

    @BeforeEach
    void setUp() {
        createDto = CreatePostCommentDTO.builder()
                .text("hello")
                .build();
        updateDto = UpdatePostCommentDTO.builder()
                .text("updated")
                .build();
        post = mock(Post.class);
        author = User.builder()
                .userName("a").creationDate(Instant.now())
                .birthDate(LocalDate.now().minusYears(30))
                .password("p").email("a@a")
                .firstName("A").secondName("A")
                .gender(Gender.MALE).systemRole(SystemRole.USER)
                .deleted(false).acceptingPrivateMsgs(true)
                .postsPublic(true).banned(false)
                .build();
        comment = PostComment.builder()
                .post(post)
                .author(author)
                .text("hello")
                .creationDate(Instant.now())
                .deleted(false)
                .build();
        comment.setId(42);
    }

    @Test
    void createComment_positive() {
        when(postService.getPostById(1)).thenReturn(post);
        when(userService.getUserById(2)).thenReturn(author);

        PostComment out = service.createComment(1, 2, createDto);

        verify(dao).addComment(argThat(c ->
                c.getPost() == post &&
                        c.getAuthor() == author &&
                        "hello".equals(c.getText()) &&
                        Boolean.FALSE.equals(c.getDeleted())
        ));
        verify(dao).forceFlush();
        assertEquals("hello", out.getText());
    }

    @Test
    void createComment_negative() {
        when(postService.getPostById(1)).thenThrow(new EntityNotFoundException("not found"));

        assertThrows(EntityNotFoundException.class, () -> service.createComment(1, 2, createDto));
    }

    @Test
    void getCommentById_positive() {
        when(dao.getCommentById(42)).thenReturn(comment);

        assertSame(comment, service.getCommentById(42));
    }

    @Test
    void getCommentById_negative() {
        when(dao.getCommentById(99)).thenReturn(null);

        assertThrows(EntityNotFoundException.class, () -> service.getCommentById(99));
    }

    @Test
    void setDeleted_positive() {
        when(dao.getCommentById(42)).thenReturn(comment);

        service.setDeleted(42, true);

        assertTrue(comment.getDeleted());
    }

    @Test
    void setDeleted_negative() {
        when(dao.getCommentById(99)).thenReturn(null);

        assertThrows(EntityNotFoundException.class, () -> service.setDeleted(99, true));
    }

    @Test
    void updateComment_positive() {
        when(dao.getCommentById(42)).thenReturn(comment);

        service.updateComment(42, updateDto);

        assertEquals("updated", comment.getText());
        assertNotNull(comment.getUpdateDate());
    }

    @Test
    void updateComment_negative() {
        when(dao.getCommentById(99)).thenReturn(null);

        assertThrows(EntityNotFoundException.class, () -> service.updateComment(99, updateDto));
    }

    @Test
    void getCommentsByPostId_positive() {
        List<PostComment> list = List.of(comment);
        when(dao.getCommentsByPostId(1)).thenReturn(list);

        assertEquals(list, service.getCommentsByPostId(1));
    }

    @Test
    void getCommentsByPostId_negative() {
        when(dao.getCommentsByPostId(1)).thenReturn(List.of());

        assertTrue(service.getCommentsByPostId(1).isEmpty());
    }
}
