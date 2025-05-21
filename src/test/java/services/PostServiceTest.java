package services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.pocgg.SNSApp.DTO.create.CreatePostDTO;
import ru.pocgg.SNSApp.DTO.update.UpdatePostDTO;
import ru.pocgg.SNSApp.model.*;
import ru.pocgg.SNSApp.model.exceptions.EntityNotFoundException;
import ru.pocgg.SNSApp.services.PostService;
import ru.pocgg.SNSApp.services.DAO.interfaces.PostServiceDAO;
import ru.pocgg.SNSApp.services.UserService;
import ru.pocgg.SNSApp.services.CommunityService;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    PostServiceDAO dao;
    @Mock
    UserService userService;
    @Mock
    CommunityService communityService;
    @InjectMocks
    PostService service;

    private User user;
    private Community community;
    private CreatePostDTO createDto;
    private UpdatePostDTO updateDto;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .userName("a").creationDate(Instant.now())
                .birthDate(LocalDate.now().minusYears(30))
                .password("p").email("a@a")
                .firstName("A").secondName("A")
                .gender(Gender.MALE).systemRole(SystemRole.USER)
                .deleted(false).acceptingPrivateMsgs(true)
                .postsPublic(true).banned(false)
                .build();
        user.setId(1);

        community = Community.builder()
                .communityName("c")
                .owner(user)
                .creationDate(Instant.now())
                .isPrivate(false)
                .description(null)
                .deleted(false)
                .banned(false)
                .build();
        community.setId(2);

        createDto = CreatePostDTO.builder()
                .text("hello").build();
        updateDto = UpdatePostDTO.builder()
                .text("edited").build();
    }

    @Test
    void createUserPost_positive() {
        when(userService.getUserById(1)).thenReturn(user);
        when(userService.getUserById(3)).thenReturn(user);

        Post post = service.createUserPost(1, 3, createDto);

        verify(dao).addPost(argThat(p ->
                p.getOwnerUser() == user &&
                        p.getAuthor() != null &&
                        "hello".equals(p.getText()) &&
                        !p.getDeleted()
        ));
        verify(dao).forceFlush();
        assertEquals("hello", post.getText());
    }

    @Test
    void createUserPost_negative() {
        when(userService.getUserById(1))
                .thenThrow(new EntityNotFoundException("not found"));
        assertThrows(EntityNotFoundException.class,
                () -> service.createUserPost(1, 3, createDto));
    }

    @Test
    void createCommunityPost_positive() {
        when(communityService.getCommunityById(2)).thenReturn(community);
        when(userService.getUserById(3)).thenReturn(user);

        Post post = service.createCommunityPost(2, 3, createDto);

        verify(dao).addPost(argThat(p ->
                p.getOwnerCommunity() == community &&
                        p.getAuthor() == user &&
                        "hello".equals(p.getText())
        ));
        assertEquals("hello", post.getText());
    }

    @Test
    void createCommunityPost_negative() {
        when(communityService.getCommunityById(2))
                .thenThrow(new EntityNotFoundException("no community"));
        assertThrows(EntityNotFoundException.class,
                () -> service.createCommunityPost(2, 3, createDto));
    }

    @Test
    void getPostById_positive() {
        Post post = Post.userPostBuilder()
                .ownerUser(user).author(user)
                .text("x").creationDate(Instant.now())
                .deleted(false).updateDate(null).build();
        post.setId(5);
        when(dao.getPostById(5)).thenReturn(post);
        assertSame(post, service.getPostById(5));
    }

    @Test
    void getPostById_negative() {
        when(dao.getPostById(9)).thenReturn(null);
        assertThrows(EntityNotFoundException.class,
                () -> service.getPostById(9));
    }

    @Test
    void updatePost_positive() {
        Post post = Post.userPostBuilder()
                .ownerUser(user).author(user)
                .text("old").creationDate(Instant.now())
                .deleted(false).updateDate(null).build();
        post.setId(7);
        when(dao.getPostById(7)).thenReturn(post);

        service.updatePost(7, updateDto);

        assertEquals("edited", post.getText());
        assertNotNull(post.getUpdateDate());
    }

    @Test
    void updatePost_negative() {
        when(dao.getPostById(8)).thenReturn(null);

        assertThrows(EntityNotFoundException.class,
                () -> service.updatePost(8, updateDto));
    }

    @Test
    void getPostsByCommunityOwner_positive() {
        List<Post> list = List.of(mock(Post.class));
        when(dao.getPostsByCommunityOwnerId(2)).thenReturn(list);

        assertEquals(list, service.getPostsByCommunityOwner(2));
    }

    @Test
    void getPostsByUserOwner_positive() {
        List<Post> list = List.of(mock(Post.class));
        when(dao.getPostsByUserOwnerId(1)).thenReturn(list);

        assertEquals(list, service.getPostsByUserOwner(1));
    }

    @Test
    void getPostsByAuthor_positive() {
        List<Post> list = List.of(mock(Post.class));
        when(dao.getPostsByAuthorId(3)).thenReturn(list);

        assertEquals(list, service.getPostsByAuthor(3));
    }

    @Test
    void updatePostText_positive() {
        Post post = Post.userPostBuilder()
                .ownerUser(user).author(user)
                .text("a").creationDate(Instant.now())
                .deleted(false).updateDate(null).build();
        post.setId(11);
        when(dao.getPostById(11)).thenReturn(post);

        service.updatePostText(11, "new");

        assertEquals("new", post.getText());
        assertNotNull(post.getUpdateDate());
    }

    @Test
    void updatePostText_negative() {
        when(dao.getPostById(12)).thenReturn(null);

        assertThrows(EntityNotFoundException.class,
                () -> service.updatePostText(12, "x"));
    }

    @Test
    void setIsDeleted_positive() {
        Post post = Post.userPostBuilder()
                .ownerUser(user).author(user)
                .text("t").creationDate(Instant.now())
                .deleted(false).updateDate(null).build();
        post.setId(13);
        when(dao.getPostById(13)).thenReturn(post);

        service.setIsDeleted(13, true);

        assertTrue(post.getDeleted());
        assertNotNull(post.getUpdateDate());
    }

    @Test
    void setIsDeleted_negative() {
        when(dao.getPostById(14)).thenReturn(null);

        assertThrows(EntityNotFoundException.class,
                () -> service.setIsDeleted(14, false));
    }

    @Test
    void removePost_positive() {
        Post post = Post.userPostBuilder()
                .ownerUser(user).author(user)
                .text("t").creationDate(Instant.now())
                .deleted(false).updateDate(null).build();
        post.setId(15);
        when(dao.getPostById(15)).thenReturn(post);

        service.removePost(15);
        verify(dao).removePost(post);
    }

    @Test
    void removePost_negative() {
        when(dao.getPostById(16)).thenReturn(null);
        assertThrows(EntityNotFoundException.class,
                () -> service.removePost(16));
    }
}
