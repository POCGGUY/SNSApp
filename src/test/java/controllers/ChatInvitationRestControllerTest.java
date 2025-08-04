package controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import ru.pocgg.SNSApp.DTO.create.CreateChatInvitationDTO;
import ru.pocgg.SNSApp.DTO.display.ChatInvitationDisplayDTO;
import ru.pocgg.SNSApp.DTO.mappers.display.ChatInvitationDisplayMapper;
import ru.pocgg.SNSApp.controller.rest.ChatInvitationRestController;
import ru.pocgg.SNSApp.model.*;
import ru.pocgg.SNSApp.model.exceptions.EntityNotFoundException;
import ru.pocgg.SNSApp.services.ChatInvitationService;
import ru.pocgg.SNSApp.services.ChatMemberService;
import ru.pocgg.SNSApp.services.ChatService;
import ru.pocgg.SNSApp.services.UserService;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatInvitationRestControllerTest {

    @Mock
    private ChatInvitationService chatInvitationService;

    @Mock
    private PermissionCheckService permissionCheckService;

    @Mock
    private UserService userService;

    @Mock
    private ChatService chatService;

    @Mock
    private ChatInvitationDisplayMapper chatInvitationDisplayMapper;

    @Mock
    private ChatMemberService chatMemberService;

    @InjectMocks
    private ChatInvitationRestController controller;

    private int senderId;
    private int receiverId;
    private int chatId;
    private Instant creationDate;
    private CreateChatInvitationDTO createDto;
    private ChatInvitation invitation;
    private ChatInvitationDisplayDTO invitationDto;
    private List<ChatInvitation> invitationList;
    private List<ChatInvitationDisplayDTO> dtoList;

    @BeforeEach
    void setUp() {
        senderId = 1;
        receiverId = 2;
        chatId = 3;
        creationDate = Instant.now();

        createDto = CreateChatInvitationDTO.builder()
                .description("desc")
                .build();

        User sender = User.builder().userName("r").creationDate(Instant.now())
                .birthDate(LocalDate.now().minusYears(30))
                .password("p").email("r@e").firstName("R").secondName("R")
                .gender(null).systemRole(null)
                .deleted(false).acceptingPrivateMsgs(true)
                .postsPublic(true).banned(false)
                .build();
        sender.setId(senderId);

        User receiver = User.builder()
                .userName("a").creationDate(Instant.now())
                .birthDate(LocalDate.now().minusYears(30))
                .password("p").email("a@a")
                .firstName("A").secondName("A")
                .gender(Gender.MALE).systemRole(SystemRole.USER)
                .deleted(false).acceptingPrivateMsgs(true)
                .postsPublic(true).banned(false)
                .build();
        receiver.setId(receiverId);

        Chat chat = Chat.builder()
                .name("ChatName")
                .creationDate(creationDate)
                .deleted(false)
                .isPrivate(true)
                .build();
        chat.setId(chatId);

        invitation = ChatInvitation.builder()
                .sender(sender)
                .receiver(receiver)
                .chat(chat)
                .creationDate(creationDate)
                .build();

        invitationDto = ChatInvitationDisplayDTO.builder()
                .senderId(senderId)
                .receiverId(receiverId)
                .chatId(chatId)
                .creationDate(creationDate.toString())
                .description("test")
                .build();

        invitationList = Arrays.asList(invitation);
        dtoList = Arrays.asList(invitationDto);
    }

    @Test
    void create_positive() {
        when(permissionCheckService.isUserChatOwner(senderId, chatId)).thenReturn(true);
        when(permissionCheckService.isUserChatMember(receiverId, chatId)).thenReturn(false);
        when(chatInvitationService.isInvitationExist(invitation.getId())).thenReturn(false);
        when(chatService.getChatById(chatId)).thenReturn(invitation.getChat());
        when(permissionCheckService.isUserActive(receiverId)).thenReturn(true);
        when(userService.getUserById(receiverId)).thenReturn(invitation.getReceiver());
        when(chatInvitationService.createChatInvitation(senderId, receiverId, chatId, createDto))
                .thenReturn(invitation);
        when(chatInvitationDisplayMapper.toDTO(invitation)).thenReturn(invitationDto);

        ResponseEntity<ChatInvitationDisplayDTO> resp =
                controller.create(senderId, chatId, receiverId, createDto);

        assertEquals(201, resp.getStatusCodeValue());
        assertEquals(invitationDto, resp.getBody());
    }

    @Test
    void create_negative_notOwner() {
        when(permissionCheckService.isUserChatOwner(senderId, chatId)).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> controller.create(senderId, chatId, receiverId, createDto));
    }

    @Test
    void sent_positive() {
        when(chatInvitationService.getBySender(senderId)).thenReturn(invitationList);
        when(chatInvitationDisplayMapper.toDTO(invitation)).thenReturn(invitationDto);

        ResponseEntity<List<ChatInvitationDisplayDTO>> resp =
                controller.sent(senderId);

        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(dtoList, resp.getBody());
    }

    @Test
    void sent_negative() {
        when(chatInvitationService.getBySender(senderId))
                .thenThrow(new RuntimeException("DB error"));

        assertThrows(RuntimeException.class,
                () -> controller.sent(senderId));
    }

    @Test
    void received_positive() {
        when(chatInvitationService.getByReceiver(receiverId)).thenReturn(invitationList);
        when(chatInvitationDisplayMapper.toDTO(invitation)).thenReturn(invitationDto);

        ResponseEntity<List<ChatInvitationDisplayDTO>> resp =
                controller.received(receiverId);

        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(dtoList, resp.getBody());
    }

    @Test
    void received_negative() {
        when(chatInvitationService.getByReceiver(receiverId))
                .thenThrow(new RuntimeException("DB error"));

        assertThrows(RuntimeException.class,
                () -> controller.received(receiverId));
    }

    @Test
    void byChat_positive() {
        when(permissionCheckService.canUserViewInvitationsInChat(senderId, chatId)).thenReturn(true);
        when(chatInvitationService.getByChat(chatId)).thenReturn(invitationList);
        when(chatInvitationDisplayMapper.toDTO(invitation)).thenReturn(invitationDto);

        ResponseEntity<List<ChatInvitationDisplayDTO>> resp =
                controller.byChat(senderId, chatId);

        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(dtoList, resp.getBody());
    }

    @Test
    void byChat_negative() {
        when(permissionCheckService.canUserViewInvitationsInChat(senderId, chatId)).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> controller.byChat(senderId, chatId));
    }

    @Test
    void delete_positive() {
        doNothing().when(chatInvitationService)
                .removeInvitation(invitation.getId());

        ResponseEntity<Void> resp =
                controller.delete(senderId, chatId, receiverId);

        assertEquals(204, resp.getStatusCodeValue());
    }

    @Test
    void delete_negative() {
        doThrow(new EntityNotFoundException("not found")).when(chatInvitationService)
                .removeInvitation(invitation.getId());

        assertThrows(EntityNotFoundException.class,
                () -> controller.delete(senderId, chatId, receiverId));
    }

    @Test
    void accept_positive() {
        doNothing().when(chatInvitationService).acceptInvitation(invitation.getId());

        ResponseEntity<Void> resp =
                controller.accept(receiverId, chatId, senderId);

        assertEquals(204, resp.getStatusCodeValue());
    }

    @Test
    void accept_negative() {
        doThrow(new RuntimeException("fail")).when(chatInvitationService)
                .acceptInvitation(invitation.getId());

        assertThrows(RuntimeException.class,
                () -> controller.accept(receiverId, chatId, senderId));
    }

    @Test
    void decline_positive() {
        doNothing().when(chatInvitationService).declineInvitation(invitation.getId());

        ResponseEntity<Void> resp =
                controller.decline(receiverId, chatId, senderId);

        assertEquals(204, resp.getStatusCodeValue());
    }

    @Test
    void decline_negative() {
        doThrow(new RuntimeException("fail")).when(chatInvitationService)
                .declineInvitation(invitation.getId());

        assertThrows(RuntimeException.class,
                () -> controller.decline(receiverId, chatId, senderId));
    }
}
