package controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import ru.pocgg.SNSApp.DTO.create.CreateChatDTO;
import ru.pocgg.SNSApp.DTO.update.UpdateChatDTO;
import ru.pocgg.SNSApp.DTO.display.ChatDisplayDTO;
import ru.pocgg.SNSApp.DTO.mappers.display.ChatDisplayMapper;
import ru.pocgg.SNSApp.controller.rest.ChatRestController;
import ru.pocgg.SNSApp.model.Chat;
import ru.pocgg.SNSApp.model.exceptions.EntityNotFoundException;
import ru.pocgg.SNSApp.services.ChatService;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatRestControllerTest {

    @Mock
    private ChatService chatService;

    @Mock
    private ChatDisplayMapper chatMapper;

    @Mock
    private PermissionCheckService permissionCheckService;

    @InjectMocks
    private ChatRestController controller;

    private int userId;
    private int chatId;
    private Instant creationDate;
    private CreateChatDTO createDto;
    private UpdateChatDTO updateDto;
    private Chat chat;
    private ChatDisplayDTO chatDto;
    private List<Chat> chatList;
    private List<ChatDisplayDTO> dtoList;

    @BeforeEach
    void setUp() {
        userId = 1;
        chatId = 2;
        creationDate = Instant.now();

        createDto = CreateChatDTO.builder()
                .name("ChatName")
                .build();

        updateDto = UpdateChatDTO.builder()
                .name("NewName")
                .build();

        chat = Chat.builder()
                .name("ChatName")
                .creationDate(creationDate)
                .deleted(false)
                .build();
        chat.setId(chatId);

        chatDto = ChatDisplayDTO.builder()
                .id(chatId)
                .name("ChatName")
                .creationDate(creationDate.toString())
                .build();

        chatList = Arrays.asList(chat);
        dtoList = Arrays.asList(chatDto);
    }

    @Test
    void createChat_positive() {
        when(chatService.createChat(userId, createDto)).thenReturn(chat);

        when(chatMapper.toDTO(chat)).thenReturn(chatDto);

        ResponseEntity<ChatDisplayDTO> resp =
                controller.createChat(userId, createDto);

        assertEquals(201, resp.getStatusCodeValue());
        assertEquals(chatDto, resp.getBody());
    }

    @Test
    void createChat_negative() {
        when(chatService.createChat(userId, createDto))
                .thenThrow(new RuntimeException("fail"));

        assertThrows(RuntimeException.class,
                () -> controller.createChat(userId, createDto));
    }

    @Test
    void getChat_positive() {
        when(chatService.getChatById(chatId)).thenReturn(chat);

        when(permissionCheckService.canViewChat(userId, chat)).thenReturn(true);

        when(chatMapper.toDTO(chat)).thenReturn(chatDto);

        ResponseEntity<ChatDisplayDTO> resp =
                controller.getChat(chatId);

        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(chatDto, resp.getBody());
    }

    @Test
    void getChat_negative_permission() {
        when(chatService.getChatById(chatId)).thenReturn(chat);

        when(permissionCheckService.canViewChat(userId, chat)).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> controller.getChat(chatId));
    }

    @Test
    void getChat_negative_notFound() {
        when(chatService.getChatById(chatId))
                .thenThrow(new EntityNotFoundException("not found"));

        assertThrows(EntityNotFoundException.class,
                () -> controller.getChat(chatId));
    }

    @Test
    void listChats_positive() {
        when(chatService.getAllChats()).thenReturn(chatList);

        when(chatMapper.toDTO(chat)).thenReturn(chatDto);

        ResponseEntity<List<ChatDisplayDTO>> resp =
                controller.listChats();

        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(dtoList, resp.getBody());
    }

    @Test
    void listChats_negative() {
        when(chatService.getAllChats())
                .thenThrow(new RuntimeException("something went wrong"));

        assertThrows(RuntimeException.class,
                () -> controller.listChats());
    }

    @Test
    void editChat_positive() {
        when(permissionCheckService.canEditChat(userId, chatId)).thenReturn(true);

        doNothing().when(chatService).updateChat(chatId, updateDto);

        ResponseEntity<Void> resp =
                controller.editChat(userId, updateDto);

        assertEquals(204, resp.getStatusCodeValue());
    }

    @Test
    void editChat_negative() {
        when(permissionCheckService.canEditChat(userId, chatId)).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> controller.editChat(userId, updateDto));
    }

    @Test
    void delete_positive() {
        when(permissionCheckService.isUserChatOwnerOrSystemModerator(userId, chatId)).thenReturn(true);

        doNothing().when(chatService).setDeleted(chatId, true);

        ResponseEntity<Void> resp =
                controller.delete(chatId);

        assertEquals(200, resp.getStatusCodeValue());
    }

    @Test
    void delete_negative() {
        when(permissionCheckService.isUserChatOwnerOrSystemModerator(userId, chatId)).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> controller.delete(chatId));
    }
}
