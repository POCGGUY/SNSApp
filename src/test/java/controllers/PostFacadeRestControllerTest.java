package controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import ru.pocgg.SNSApp.DTO.display.PostCommentDisplayDTO;
import ru.pocgg.SNSApp.DTO.display.PostDisplayDTO;
import ru.pocgg.SNSApp.DTO.display.PostFacadeDTO;
import ru.pocgg.SNSApp.DTO.mappers.PostCommentDisplayMapper;
import ru.pocgg.SNSApp.DTO.mappers.PostDisplayMapper;
import ru.pocgg.SNSApp.controller.rest.PostFacadeRestController;
import ru.pocgg.SNSApp.model.Post;
import ru.pocgg.SNSApp.model.PostComment;
import ru.pocgg.SNSApp.model.User;
import ru.pocgg.SNSApp.services.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostFacadeRestControllerTest {

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

    @Mock
    private PostCommentDisplayMapper commentDisplayMapper;

    @Mock
    private PostCommentService postCommentService;

    @InjectMocks
    private PostFacadeRestController controller;

    private int userId;
    private int postId;
    private int comment1Id;
    private int comment2Id;
    private int ownerUserId;
    private Instant creationDate;

    private Post post;
    private PostDisplayDTO postDto;

    private PostComment comment1;
    private PostComment comment2;
    private PostCommentDisplayDTO commentDto1;
    private PostCommentDisplayDTO commentDto2;
    private List<PostComment> comments;
    private List<PostCommentDisplayDTO> commentDtos;

    @BeforeEach
    void setUp() {
        userId = 1;
        postId = 2;
        comment1Id = 3;
        comment2Id = 4;
        ownerUserId = 5;
        creationDate = Instant.now();

        postDto = PostDisplayDTO.builder()
                .id(postId)
                .text("Hello World!")
                .creationDate(creationDate.toString())
                .build();

        User ownerUser = User.builder().userName("r").creationDate(Instant.now())
                .birthDate(LocalDate.now().minusYears(30))
                .password("p").email("r@e").firstName("R").secondName("R")
                .gender(null).systemRole(null)
                .deleted(false).acceptingPrivateMsgs(true)
                .postsPublic(true).banned(false)
                .build();
        ownerUser.setId(ownerUserId);

        post = Post.userPostBuilder()
                .ownerUser(ownerUser)
                .text(postDto.getText())
                .creationDate(creationDate)
                .deleted(false)
                .build();

        comment1 = PostComment.builder()
                .text("First comment")
                .creationDate(creationDate.plusSeconds(10))
                .build();
        comment1.setId(comment1Id);

        comment2 = PostComment.builder()
                .text("Second comment")
                .creationDate(creationDate)
                .build();
        comment2.setId(comment2Id);

        commentDto1 = PostCommentDisplayDTO.builder()
                .id(comment1Id)
                .text("First comment")
                .creationDate(creationDate.plusSeconds(10).toString())
                .build();

        commentDto2 = PostCommentDisplayDTO.builder()
                .id(comment2Id)
                .text("Second comment")
                .creationDate(creationDate.toString())
                .build();

        comments = Arrays.asList(comment1, comment2);
        commentDtos = Arrays.asList(commentDto1, commentDto2);
    }

    @Test
    void getPostByCommunity_positive() {
        when(permissionCheckService.canUserViewPost(userId, postId)).thenReturn(true);

        when(postService.getPostById(postId)).thenReturn(post);

        when(postDisplayMapper.toDTO(post)).thenReturn(postDto);

        when(postCommentService.getCommentsByPostId(postId)).thenReturn(comments);

        when(commentDisplayMapper.toDTO(comment1)).thenReturn(commentDto1);
        when(commentDisplayMapper.toDTO(comment2)).thenReturn(commentDto2);

        ResponseEntity<PostFacadeDTO> resp =
                controller.getPostByCommunity(userId, postId);

        assertEquals(200, resp.getStatusCodeValue());
        assertNotNull(resp.getBody());
        assertEquals(postDto, resp.getBody().getPost());
        assertEquals(commentDtos, resp.getBody().getPostComments());

        verify(permissionCheckService).canUserViewPost(userId, postId);
        verify(postService).getPostById(postId);
        verify(postDisplayMapper).toDTO(post);
        verify(postCommentService).getCommentsByPostId(postId);
        verify(commentDisplayMapper).toDTO(comment1);
        verify(commentDisplayMapper).toDTO(comment2);
    }

    @Test
    void getPostByCommunity_negative() {
        when(permissionCheckService.canUserViewPost(userId, postId)).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> controller.getPostByCommunity(userId, postId));

        verify(permissionCheckService).canUserViewPost(userId, postId);
        verifyNoInteractions(postService);
        verifyNoInteractions(postCommentService);
    }
}
