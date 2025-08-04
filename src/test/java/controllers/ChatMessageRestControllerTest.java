package controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import ru.pocgg.SNSApp.DTO.create.CreateChatMessageDTO;
import ru.pocgg.SNSApp.DTO.update.UpdateChatMessageDTO;
import ru.pocgg.SNSApp.DTO.display.ChatMessageDisplayDTO;
import ru.pocgg.SNSApp.DTO.mappers.display.ChatMessageDisplayMapper;
import ru.pocgg.SNSApp.controller.rest.ChatMessageRestController;
import ru.pocgg.SNSApp.model.Chat;
import ru.pocgg.SNSApp.model.ChatMessage;
import ru.pocgg.SNSApp.model.User;
import ru.pocgg.SNSApp.model.exceptions.EntityNotFoundException;
import ru.pocgg.SNSApp.services.ChatMessageService;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatMessageRestControllerTest {

    @Mock
    private ChatMessageService chatMessageService;

    @Mock
    private ChatMessageDisplayMapper messageMapper;

    @Mock
    private PermissionCheckService permissionCheckService;

    @InjectMocks
    private ChatMessageRestController controller;

    private int userId;
    private int chatId;
    private int messageId;
    private int message1Id;
    private int message2Id;
    private Instant sendDate;
    private CreateChatMessageDTO createDto;
    private UpdateChatMessageDTO updateDto;
    private ChatMessage message;
    private ChatMessageDisplayDTO messageDto;
    private ChatMessage message1;
    private ChatMessage message2;
    private ChatMessageDisplayDTO messageDto1;
    private ChatMessageDisplayDTO messageDto2;
    private List<ChatMessage> messageList;
    private List<ChatMessageDisplayDTO> dtoList;

    @BeforeEach
    void setUp() {
        userId = 1;
        chatId = 2;
        messageId = 3;
        message1Id = 4;
        message2Id = 5;
        sendDate = Instant.now();

        createDto = CreateChatMessageDTO.builder()
                .text("Hello")
                .build();

        updateDto = UpdateChatMessageDTO.builder()
                .text("Upd")
                .build();

        Chat chat = Chat.builder()
                .build();
        chat.setId(chatId);

        User senderUser = User.builder().userName("r").creationDate(Instant.now())
                .birthDate(LocalDate.now().minusYears(30))
                .password("p").email("r@e").firstName("R").secondName("R")
                .gender(null).systemRole(null)
                .deleted(false).acceptingPrivateMsgs(true)
                .postsPublic(true).banned(false)
                .build();
        senderUser.setId(userId);

        message = ChatMessage.builder()
                .chat(chat)
                .sender(senderUser)
                .text("Hello")
                .sendingDate(sendDate)
                .build();
        message.setId(messageId);

        messageDto = ChatMessageDisplayDTO.builder()
                .id(messageId)
                .chatId(chatId)
                .senderId(userId)
                .text("Hello")
                .sendingDate(sendDate.toString())
                .build();

        message1 = ChatMessage.builder()
                .chat(chat)
                .sender(senderUser)
                .text("A")
                .sendingDate(sendDate.plusSeconds(10))
                .build();
        message1.setId(message1Id);

        message2 = ChatMessage.builder()
                .chat(chat)
                .sender(senderUser)
                .text("B")
                .sendingDate(sendDate)
                .build();
        message2.setId(message2Id);

        messageDto1 = ChatMessageDisplayDTO.builder()
                .id(message1Id)
                .chatId(chatId)
                .senderId(userId)
                .text("A")
                .sendingDate(sendDate.plusSeconds(10).toString())
                .build();

        messageDto2 = ChatMessageDisplayDTO.builder()
                .id(message2Id)
                .chatId(chatId)
                .senderId(userId)
                .text("B")
                .sendingDate(sendDate.toString())
                .build();

        messageList = Arrays.asList(message1, message2);
        dtoList = Arrays.asList(messageDto1, messageDto2);
    }

    @Test
    void createMessage_positive() {
        when(permissionCheckService.canUserCreateMessageInChat(userId, chatId)).thenReturn(true);

        when(chatMessageService.createChatMessage(chatId, userId, createDto)).thenReturn(message);

        when(messageMapper.toDTO(message)).thenReturn(messageDto);

        ResponseEntity<ChatMessageDisplayDTO> resp =
                controller.createMessage(chatId, userId, createDto);

        assertEquals(201, resp.getStatusCodeValue());
        assertEquals(messageDto, resp.getBody());
    }

    @Test
    void createMessage_negative() {
        when(permissionCheckService.canUserCreateMessageInChat(userId, chatId)).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> controller.createMessage(chatId, userId, createDto));
    }

    @Test
    void listMessages_positive() {
        when(permissionCheckService.canUserViewMessagesInChat(userId, chatId)).thenReturn(true);

        when(chatMessageService.getMessagesByChatId(chatId)).thenReturn(messageList);

        when(messageMapper.toDTO(message1)).thenReturn(messageDto1);
        when(messageMapper.toDTO(message2)).thenReturn(messageDto2);

        ResponseEntity<List<ChatMessageDisplayDTO>> resp =
                controller.listMessages(userId, chatId);

        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(dtoList, resp.getBody());
    }

    @Test
    void listMessages_negative() {
        when(permissionCheckService.canUserViewMessagesInChat(userId, chatId)).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> controller.listMessages(userId, chatId));
    }

    @Test
    void getMessage_positive() {
        when(chatMessageService.getChatMessageById(messageId)).thenReturn(message);

        when(permissionCheckService.canUserViewMessagesInChat(userId, chatId)).thenReturn(true);

        when(messageMapper.toDTO(message)).thenReturn(messageDto);

        ResponseEntity<ChatMessageDisplayDTO> resp =
                controller.getMessage(messageId);

        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(messageDto, resp.getBody());
    }

    @Test
    void getMessage_negative_notFound() {
        when(chatMessageService.getChatMessageById(messageId))
                .thenThrow(new EntityNotFoundException("not found"));

        assertThrows(EntityNotFoundException.class,
                () -> controller.getMessage(messageId));
    }

    @Test
    void getMessage_negative_noPerm() {
        when(chatMessageService.getChatMessageById(messageId)).thenReturn(message);

        when(permissionCheckService.canUserViewMessagesInChat(userId, chatId)).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> controller.getMessage(messageId));
    }

    @Test
    void updateMessage_positive() {
        when(permissionCheckService.canUserModifyChatMessage(userId, messageId)).thenReturn(true);

        doNothing().when(chatMessageService).updateChatMessage(messageId, updateDto);

        ResponseEntity<Void> resp =
                controller.updateMessage(messageId, updateDto);

        assertEquals(204, resp.getStatusCodeValue());
    }

    @Test
    void updateMessage_negative() {
        when(permissionCheckService.canUserModifyChatMessage(userId, messageId)).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> controller.updateMessage(messageId, updateDto));
    }

    @Test
    void deleteMessage_positive() {
        when(permissionCheckService.canUserDeleteChatMessage(userId, messageId)).thenReturn(true);

        doNothing().when(chatMessageService).setDeleted(messageId, true);

        ResponseEntity<Void> resp =
                controller.deleteMessage(messageId, userId);

        assertEquals(204, resp.getStatusCodeValue());
    }

    @Test
    void deleteMessage_negative() {
        when(permissionCheckService.canUserDeleteChatMessage(userId, messageId)).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> controller.deleteMessage(messageId, userId));
    }
}
