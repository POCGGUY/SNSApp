package controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import ru.pocgg.SNSApp.controller.rest.UserChatsRestController;
import ru.pocgg.SNSApp.DTO.display.ChatDisplayDTO;
import ru.pocgg.SNSApp.DTO.mappers.display.ChatDisplayMapper;
import ru.pocgg.SNSApp.model.Chat;
import ru.pocgg.SNSApp.services.ChatService;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserChatsRestControllerTest {

    @Mock
    private ChatService chatService;
    @Mock
    private ChatDisplayMapper chatDisplayMapper;
    @InjectMocks
    private UserChatsRestController controller;

    private int currentUserId;
    private int otherUserId;
    private Instant creationDate;
    private Chat chat;
    private ChatDisplayDTO chatDto;

    @BeforeEach
    void setUp() {
        currentUserId = 100;
        otherUserId = 200;
        creationDate = Instant.now();

        chat = Chat.builder()
                .owner(null)
                .name("TestChat")
                .description("desc")
                .creationDate(creationDate)
                .isPrivate(false)
                .build();
        chat.setId(10);

        chatDto = ChatDisplayDTO.builder()
                .id(10)
                .name("TestChat")
                .build();
    }

    @Test
    void listUserChats_positive() {
        when(chatService.getChatsByMemberId(currentUserId))
                .thenReturn(List.of(chat));

        when(chatDisplayMapper.toDTO(chat))
                .thenReturn(chatDto);

        ResponseEntity<List<ChatDisplayDTO>> resp =
                controller.listUserChats(currentUserId);

        assertEquals(200, resp.getStatusCodeValue());
        List<ChatDisplayDTO> body = resp.getBody();
        assertNotNull(body);
        assertEquals(1, body.size());
        assertEquals(chatDto, body.get(0));
    }

    @Test
    void listUserChats_negative() {
        when(chatService.getChatsByMemberId(currentUserId))
                .thenReturn(Collections.emptyList());

        ResponseEntity<List<ChatDisplayDTO>> resp =
                controller.listUserChats(currentUserId);

        assertEquals(200, resp.getStatusCodeValue());
        List<ChatDisplayDTO> body = resp.getBody();
        assertNotNull(body);
        assertTrue(body.isEmpty());
    }

    @Test
    void listOtherUserChats_positive() {
        when(chatService.getChatsByMemberId(otherUserId))
                .thenReturn(List.of(chat));

        when(chatDisplayMapper.toDTO(chat))
                .thenReturn(chatDto);

        ResponseEntity<List<ChatDisplayDTO>> resp =
                controller.listOtherUserChats(otherUserId);

        assertEquals(200, resp.getStatusCodeValue());
        List<ChatDisplayDTO> body = resp.getBody();
        assertNotNull(body);
        assertEquals(1, body.size());
        assertEquals(chatDto, body.get(0));
    }

    @Test
    void listOtherUserChats_negative() {
        when(chatService.getChatsByMemberId(otherUserId))
                .thenReturn(Collections.emptyList());

        ResponseEntity<List<ChatDisplayDTO>> resp =
                controller.listOtherUserChats(otherUserId);

        assertEquals(200, resp.getStatusCodeValue());
        List<ChatDisplayDTO> body = resp.getBody();
        assertNotNull(body);
        assertTrue(body.isEmpty());
    }
}
