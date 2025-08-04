package controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import ru.pocgg.SNSApp.controller.rest.UserFacadeRestController;
import ru.pocgg.SNSApp.DTO.display.PostDisplayDTO;
import ru.pocgg.SNSApp.DTO.display.FriendshipDisplayDTO;
import ru.pocgg.SNSApp.DTO.display.UserFacadeDisplayDTO;
import ru.pocgg.SNSApp.model.*;
import ru.pocgg.SNSApp.services.*;
import ru.pocgg.SNSApp.DTO.display.UserDisplayDTO;
import ru.pocgg.SNSApp.DTO.mappers.display.PostDisplayMapper;
import ru.pocgg.SNSApp.DTO.mappers.display.FriendshipDisplayMapper;
import ru.pocgg.SNSApp.DTO.mappers.display.UserDisplayMapper;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserFacadeRestControllerTest {

    @Mock
    private UserService userService;
    @Mock
    private PostService postService;
    @Mock
    private FriendshipService friendshipService;
    @Mock
    private PermissionCheckService permissionCheckService;
    @Mock
    private UserDisplayMapper userDisplayMapper;
    @Mock
    private PostDisplayMapper postDisplayMapper;
    @Mock
    private FriendshipDisplayMapper friendshipDisplayMapper;
    @InjectMocks
    private UserFacadeRestController controller;

    private int currentUserId;
    private int targetUserId;
    private int friendUserId;
    private Instant creationDate;
    private User user;
    private User friend;
    private ru.pocgg.SNSApp.DTO.display.UserDisplayDTO userDto;
    private ru.pocgg.SNSApp.DTO.display.PostDisplayDTO postDto1;
    private ru.pocgg.SNSApp.DTO.display.PostDisplayDTO postDto2;
    private Post post1;
    private Post post2;
    private Friendship friend1;
    private ru.pocgg.SNSApp.DTO.display.FriendshipDisplayDTO friendDto1;

    @BeforeEach
    void setUp() {
        currentUserId = 1;
        targetUserId = 2;
        friendUserId = 3;
        creationDate = Instant.now();

        user = User.builder()
                .userName("a").creationDate(Instant.now())
                .birthDate(LocalDate.now().minusYears(30))
                .password("p").email("a@a")
                .firstName("A").secondName("A")
                .gender(Gender.MALE).systemRole(SystemRole.USER)
                .deleted(false).acceptingPrivateMsgs(true)
                .postsPublic(true).banned(false)
                .build();
        user.setId(targetUserId);

        friend = User.builder()
                .userName("a").creationDate(Instant.now())
                .birthDate(LocalDate.now().minusYears(30))
                .password("s").email("a@a")
                .firstName("A").secondName("A")
                .gender(Gender.MALE).systemRole(SystemRole.USER)
                .deleted(false).acceptingPrivateMsgs(true)
                .postsPublic(true).banned(false)
                .build();
        friend.setId(friendUserId);

        userDto = UserDisplayDTO.builder()
                .id(42)
                .firstName("u")
                .secondName("d")
                .build();

        post1 = Post.userPostBuilder()
                .ownerUser(user)
                .creationDate(creationDate.minusSeconds(15))
                .text("old")
                .build();
        post1.setId(1);

        post2 = Post.userPostBuilder()
                .ownerUser(user)
                .creationDate(creationDate.minusSeconds(10))
                .text("new")
                .build();
        post1.setId(2);

        postDto1 = PostDisplayDTO.builder()
                .id(1)
                .text("old")
                .build();

        postDto2 = PostDisplayDTO.builder()
                .id(2)
                .text("new")
                .build();

        friend1 = Friendship.builder()
                .user(user)
                .friend(friend)
                .creationDate(creationDate)
                .build();

        friendDto1 = FriendshipDisplayDTO.builder()
                .friendId(30)
                .creationDate(creationDate.toString())
                .build();
    }

    @Test
    void getUserFacade_positive() {
        when(permissionCheckService.canViewUserProfile(currentUserId, targetUserId))
                .thenReturn(true);

        when(userService.getUserById(targetUserId))
                .thenReturn(user);

        when(permissionCheckService.canViewPostsAtUser(currentUserId, targetUserId))
                .thenReturn(true);

        when(postService.getPostsByUserOwner(targetUserId))
                .thenReturn(Arrays.asList(post1, post2));

        when(postDisplayMapper.toDTO(post1))
                .thenReturn(postDto1);

        when(postDisplayMapper.toDTO(post2))
                .thenReturn(postDto2);

        when(friendshipService.getUserFriendships(targetUserId))
                .thenReturn(List.of(friend1));

        when(friendshipDisplayMapper.toDTO(friend1, targetUserId))
                .thenReturn(friendDto1);

        when(userDisplayMapper.toDTO(user))
                .thenReturn(userDto);

        ResponseEntity<UserFacadeDisplayDTO> resp =
                controller.getUserFacade(currentUserId, targetUserId);

        assertEquals(200, resp.getStatusCodeValue());
        UserFacadeDisplayDTO body = resp.getBody();
        assertNotNull(body);
        assertEquals(userDto, body.getUser());
        assertEquals(List.of(postDto2, postDto1), body.getPosts());
        assertEquals(List.of(friendDto1), body.getFriends());
    }

    @Test
    void getUserFacade_negative() {
        when(permissionCheckService.canViewUserProfile(currentUserId, targetUserId))
                .thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> controller.getUserFacade(currentUserId, targetUserId));
    }
}
