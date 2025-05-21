package services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.pocgg.SNSApp.DTO.create.CreateChatDTO;
import ru.pocgg.SNSApp.DTO.update.UpdateChatDTO;
import ru.pocgg.SNSApp.model.Chat;
import ru.pocgg.SNSApp.model.Gender;
import ru.pocgg.SNSApp.model.SystemRole;
import ru.pocgg.SNSApp.model.User;
import ru.pocgg.SNSApp.events.events.ChatBecamePublicEvent;
import ru.pocgg.SNSApp.events.events.ChatCreatedEvent;
import ru.pocgg.SNSApp.events.events.ChatDeactivatedEvent;
import ru.pocgg.SNSApp.model.exceptions.EntityNotFoundException;
import ru.pocgg.SNSApp.services.ChatService;
import ru.pocgg.SNSApp.services.DAO.interfaces.ChatMemberServiceDAO;
import ru.pocgg.SNSApp.services.DAO.interfaces.ChatServiceDAO;
import ru.pocgg.SNSApp.services.UserService;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatServiceDAO chatServiceDAO;
    @Mock
    private ChatMemberServiceDAO chatMemberServiceDAO;
    @Mock
    private UserService userService;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @InjectMocks
    private ChatService service;

    private int ownerId;
    private int chatId;
    private Chat chat;
    private User user;
    private CreateChatDTO createDto;
    private UpdateChatDTO updateDto;

    @BeforeEach
    void setUp() {
        ownerId = 1;
        chatId = 2;
        user = User.builder()
                .userName("a").creationDate(Instant.now())
                .birthDate(LocalDate.now().minusYears(30))
                .password("p").email("a@a")
                .firstName("A").secondName("A")
                .gender(Gender.MALE).systemRole(SystemRole.USER)
                .deleted(false).acceptingPrivateMsgs(true)
                .postsPublic(true).banned(false)
                .build();
        chat = Chat.builder()
                .owner(null)
                .name("oldName")
                .description("oldDesc")
                .creationDate(Instant.now())
                .isPrivate(true)
                .build();
        chat.setId(chatId);
        createDto = CreateChatDTO.builder().name("newName").description("newDesc").isPrivate(false).build();
        updateDto = UpdateChatDTO.builder().name("updName").description("updDesc").isPrivate(false).build();
    }

    @Test
    void createChat_positive() {
        when(userService.getUserById(ownerId)).thenReturn(user);
        Chat result = service.createChat(ownerId, createDto);

        assertNotNull(result);
        assertEquals("newName", result.getName());
        assertEquals("newDesc", result.getDescription());
        assertFalse(result.isPrivate());
        verify(chatServiceDAO).addChat(result);
        verify(chatServiceDAO).forceFlush();
        verify(eventPublisher).publishEvent(any(ChatCreatedEvent.class));
    }

    @Test
    void createChat_negative() {
        when(userService.getUserById(ownerId)).thenThrow(new EntityNotFoundException("no user"));

        assertThrows(EntityNotFoundException.class, () -> service.createChat(ownerId, createDto));
        verifyNoInteractions(chatServiceDAO, eventPublisher);
    }

    @Test
    void updateChat_positive() {
        when(chatServiceDAO.getChatById(chatId)).thenReturn(chat);

        service.updateChat(chatId, updateDto);

        assertEquals("updName", chat.getName());
        assertEquals("updDesc", chat.getDescription());
        assertFalse(chat.isPrivate());
        verify(eventPublisher).publishEvent(any(ChatBecamePublicEvent.class));
    }

    @Test
    void updateChat_negative() {
        when(chatServiceDAO.getChatById(chatId)).thenReturn(null);

        assertThrows(EntityNotFoundException.class, () -> service.updateChat(chatId, updateDto));
    }

    @Test
    void getAllChats_positive() {
        List<Chat> list = List.of(chat);
        when(chatServiceDAO.getAllChats()).thenReturn(list);

        List<Chat> result = service.getAllChats();

        assertSame(list, result);
    }

    @Test
    void getAllChats_negative() {
        when(chatServiceDAO.getAllChats()).thenReturn(Collections.emptyList());

        List<Chat> result = service.getAllChats();

        assertTrue(result.isEmpty());
    }

    @Test
    void getChatById_positive() {
        when(chatServiceDAO.getChatById(chatId)).thenReturn(chat);

        Chat result = service.getChatById(chatId);

        assertSame(chat, result);
    }

    @Test
    void getChatById_negative() {
        when(chatServiceDAO.getChatById(chatId)).thenReturn(null);

        assertThrows(EntityNotFoundException.class, () -> service.getChatById(chatId));
    }

    @Test
    void getChatsByMemberId_positive() {
        List<Chat> list = List.of(chat);
        when(chatMemberServiceDAO.getChatsByMemberId(ownerId)).thenReturn(list);

        List<Chat> result = service.getChatsByMemberId(ownerId);

        assertSame(list, result);
    }

    @Test
    void getChatsByMemberId_negative() {
        when(chatMemberServiceDAO.getChatsByMemberId(ownerId)).thenReturn(Collections.emptyList());

        List<Chat> result = service.getChatsByMemberId(ownerId);

        assertTrue(result.isEmpty());
    }

    @Test
    void setDeleted_positive() {
        chat.setDeleted(false);
        when(chatServiceDAO.getChatById(chatId)).thenReturn(chat);

        service.setDeleted(chatId, true);

        assertTrue(chat.isDeleted());
        verify(eventPublisher).publishEvent(any(ChatDeactivatedEvent.class));
    }

    @Test
    void setDeleted_negative() {
        when(chatServiceDAO.getChatById(chatId)).thenReturn(null);
        assertThrows(EntityNotFoundException.class, () -> service.setDeleted(chatId, true));
    }
}
