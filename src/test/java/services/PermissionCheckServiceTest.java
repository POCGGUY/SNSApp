package services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.pocgg.SNSApp.model.*;
import ru.pocgg.SNSApp.services.*;

import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PermissionCheckServiceTest {

    @InjectMocks
    private PermissionCheckService permissionCheckService;

    @Mock
    private ChatMemberService chatMemberService;
    @Mock
    private CommunityMemberService communityMemberService;
    @Mock
    private ChatMessageService chatMessageService;
    @Mock
    private CommunityInvitationService communityInvitationService;
    @Mock
    private UserService userService;
    @Mock
    private ChatService chatService;
    @Mock
    private CommunityService communityService;
    @Mock
    private FriendshipService friendshipService;
    @Mock
    private PrivateMessageService privateMessageService;
    @Mock
    private PostService postService;
    @Mock
    private PostCommentService postCommentService;

    private User user;
    private Chat chat;
    private Community community;
    private ChatMessage chatMessage;
    private Post post;
    private PostComment comment;
    private PrivateMessage privateMessage;
    private CommunityInvitation invitation;

    @BeforeEach
    void init() {
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

        chat = Chat.builder()
                .owner(user).name("chat").description("d")
                .creationDate(Instant.now()).deleted(false).isPrivate(false)
                .build();
        chat.setId(2);

        community = Community.builder()
                .owner(user).communityName("comm")
                .description("desc").creationDate(Instant.now())
                .isPrivate(false).deleted(false).banned(false)
                .build();
        community.setId(3);

        chatMessage = ChatMessage.builder()
                .chat(chat).sender(user)
                .text("hi").sendingDate(Instant.now())
                .updateDate(Instant.now()).deleted(false)
                .build();
        chatMessage.setId(4);

        post = Post.userPostBuilder()
                .ownerUser(user).author(user)
                .text("text").creationDate(Instant.now())
                .deleted(false).updateDate(Instant.now())
                .build();
        post.setId(5);

        comment = PostComment.builder()
                .post(post).author(user)
                .text("c").creationDate(Instant.now())
                .updateDate(Instant.now()).deleted(false)
                .build();
        comment.setId(6);

        privateMessage = PrivateMessage.builder()
                .sender(user).receiver(user)
                .text("m").creationDate(Instant.now())
                .updateDate(Instant.now()).deleted(false)
                .build();
        privateMessage.setId(7);

        invitation = CommunityInvitation.builder()
                .sender(user).receiver(user)
                .community(community).creationDate(Instant.now())
                .description("inv").build();
        invitation.setId(new CommunityInvitationId(1, 1, 3));

        lenient().when(userService.getUserById(anyInt())).thenReturn(user);
        lenient().when(chatService.getChatById(anyInt())).thenReturn(chat);
        lenient().when(communityService.getCommunityById(anyInt())).thenReturn(community);
        lenient().when(chatMessageService.getChatMessageById(anyInt())).thenReturn(chatMessage);
        lenient().when(postService.getPostById(anyInt())).thenReturn(post);
        lenient().when(postCommentService.getCommentById(anyInt())).thenReturn(comment);
        lenient().when(privateMessageService.getById(anyInt())).thenReturn(privateMessage);
        lenient().when(communityInvitationService.getInvitationById(any(CommunityInvitationId.class)))
                .thenReturn(invitation);
    }

    @Test
    void isUserChatMember_true() {
        when(chatMemberService.isChatMemberExist(new ChatMemberId(2, 1))).thenReturn(true);

        assertTrue(permissionCheckService.isUserChatMember(1, 2));
    }

    @Test
    void isUserChatMember_negative() {
        when(chatMemberService.isChatMemberExist(any())).thenReturn(false);

        assertFalse(permissionCheckService.isUserChatMember(1, 2));
    }

    @Test
    void canUserModifyChatMessage_positive() {
        when(chatMessageService.getChatMessageById(4)).thenReturn(chatMessage);
        when(chatMemberService.isChatMemberExist(new ChatMemberId(2, 1))).thenReturn(true);

        assertTrue(permissionCheckService.canUserModifyChatMessage(1, 4));
    }

    @Test
    void canUserModifyChatMessage_negative() {
        when(chatMessageService.getChatMessageById(4)).thenReturn(chatMessage);
        when(chatMemberService.isChatMemberExist(any())).thenReturn(false);

        assertFalse(permissionCheckService.canUserModifyChatMessage(1, 4));
    }

    @Test
    void canUserCreateUserPost_positive() {
        when(userService.getUserById(1)).thenReturn(user);

        assertTrue(permissionCheckService.canUserCreateUserPost(1, 1));
    }

    @Test
    void canUserCreateUserPost_negative() {
        user.setPostsPublic(false);

        when(userService.getUserById(2)).thenReturn(user);
        when(friendshipService.isFriendshipExist(1, 2)).thenReturn(false);

        assertFalse(permissionCheckService.canUserCreateUserPost(1, 2));
    }

    @Test
    void canUserDeletePost_positive() {
        when(postService.getPostById(5)).thenReturn(post);

        assertTrue(permissionCheckService.canUserDeletePost(1, 5));
    }

    @Test
    void canUserDeletePost_negative() {
        post.setOwnerCommunity(community);

        when(postService.getPostById(5)).thenReturn(post);

        assertFalse(permissionCheckService.canUserDeletePost(2, 5));
    }

    @Test
    void canUserDeletePostComment_positive() {
        when(postCommentService.getCommentById(6)).thenReturn(comment);
        when(postService.getPostById(5)).thenReturn(post);

        assertTrue(permissionCheckService.canUserDeletePostComment(1, 6));
    }

    @Test
    void canUserDeletePostComment_negative() {
        when(postCommentService.getCommentById(6)).thenReturn(comment);
        when(postService.getPostById(5)).thenReturn(post);

        assertFalse(permissionCheckService.canUserDeletePostComment(2, 6));
    }

    @Test
    void isUserPostOwner_positive() {
        assertTrue(permissionCheckService.isUserPostOwner(1, post));
    }

    @Test
    void isUserPostOwner_negative() {
        assertFalse(permissionCheckService.isUserPostOwner(2, post));
    }

    @Test
    void canUserViewPost_positive() {
        when(postService.getPostById(5)).thenReturn(post);

        assertTrue(permissionCheckService.canUserViewPost(1, 5));
    }

    @Test
    void canUserViewPost_negative() {
        post.setDeleted(true);

        when(postService.getPostById(5)).thenReturn(post);

        assertFalse(permissionCheckService.canUserViewPost(1, 5));
    }

    @Test
    void canUserModifyPostComment_positive() {
        when(postCommentService.getCommentById(6)).thenReturn(comment);
        when(postService.getPostById(5)).thenReturn(post);

        assertTrue(permissionCheckService.canUserModifyPostComment(1, 6));
    }

    @Test
    void canUserModifyPostComment_negative() {
        when(postCommentService.getCommentById(6)).thenReturn(comment);

        assertFalse(permissionCheckService.canUserModifyPostComment(2, 6));
    }

    @Test
    void canViewPostsAtUser_positive() {
        when(userService.getUserById(1)).thenReturn(user);

        assertTrue(permissionCheckService.canViewPostsAtUser(1, 1));
    }

    @Test
    void canViewPostsAtUser_negative() {
        user.setDeleted(true);

        when(userService.getUserById(2)).thenReturn(user);

        assertFalse(permissionCheckService.canViewPostsAtUser(1, 2));
    }

    @Test
    void canViewPostsAtCommunity_positive() {
        when(communityService.getCommunityById(3)).thenReturn(community);

        assertTrue(permissionCheckService.canViewPostsAtCommunity(1, 3));
    }

    @Test
    void canViewPostsAtCommunity_negative() {
        community.setIsPrivate(true);

        when(communityService.getCommunityById(3)).thenReturn(community);
        when(communityMemberService.isMemberExist(new CommunityMemberId(3, 1))).thenReturn(false);

        assertFalse(permissionCheckService.canViewPostsAtCommunity(1, 3));
    }

    @Test
    void canUserCreateCommunityPost_positive() {
        community.setDeleted(false);

        when(communityMemberService.isMemberExist(new CommunityMemberId(3, 1))).thenReturn(true);
        when(communityMemberService.getMemberById(new CommunityMemberId(3, 1)))
                .thenReturn(new CommunityMember(community, user, Instant.now(), CommunityRole.OWNER));

        assertTrue(permissionCheckService.canUserCreateCommunityPost(1, 3));
    }

    @Test
    void canUserCreateCommunityPost_negative() {
        when(communityMemberService.isMemberExist(new CommunityMemberId(3, 1))).thenReturn(false);

        assertFalse(permissionCheckService.canUserCreateCommunityPost(1, 3));
    }

    @Test
    void canUserModifyPost_positive() {
        when(postService.getPostById(5)).thenReturn(post);

        assertTrue(permissionCheckService.canUserModifyPost(1, 5));
    }

    @Test
    void canUserModifyPost_negative() {
        when(postService.getPostById(5)).thenReturn(post);

        assertFalse(permissionCheckService.canUserModifyPost(2, 5));
    }

    @Test
    void canUserModifyPrivateMessage_positive() {
        when(privateMessageService.getById(7)).thenReturn(privateMessage);

        assertTrue(permissionCheckService.canUserModifyPrivateMessage(1, 7));
    }

    @Test
    void canUserModifyPrivateMessage_negative() {
        when(privateMessageService.getById(7)).thenReturn(privateMessage);

        assertFalse(permissionCheckService.canUserModifyPrivateMessage(2, 7));
    }

    @Test
    void canUserDeleteCommunityInvitation_positive() {
        when(communityInvitationService.getInvitationById(invitation.getId()))
                .thenReturn(invitation);

        assertTrue(permissionCheckService.canUserDeleteCommunityInvitation(1, invitation.getId()));
    }

    @Test
    void canUserDeleteCommunityInvitation_negative() {
        when(communityInvitationService.getInvitationById(invitation.getId()))
                .thenReturn(invitation);

        assertFalse(permissionCheckService.canUserDeleteCommunityInvitation(2, invitation.getId()));
    }

    @Test
    void canUserReadPrivateMessage_positive() {
        when(privateMessageService.getById(7)).thenReturn(privateMessage);

        assertTrue(permissionCheckService.canUserReadPrivateMessage(1, 7));
        assertTrue(permissionCheckService.canUserReadPrivateMessage(1, 7));
    }

    @Test
    void canUserReadPrivateMessage_negative() {
        when(privateMessageService.getById(7)).thenReturn(privateMessage);

        assertFalse(permissionCheckService.canUserReadPrivateMessage(2, 7));
    }

    @Test
    void canUserDeletePrivateMessage_positive() {
        when(privateMessageService.getById(7)).thenReturn(privateMessage);

        assertTrue(permissionCheckService.canUserDeletePrivateMessage(1, 7));

        user.setSystemRole(SystemRole.MODERATOR);

        when(userService.getUserById(2)).thenReturn(user);

        assertTrue(permissionCheckService.canUserDeletePrivateMessage(2, 7));
    }

    @Test
    void canUserDeletePrivateMessage_negative() {
        when(privateMessageService.getById(7)).thenReturn(privateMessage);

        assertFalse(permissionCheckService.canUserDeletePrivateMessage(2, 7));
    }

    @Test
    void isUserSystemModerator_true() {
        user.setSystemRole(SystemRole.ADMIN);

        when(userService.getUserById(1)).thenReturn(user);

        assertTrue(permissionCheckService.isUserSystemModerator(1));
    }

    @Test
    void isUserSystemModerator_negative() {
        user.setSystemRole(SystemRole.USER);

        when(userService.getUserById(1)).thenReturn(user);

        assertFalse(permissionCheckService.isUserSystemModerator(1));
    }

    @Test
    void isChatOwnerOrMod_positive() {
        when(chatService.getChatById(2)).thenReturn(chat);

        assertTrue(permissionCheckService.isUserChatOwnerOrSystemModerator(1, 2));
    }

    @Test
    void isChatOwnerOrMod_negative() {
        when(chatService.getChatById(2)).thenReturn(chat);

        assertFalse(permissionCheckService.isUserChatOwnerOrSystemModerator(2, 2));
    }

    @Test
    void isChatOwner_positive() {
        when(chatService.getChatById(2)).thenReturn(chat);

        assertTrue(permissionCheckService.isUserChatOwner(1, 2));
    }

    @Test
    void isChatOwner_negative() {
        when(chatService.getChatById(2)).thenReturn(chat);

        assertFalse(permissionCheckService.isUserChatOwner(2, 2));
    }

    @Test
    void canSendMessageToThisUser_positive() {
        user.setAcceptingPrivateMsgs(false);

        when(userService.getUserById(1)).thenReturn(user);
        when(friendshipService.isFriendshipExist(2, 1)).thenReturn(true);

        assertTrue(permissionCheckService.canSendMessageToThisUser(2, 1));
    }

    @Test
    void canSendMessageToThisUser_negative() {
        user.setAcceptingPrivateMsgs(false);

        when(userService.getUserById(1)).thenReturn(user);
        when(friendshipService.isFriendshipExist(2, 1)).thenReturn(false);

        assertFalse(permissionCheckService.canSendMessageToThisUser(2, 1));
    }

    @Test
    void isUserActive_positive() {
        when(userService.getUserById(1)).thenReturn(user);

        assertTrue(permissionCheckService.isUserActive(1));
    }

    @Test
    void isUserActive_negative() {
        user.setBanned(true);

        when(userService.getUserById(1)).thenReturn(user);

        assertFalse(permissionCheckService.isUserActive(1));
    }

    @Test
    void isCommunityActive_positive() {
        when(communityService.getCommunityById(3)).thenReturn(community);

        assertTrue(permissionCheckService.isCommunityActive(3));
    }

    @Test
    void isCommunityActive_negative() {
        community.setBanned(true);

        when(communityService.getCommunityById(3)).thenReturn(community);

        assertFalse(permissionCheckService.isCommunityActive(3));
    }

    @Test
    void isChatActive_positive() {
        when(chatService.getChatById(2)).thenReturn(chat);

        assertTrue(permissionCheckService.isChatActive(2));
    }

    @Test
    void isChatActive_negative() {
        chat.setDeleted(true);

        when(chatService.getChatById(2)).thenReturn(chat);

        assertFalse(permissionCheckService.isChatActive(2));
    }
}
