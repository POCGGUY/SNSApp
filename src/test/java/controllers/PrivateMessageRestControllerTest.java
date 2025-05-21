package controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import ru.pocgg.SNSApp.DTO.create.CreatePrivateMessageDTO;
import ru.pocgg.SNSApp.DTO.display.PrivateMessageDisplayDTO;
import ru.pocgg.SNSApp.DTO.update.UpdatePrivateMessageDTO;
import ru.pocgg.SNSApp.DTO.mappers.PrivateMessageDisplayMapper;
import ru.pocgg.SNSApp.model.Gender;
import ru.pocgg.SNSApp.model.PrivateMessage;
import ru.pocgg.SNSApp.model.SystemRole;
import ru.pocgg.SNSApp.model.User;
import ru.pocgg.SNSApp.controller.rest.*;
import ru.pocgg.SNSApp.model.exceptions.EntityNotFoundException;
import ru.pocgg.SNSApp.services.PermissionCheckService;
import ru.pocgg.SNSApp.services.PrivateMessageService;
import ru.pocgg.SNSApp.services.UserService;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrivateMessageRestControllerTest {

    @Mock
    private PrivateMessageService privateMessageService;

    @Mock
    private PrivateMessageDisplayMapper messageDisplayMapper;

    @Mock
    private PermissionCheckService permissionCheckService;

    @Mock
    private UserService userService;

    @InjectMocks
    private PrivateMessageRestController controller;

    private int senderId;
    private int receiverId;
    private int partnerId;
    private int messageId;
    private Instant now;
    private CreatePrivateMessageDTO createDto;
    private UpdatePrivateMessageDTO updateDto;
    private PrivateMessage message;
    private PrivateMessageDisplayDTO messageDto;
    private List<PrivateMessage> sentList;
    private List<PrivateMessage> receivedList;
    private List<PrivateMessageDisplayDTO> dtoList;

    @BeforeEach
    void setUp() {
        senderId = 1;
        receiverId = 2;
        partnerId = 3;
        messageId = 4;
        now = Instant.now();

        createDto = CreatePrivateMessageDTO.builder()
                .text("Hi!")
                .build();

        updateDto = UpdatePrivateMessageDTO.builder()
                .text("Edited")
                .build();

        User sender = User.builder()
                .userName("a").creationDate(Instant.now())
                .birthDate(LocalDate.now().minusYears(30))
                .password("p").email("a@a")
                .firstName("A").secondName("A")
                .gender(Gender.MALE).systemRole(SystemRole.USER)
                .deleted(false).acceptingPrivateMsgs(true)
                .postsPublic(true).banned(false)
                .build();
        sender.setId(senderId);

        User receiver = User.builder().userName("r").creationDate(Instant.now())
                .birthDate(LocalDate.now().minusYears(30))
                .password("p").email("r@e").firstName("R").secondName("R")
                .gender(null).systemRole(null)
                .deleted(false).acceptingPrivateMsgs(true)
                .postsPublic(true).banned(false)
                .build();
        receiver.setId(receiverId);

        message = PrivateMessage.builder()
                .sender(sender)
                .receiver(receiver)
                .text(createDto.getText())
                .creationDate(now)
                .deleted(false)
                .build();
        message.setId(messageId);

        messageDto = PrivateMessageDisplayDTO.builder()
                .id(messageId)
                .senderId(senderId)
                .senderName("A A")
                .creationDate(now.toString())
                .updateDate(null)
                .deleted(false)
                .text(createDto.getText())
                .build();

        sentList = Collections.singletonList(message);
        receivedList = Collections.emptyList();
        dtoList = Collections.singletonList(messageDto);
    }

    @Test
    void create_positive() {
        when(permissionCheckService.canSendMessageToThisUser(senderId, receiverId)).thenReturn(true);

        when(privateMessageService.createMessage(senderId, receiverId, createDto))
                .thenReturn(message);

        when(messageDisplayMapper.toDTO(message))
                .thenReturn(messageDto);

        ResponseEntity<PrivateMessageDisplayDTO> resp =
                controller.create(senderId, receiverId, createDto);

        assertEquals(201, resp.getStatusCodeValue());
        assertEquals(messageDto, resp.getBody());
    }

    @Test
    void create_negative() {
        when(permissionCheckService.canSendMessageToThisUser(senderId, receiverId)).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> controller.create(senderId, receiverId, createDto));
    }

    @Test
    void getMessagesFromTo_positive() {
        when(privateMessageService.getAllBySenderAndReceiver(senderId, partnerId))
                .thenReturn(sentList);

        when(privateMessageService.getAllBySenderAndReceiver(partnerId, senderId))
                .thenReturn(receivedList);

        when(messageDisplayMapper.toDTO(message))
                .thenReturn(messageDto);

        ResponseEntity<List<PrivateMessageDisplayDTO>> resp =
                controller.getMessagesFromTo(senderId, partnerId);

        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(dtoList, resp.getBody());
    }

    @Test
    void getMessagesFromTo_negative() {
        when(privateMessageService.getAllBySenderAndReceiver(anyInt(), anyInt()))
                .thenThrow(new EntityNotFoundException("not found"));

        assertThrows(EntityNotFoundException.class,
                () -> controller.getMessagesFromTo(senderId, partnerId));
    }

    @Test
    void getById_positive() {
        when(privateMessageService.getById(messageId))
                .thenReturn(message);

        when(permissionCheckService.canUserReadPrivateMessage(senderId, messageId))
                .thenReturn(true);

        when(messageDisplayMapper.toDTO(message))
                .thenReturn(messageDto);

        ResponseEntity<PrivateMessageDisplayDTO> resp =
                controller.getById(messageId, senderId);

        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(messageDto, resp.getBody());
    }

    @Test
    void getById_negative() {
        when(privateMessageService.getById(messageId))
                .thenReturn(message);

        when(permissionCheckService.canUserReadPrivateMessage(senderId, messageId))
                .thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> controller.getById(messageId, senderId));
    }

    @Test
    void edit_positive() {
        when(permissionCheckService.canUserModifyPrivateMessage(senderId, messageId))
                .thenReturn(true);

        ResponseEntity<Void> resp =
                controller.edit(messageId, updateDto, senderId);

        assertEquals(204, resp.getStatusCodeValue());
        verify(privateMessageService).updateMessage(messageId, updateDto);
    }

    @Test
    void edit_negative() {
        when(permissionCheckService.canUserModifyPrivateMessage(senderId, messageId))
                .thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> controller.edit(messageId, updateDto, senderId));
    }

    @Test
    void delete_positive() {
        when(permissionCheckService.canUserDeletePrivateMessage(senderId, messageId))
                .thenReturn(true);

        ResponseEntity<Void> resp =
                controller.delete(messageId, senderId);

        assertEquals(204, resp.getStatusCodeValue());
        verify(privateMessageService).setDeleted(messageId, true);
    }

    @Test
    void delete_negative() {
        when(permissionCheckService.canUserDeletePrivateMessage(senderId, messageId))
                .thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> controller.delete(messageId, senderId));
    }
}
