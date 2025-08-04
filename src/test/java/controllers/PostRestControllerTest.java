package controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import ru.pocgg.SNSApp.DTO.create.CreatePostDTO;
import ru.pocgg.SNSApp.DTO.display.PostDisplayDTO;
import ru.pocgg.SNSApp.DTO.update.UpdatePostDTO;
import ru.pocgg.SNSApp.controller.rest.PostRestController;
import ru.pocgg.SNSApp.model.*;
import ru.pocgg.SNSApp.services.CommunityService;
import ru.pocgg.SNSApp.services.PostService;
import ru.pocgg.SNSApp.services.UserService;
import ru.pocgg.SNSApp.DTO.mappers.display.PostDisplayMapper;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostRestControllerTest {

    @Mock
    private PostService postService;

    @Mock
    private PermissionCheckService permissionCheckService;

    @Mock
    private UserService userService;

    @Mock
    private CommunityService communityService;

    @Mock
    private PostDisplayMapper postDisplayMapper;

    @InjectMocks
    private PostRestController controller;

    private int authorId;
    private int ownerUserId;
    private int ownerCommId;
    private int userId;
    private int postId;
    private Instant creationDate;
    private CreatePostDTO createDto;
    private UpdatePostDTO updateDto;
    private Post post;
    private PostDisplayDTO postDto;
    private List<Post> postList;
    private List<PostDisplayDTO> dtoList;

    @BeforeEach
    void setUp() {
        authorId = 1;
        ownerUserId = 2;
        ownerCommId = 3;
        userId = 4;
        postId = 10;
        creationDate = Instant.now();

        createDto = CreatePostDTO.builder()
                .text("Hello World!")
                .build();

        updateDto = UpdatePostDTO.builder()
                .text("Updated Text")
                .build();

        User author = User.builder()
                .userName("a").creationDate(Instant.now())
                .birthDate(LocalDate.now().minusYears(30))
                .password("p").email("a@a")
                .firstName("A").secondName("A")
                .gender(Gender.MALE).systemRole(SystemRole.USER)
                .deleted(false).acceptingPrivateMsgs(true)
                .postsPublic(true).banned(false)
                .build();
        author.setId(authorId);

        User ownerUser = User.builder().userName("r").creationDate(Instant.now())
                .birthDate(LocalDate.now().minusYears(30))
                .password("p").email("r@e").firstName("R").secondName("R")
                .gender(null).systemRole(null)
                .deleted(false).acceptingPrivateMsgs(true)
                .postsPublic(true).banned(false)
                .build();
        ownerUser.setId(ownerUserId);

        Community ownerComm = Community.builder()
                .owner(ownerUser)
                .communityName("Test")
                .creationDate(Instant.now())
                .description("desc")
                .isPrivate(false)
                .deleted(false)
                .banned(false)
                .build();

        post = Post.userPostBuilder()
                .author(author)
                .ownerUser(ownerUser)
                .text(createDto.getText())
                .creationDate(creationDate)
                .deleted(false)
                .build();
        post.setId(postId);

        postDto = PostDisplayDTO.builder()
                .id(postId)
                .ownerUserId(ownerUserId)
                .ownerCommunityId(null)
                .ownerName(ownerUser.getFirstAndSecondName())
                .authorId(authorId)
                .authorName(author.getFirstAndSecondName())
                .creationDate(creationDate.toString())
                .updateDate(null)
                .deleted(false)
                .text(createDto.getText())
                .build();

        postList = Collections.singletonList(post);
        dtoList = Collections.singletonList(postDto);
    }

    @Test
    void createUserPost_positive() {
        when(permissionCheckService.canUserCreateUserPost(authorId, ownerUserId)).thenReturn(true);

        User testUser = User.builder().build();
        testUser.setId(ownerUserId);

        when(userService.getUserById(ownerUserId)).thenReturn(testUser);

        when(postService.createUserPost(ownerUserId, authorId, createDto)).thenReturn(post);

        when(postDisplayMapper.toDTO(post)).thenReturn(postDto);

        ResponseEntity<PostDisplayDTO> resp = controller.createUserPost(authorId, ownerUserId, createDto);

        assertEquals(201, resp.getStatusCodeValue());
        assertEquals(postDto, resp.getBody());

        verify(permissionCheckService).canUserCreateUserPost(authorId, ownerUserId);
        verify(userService).getUserById(ownerUserId);
        verify(postService).createUserPost(ownerUserId, authorId, createDto);
        verify(postDisplayMapper).toDTO(post);
    }

    @Test
    void createUserPost_negative() {
        when(permissionCheckService.canUserCreateUserPost(authorId, ownerUserId)).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> controller.createUserPost(authorId, ownerUserId, createDto));

        verify(permissionCheckService).canUserCreateUserPost(authorId, ownerUserId);
        verifyNoInteractions(userService);
        verifyNoInteractions(postService);
    }

    @Test
    void createCommunityPost_positive() {
        when(permissionCheckService.canUserCreateCommunityPost(authorId, ownerCommId)).thenReturn(true);

        Community testCommunity = Community.builder().build();
        testCommunity.setId(ownerCommId);

        when(communityService.getCommunityById(ownerCommId)).thenReturn(testCommunity);

        when(postService.createCommunityPost(ownerCommId, authorId, createDto)).thenReturn(post);

        when(postDisplayMapper.toDTO(post)).thenReturn(postDto);

        ResponseEntity<PostDisplayDTO> resp = controller.createCommunityPost(authorId, ownerCommId, createDto);

        assertEquals(201, resp.getStatusCodeValue());
        assertEquals(postDto, resp.getBody());

        verify(permissionCheckService).canUserCreateCommunityPost(authorId, ownerCommId);
        verify(communityService).getCommunityById(ownerCommId);
        verify(postService).createCommunityPost(ownerCommId, authorId, createDto);
        verify(postDisplayMapper).toDTO(post);
    }

    @Test
    void createCommunityPost_negative() {
        when(permissionCheckService.canUserCreateCommunityPost(authorId, ownerCommId)).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> controller.createCommunityPost(authorId, ownerCommId, createDto));

        verify(permissionCheckService).canUserCreateCommunityPost(authorId, ownerCommId);
        verifyNoInteractions(communityService);
        verifyNoInteractions(postService);
    }

    @Test
    void getPostsByCommunity_positive() {
        when(permissionCheckService.canViewPostsAtCommunity(userId, ownerCommId)).thenReturn(true);

        when(postService.getPostsByCommunityOwner(ownerCommId)).thenReturn(postList);

        when(postDisplayMapper.toDTO(post)).thenReturn(postDto);

        ResponseEntity<List<PostDisplayDTO>> resp = controller.getPostsByCommunity(userId, ownerCommId);

        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(dtoList, resp.getBody());

        verify(permissionCheckService).canViewPostsAtCommunity(userId, ownerCommId);
        verify(postService).getPostsByCommunityOwner(ownerCommId);
        verify(postDisplayMapper).toDTO(post);
    }

    @Test
    void getPostsByCommunity_negative() {
        when(permissionCheckService.canViewPostsAtCommunity(userId, ownerCommId)).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> controller.getPostsByCommunity(userId, ownerCommId));

        verify(permissionCheckService).canViewPostsAtCommunity(userId, ownerCommId);
        verifyNoInteractions(postService);
    }

    @Test
    void getPostsByUser_positive() {
        when(permissionCheckService.canViewPostsAtUser(userId, ownerUserId)).thenReturn(true);

        when(postService.getPostsByUserOwner(ownerUserId)).thenReturn(postList);

        when(postDisplayMapper.toDTO(post)).thenReturn(postDto);

        ResponseEntity<List<PostDisplayDTO>> resp = controller.getPostsByUser(userId, ownerUserId);

        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(dtoList, resp.getBody());

        verify(permissionCheckService).canViewPostsAtUser(userId, ownerUserId);
        verify(postService).getPostsByUserOwner(ownerUserId);
        verify(postDisplayMapper).toDTO(post);
    }

    @Test
    void getPostsByUser_negative() {
        when(permissionCheckService.canViewPostsAtUser(userId, ownerUserId)).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> controller.getPostsByUser(userId, ownerUserId));

        verify(permissionCheckService).canViewPostsAtUser(userId, ownerUserId);
        verifyNoInteractions(postService);
    }

    @Test
    void updatePost_positive() {
        when(permissionCheckService.canUserModifyPost(userId, postId)).thenReturn(true);

        ResponseEntity<Void> resp = controller.updatePost(userId, postId, updateDto);

        assertEquals(204, resp.getStatusCodeValue());

        verify(permissionCheckService).canUserModifyPost(userId, postId);
        verify(postService).updatePost(postId, updateDto.getText());
    }

    @Test
    void updatePost_negative() {
        when(permissionCheckService.canUserModifyPost(userId, postId)).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> controller.updatePost(userId, postId, updateDto));

        verify(permissionCheckService).canUserModifyPost(userId, postId);
        verifyNoMoreInteractions(postService);
    }

    @Test
    void setDeleted_positive() {
        when(permissionCheckService.canUserDeletePost(userId, postId)).thenReturn(true);

        ResponseEntity<Void> resp = controller.setDeleted(userId, postId);

        assertEquals(204, resp.getStatusCodeValue());

        verify(permissionCheckService).canUserDeletePost(userId, postId);
        verify(postService).setIsDeleted(postId, true);
    }

    @Test
    void setDeleted_negative() {
        when(permissionCheckService.canUserDeletePost(userId, postId)).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> controller.setDeleted(userId, postId));

        verify(permissionCheckService).canUserDeletePost(userId, postId);
        verifyNoMoreInteractions(postService);
    }
}
