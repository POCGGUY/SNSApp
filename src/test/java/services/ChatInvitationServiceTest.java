package services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationEventPublisher;
import ru.pocgg.SNSApp.DTO.create.CreateChatInvitationDTO;
import ru.pocgg.SNSApp.model.*;
import ru.pocgg.SNSApp.events.events.*;
import ru.pocgg.SNSApp.model.exceptions.EntityNotFoundException;
import ru.pocgg.SNSApp.services.ChatInvitationService;
import ru.pocgg.SNSApp.services.ChatService;
import ru.pocgg.SNSApp.services.UserService;
import ru.pocgg.SNSApp.services.DAO.interfaces.ChatInvitationServiceDAO;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatInvitationServiceTest {

    @Mock
    private ChatInvitationServiceDAO dao;
    @Mock
    private UserService userService;
    @Mock
    private ChatService chatService;
    @Mock
    private RabbitTemplate rabbitTemplate;
    @InjectMocks
    private ChatInvitationService service;

    private int senderId;
    private int receiverId;
    private int chatId;
    private Instant creationDate;
    private CreateChatInvitationDTO dto;
    private User sender;
    private User receiver;
    private Chat chat;
    private ChatInvitationId invitationId;
    private ChatInvitation invitation;

    @BeforeEach
    void setUp() {
        senderId = 1;
        receiverId = 2;
        chatId = 3;
        creationDate = Instant.now();
        dto = CreateChatInvitationDTO.builder()
                .description("desc")
                .build();

        sender = User.builder()
                .userName("a").creationDate(Instant.now())
                .birthDate(LocalDate.now().minusYears(30))
                .password("p").email("a@a")
                .firstName("A").secondName("A")
                .gender(Gender.MALE).systemRole(SystemRole.USER)
                .deleted(false).acceptingPrivateMsgs(true)
                .postsPublic(true).banned(false)
                .build();
        sender.setId(senderId);

        receiver = User.builder()
                .userName("a").creationDate(Instant.now())
                .birthDate(LocalDate.now().minusYears(30))
                .password("s").email("a@a")
                .firstName("A").secondName("A")
                .gender(Gender.FEMALE).systemRole(SystemRole.USER)
                .deleted(false).acceptingPrivateMsgs(true)
                .postsPublic(true).banned(false)
                .build();
        receiver.setId(receiverId);

        chat = Chat.builder()
                .owner(sender)
                .name("chat")
                .description("d")
                .creationDate(creationDate)
                .isPrivate(false)
                .build();
        chat.setId(chatId);

        invitationId = ChatInvitationId.builder()
                .senderId(senderId)
                .receiverId(receiverId)
                .chatId(chatId)
                .build();

        invitation = ChatInvitation.builder()
                .sender(sender)
                .receiver(receiver)
                .chat(chat)
                .creationDate(creationDate)
                .description(dto.getDescription())
                .build();
    }

    @Test
    void createChatInvitation_positive() {
        when(userService.getUserById(senderId)).thenReturn(sender);
        when(userService.getUserById(receiverId)).thenReturn(receiver);
        when(chatService.getChatById(chatId)).thenReturn(chat);

        ChatInvitation result = service.createChatInvitation(senderId, receiverId, chatId, dto);

        assertNotNull(result);

        verify(dao).addChatInvitation(result);
        verify(dao).forceFlush();
    }

    @Test
    void createChatInvitation_negative() {
        when(userService.getUserById(senderId)).thenThrow(new EntityNotFoundException("no sender"));

        assertThrows(EntityNotFoundException.class,
                () -> service.createChatInvitation(senderId, receiverId, chatId, dto));

        verifyNoInteractions(dao, rabbitTemplate);
    }

    @Test
    void getById_positive() {
        when(dao.getChatInvitationById(invitationId)).thenReturn(invitation);

        ChatInvitation result = service.getById(invitationId);

        assertSame(invitation, result);
    }

    @Test
    void getById_negative() {
        when(dao.getChatInvitationById(invitationId)).thenReturn(null);

        assertThrows(EntityNotFoundException.class,
                () -> service.getById(invitationId));
    }

    @Test
    void getByReceiver_positive() {
        when(dao.getByReceiverId(receiverId)).thenReturn(List.of(invitation));

        List<ChatInvitation> result = service.getByReceiver(receiverId);

        assertSame(invitation, result.get(0));
    }

    @Test
    void getByReceiver_negative() {
        when(dao.getByReceiverId(receiverId)).thenReturn(Collections.emptyList());

        List<ChatInvitation> result = service.getByReceiver(receiverId);

        assertTrue(result.isEmpty());
    }

    @Test
    void getBySender_positive() {
        when(dao.getBySenderId(senderId)).thenReturn(List.of(invitation));

        List<ChatInvitation> result = service.getBySender(senderId);

        assertSame(invitation, result.get(0));
    }

    @Test
    void getBySender_negative() {
        when(dao.getBySenderId(senderId)).thenReturn(Collections.emptyList());

        List<ChatInvitation> result = service.getBySender(senderId);

        assertTrue(result.isEmpty());
    }

    @Test
    void getByChat_positive() {
        when(dao.getByChatId(chatId)).thenReturn(List.of(invitation));

        List<ChatInvitation> result = service.getByChat(chatId);

        assertSame(invitation, result.get(0));
    }

    @Test
    void getByChat_negative() {
        when(dao.getByChatId(chatId)).thenReturn(Collections.emptyList());

        List<ChatInvitation> result = service.getByChat(chatId);

        assertTrue(result.isEmpty());
    }

    @Test
    void getAllChatInvitations_positive() {
        when(dao.getAllChatInvitations()).thenReturn(List.of(invitation));

        List<ChatInvitation> result = service.getAllChatInvitations();

        assertSame(invitation, result.get(0));
    }

    @Test
    void getAllChatInvitations_negative() {
        when(dao.getAllChatInvitations()).thenReturn(Collections.emptyList());

        List<ChatInvitation> result = service.getAllChatInvitations();

        assertTrue(result.isEmpty());
    }

    @Test
    void isInvitationExist_positive() {
        when(dao.getChatInvitationById(invitationId)).thenReturn(invitation);

        assertTrue(service.isInvitationExist(invitationId));
    }

    @Test
    void isInvitationExist_negative() {
        when(dao.getChatInvitationById(invitationId)).thenReturn(null);

        assertFalse(service.isInvitationExist(invitationId));
    }

    @Test
    void removeInvitation_positive() {
        when(dao.getChatInvitationById(invitationId)).thenReturn(invitation);

        service.removeInvitation(invitationId);

        verify(dao).removeChatInvitation(invitation);
    }

    @Test
    void removeInvitation_negative() {
        when(dao.getChatInvitationById(invitationId)).thenReturn(null);

        assertThrows(EntityNotFoundException.class,
                () -> service.removeInvitation(invitationId));
    }

    @Test
    void acceptInvitation_positive() {
        when(dao.getChatInvitationById(invitationId)).thenReturn(invitation);

        service.acceptInvitation(invitationId);

        verify(dao).removeChatInvitation(invitation);
    }

    @Test
    void acceptInvitation_negative() {
        when(dao.getChatInvitationById(invitationId)).thenReturn(null);

        assertThrows(EntityNotFoundException.class,
                () -> service.acceptInvitation(invitationId));
    }

    @Test
    void declineInvitation_positive() {
        when(dao.getChatInvitationById(invitationId)).thenReturn(invitation);

        service.declineInvitation(invitationId);

        verify(dao).removeChatInvitation(invitation);
    }

    @Test
    void declineInvitation_negative() {
        when(dao.getChatInvitationById(invitationId)).thenReturn(null);

        assertThrows(EntityNotFoundException.class,
                () -> service.declineInvitation(invitationId));
    }

    @Test
    void removeBySenderId_positive() {
        service.removeBySenderId(senderId);

        verify(dao).removeBySenderId(senderId);
    }

    @Test
    void removeByReceiverId_positive() {
        service.removeByReceiverId(receiverId);

        verify(dao).removeByReceiverId(receiverId);
    }

    @Test
    void removeByChatId_positive() {
        service.removeByChatId(chatId);

        verify(dao).removeByChatId(chatId);
    }
}
