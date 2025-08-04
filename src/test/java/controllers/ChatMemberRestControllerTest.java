package controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import ru.pocgg.SNSApp.DTO.display.ChatMemberDisplayDTO;
import ru.pocgg.SNSApp.DTO.mappers.display.ChatMemberDisplayMapper;
import ru.pocgg.SNSApp.controller.rest.ChatMemberRestController;
import ru.pocgg.SNSApp.model.Chat;
import ru.pocgg.SNSApp.model.ChatMember;
import ru.pocgg.SNSApp.model.ChatMemberId;
import ru.pocgg.SNSApp.model.User;
import ru.pocgg.SNSApp.model.exceptions.EntityNotFoundException;
import ru.pocgg.SNSApp.services.ChatMemberService;
import ru.pocgg.SNSApp.services.ChatService;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatMemberRestControllerTest {

    @Mock
    private ChatMemberService chatMemberService;

    @Mock
    private ChatMemberDisplayMapper chatMemberDisplayMapper;

    @Mock
    private PermissionCheckService permissionCheckService;

    @Mock
    private ChatService chatService;

    @InjectMocks
    private ChatMemberRestController controller;

    private int userId;
    private int chatId;
    private int memberId;
    private Instant entryDate;
    private Chat chat;
    private ChatMember member;
    private ChatMemberDisplayDTO memberDto;
    private List<ChatMember> memberList;
    private List<ChatMemberDisplayDTO> dtoList;

    @BeforeEach
    void setUp() {
        userId = 1;
        memberId = userId;
        chatId = 2;
        entryDate = Instant.now();

        User memberUser = User.builder().userName("r").creationDate(Instant.now())
                .birthDate(LocalDate.now().minusYears(30))
                .password("p").email("r@e").firstName("R").secondName("R")
                .gender(null).systemRole(null)
                .deleted(false).acceptingPrivateMsgs(true)
                .postsPublic(true).banned(false)
                .build();
        memberUser.setId(memberId);

        chat = Chat.builder()
                .deleted(false)
                .isPrivate(false)
                .owner(null)
                .build();
        chat.setId(chatId);

        member = ChatMember.builder()
                .chat(chat)
                .member(memberUser)
                .entryDate(entryDate)
                .build();

        memberDto = ChatMemberDisplayDTO.builder()
                .chatId(chatId)
                .memberId(memberId)
                .entryDate(entryDate.toString())
                .build();

        memberList = Arrays.asList(member);
        dtoList = Arrays.asList(memberDto);
    }

    @Test
    void addMember_positive() {
        when(permissionCheckService.isUserChatMember(userId, chatId)).thenReturn(false);

        when(chatService.getChatById(chatId)).thenReturn(chat);

        when(chatMemberService.createChatMember(eq(chatId), eq(userId), any()))
                .thenReturn(member);

        when(chatMemberDisplayMapper.toDTO(member)).thenReturn(memberDto);

        ResponseEntity<ChatMemberDisplayDTO> resp =
                controller.addMember(userId, chatId);

        assertEquals(201, resp.getStatusCodeValue());
        assertEquals(memberDto, resp.getBody());
    }

    @Test
    void addMember_negative() {
        when(permissionCheckService.isUserChatMember(userId, chatId)).thenReturn(true);

        assertThrows(AccessDeniedException.class,
                () -> controller.addMember(userId, chatId));
    }

    @Test
    void listMembers_positive() {
        when(permissionCheckService.isUserChatMember(userId, chatId)).thenReturn(true);

        when(chatMemberService.getChatMembersByChatId(chatId)).thenReturn(memberList);

        when(chatMemberDisplayMapper.toDTO(member)).thenReturn(memberDto);

        ResponseEntity<List<ChatMemberDisplayDTO>> resp =
                controller.listMembers(userId, chatId);

        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(dtoList, resp.getBody());
    }

    @Test
    void listMembers_negative() {
        when(permissionCheckService.isUserChatMember(userId, chatId)).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> controller.listMembers(userId, chatId));
    }

    @Test
    void getMember_positive() {
        when(permissionCheckService.isUserChatMember(userId, chatId)).thenReturn(true);

        when(chatMemberService.getChatMemberById(new ChatMemberId(chatId, memberId)))
                .thenReturn(member);

        when(chatMemberDisplayMapper.toDTO(member)).thenReturn(memberDto);

        ResponseEntity<ChatMemberDisplayDTO> resp =
                controller.getMember(userId, chatId, memberId);

        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(memberDto, resp.getBody());
    }

    @Test
    void getMember_negative_notFound() {
        when(permissionCheckService.isUserChatMember(userId, chatId)).thenReturn(true);

        when(chatMemberService.getChatMemberById(new ChatMemberId(chatId, memberId)))
                .thenThrow(new EntityNotFoundException("not found"));

        assertThrows(EntityNotFoundException.class,
                () -> controller.getMember(userId, chatId, memberId));
    }

    @Test
    void removeMember_positive() {
        when(permissionCheckService.isUserChatOwnerOrSystemModerator(userId, chatId)).thenReturn(true);

        doNothing().when(chatMemberService).deleteChatMember(new ChatMemberId(chatId, memberId));

        ResponseEntity<Void> resp =
                controller.removeMember(userId, chatId, memberId);

        assertEquals(204, resp.getStatusCodeValue());
    }

    @Test
    void removeMember_negative() {
        when(permissionCheckService.isUserChatOwnerOrSystemModerator(userId, chatId)).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> controller.removeMember(userId, chatId, memberId));
    }

    @Test
    void leaveChat_positive() {
        when(permissionCheckService.isUserChatMember(userId, chatId)).thenReturn(true);

        doNothing().when(chatMemberService).deleteChatMember(new ChatMemberId(chatId, userId));

        ResponseEntity<Void> resp =
                controller.leaveChat(userId, chatId);

        assertEquals(204, resp.getStatusCodeValue());
    }

    @Test
    void leaveChat_negative() {
        when(permissionCheckService.isUserChatMember(userId, chatId)).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> controller.leaveChat(userId, chatId));
    }
}
