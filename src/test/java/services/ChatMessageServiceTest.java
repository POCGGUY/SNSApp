package services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.pocgg.SNSApp.DTO.create.CreateChatMessageDTO;
import ru.pocgg.SNSApp.DTO.mappers.update.UpdateChatMessageMapper;
import ru.pocgg.SNSApp.DTO.update.UpdateChatMessageDTO;
import ru.pocgg.SNSApp.model.*;
import ru.pocgg.SNSApp.model.exceptions.EntityNotFoundException;
import ru.pocgg.SNSApp.services.ChatMessageService;
import ru.pocgg.SNSApp.services.DAO.interfaces.ChatMessageServiceDAO;
import ru.pocgg.SNSApp.services.ChatService;
import ru.pocgg.SNSApp.services.UserService;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatMessageServiceTest {

    @Mock
    private ChatMessageServiceDAO dao;
    @Mock
    private ChatService chatService;
    @Mock
    private UserService userService;
    @Mock
    private UpdateChatMessageMapper updateChatMessageMapper;
    @InjectMocks
    private ChatMessageService service;

    private int chatId;
    private int senderId;
    private Instant now;
    private Chat chat;
    private User user;
    private CreateChatMessageDTO createDto;
    private UpdateChatMessageDTO updateDto;
    private ChatMessage message;

    @BeforeEach
    void setUp() {
        chatId = 1;
        senderId = 2;

        now = Instant.now();
        user = User.builder()
                .userName("a").creationDate(Instant.now())
                .birthDate(LocalDate.now().minusYears(30))
                .password("p").email("a@a")
                .firstName("A").secondName("A")
                .gender(Gender.MALE).systemRole(SystemRole.USER)
                .deleted(false).acceptingPrivateMsgs(true)
                .postsPublic(true).banned(false)
                .build();
        user.setId(senderId);

        chat = Chat.builder()
                .owner(null)
                .name("oldName")
                .description("oldDesc")
                .creationDate(Instant.now())
                .isPrivate(true)
                .build();
        chat.setId(chatId);

        createDto = CreateChatMessageDTO.builder()
                .text("hello")
                .build();
        updateDto = UpdateChatMessageDTO.builder().text("updated").build();

        message = ChatMessage.builder()
                .sender(user)
                .chat(chat)
                .sendingDate(now)
                .updateDate(now)
                .deleted(false)
                .text("hello")
                .build();
        message.setId(100);
    }

    @Test
    void createChatMessage_positive() {
        when(chatService.getChatById(chatId)).thenReturn(chat);
        when(userService.getUserById(senderId)).thenReturn(user);

        ChatMessage result = service.createChatMessage(chatId, senderId, createDto);

        assertNotNull(result);
        assertEquals("hello", result.getText());
        assertEquals(chat, result.getChat());
        assertEquals(user, result.getSender());
        verify(dao).addChatMessage(result);
        verify(dao).forceFlush();
    }

    @Test
    void createChatMessage_negative() {
        when(chatService.getChatById(chatId)).thenThrow(new EntityNotFoundException("no chat"));

        assertThrows(EntityNotFoundException.class,
                () -> service.createChatMessage(chatId, senderId, createDto));
        verifyNoInteractions(dao);
    }

    @Test
    void getMessagesByChatId_positive() {
        when(dao.getMessagesByChatId(chatId)).thenReturn(List.of(message));
        List<ChatMessage> result = service.getMessagesByChatId(chatId);

        assertSame(message, result.get(0));
    }

    @Test
    void getMessagesByChatId_negative() {
        when(dao.getMessagesByChatId(chatId)).thenReturn(Collections.emptyList());
        List<ChatMessage> result = service.getMessagesByChatId(chatId);

        assertTrue(result.isEmpty());
    }

    @Test
    void getMessagesByChatIdAndSenderId_positive() {
        when(dao.getMessagesByChatIdAndSenderId(chatId, senderId)).thenReturn(List.of(message));
        List<ChatMessage> result = service.getMessagesByChatIdAndSenderId(chatId, senderId);

        assertSame(message, result.get(0));
    }

    @Test
    void getMessagesByChatIdAndSenderId_negative() {
        when(dao.getMessagesByChatIdAndSenderId(chatId, senderId)).thenReturn(Collections.emptyList());
        List<ChatMessage> result = service.getMessagesByChatIdAndSenderId(chatId, senderId);

        assertTrue(result.isEmpty());
    }

    @Test
    void getChatMessageById_positive() {
        when(dao.getChatMessageById(100)).thenReturn(message);
        ChatMessage result = service.getChatMessageById(100);
        assertSame(message, result);
    }

    @Test
    void getChatMessageById_negative() {
        when(dao.getChatMessageById(100)).thenReturn(null);
        assertThrows(EntityNotFoundException.class, () -> service.getChatMessageById(100));
    }

    @Test
    void setDeleted_positive() {
        when(dao.getChatMessageById(100)).thenReturn(message);
        service.setDeleted(100, true);
        assertTrue(message.getDeleted());
    }

    @Test
    void setDeleted_negative() {
        when(dao.getChatMessageById(100)).thenReturn(null);
        assertThrows(EntityNotFoundException.class, () -> service.setDeleted(100, true));
    }

    @Test
    void updateChatMessage_positive() {
        when(dao.getChatMessageById(100)).thenReturn(message);

        service.updateChatMessage(100, updateDto);
        verify(updateChatMessageMapper).updateFromDTO(updateDto, message);

        assertNotNull(message.getUpdateDate());
    }

    @Test
    void updateChatMessage_negative() {
        when(dao.getChatMessageById(100)).thenReturn(null);
        assertThrows(EntityNotFoundException.class, () -> service.updateChatMessage(100, updateDto));
    }
}
