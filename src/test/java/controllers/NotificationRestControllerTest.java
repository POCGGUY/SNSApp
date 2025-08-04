package controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import ru.pocgg.SNSApp.DTO.display.NotificationDisplayDTO;
import ru.pocgg.SNSApp.DTO.mappers.display.NotificationDisplayMapper;
import ru.pocgg.SNSApp.controller.rest.NotificationRestController;
import ru.pocgg.SNSApp.model.Gender;
import ru.pocgg.SNSApp.model.Notification;
import ru.pocgg.SNSApp.model.SystemRole;
import ru.pocgg.SNSApp.model.User;
import ru.pocgg.SNSApp.model.exceptions.EntityNotFoundException;
import ru.pocgg.SNSApp.services.NotificationService;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationRestControllerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private PermissionCheckService permissionCheckService;

    @Mock
    private NotificationDisplayMapper notificationDisplayMapper;

    @InjectMocks
    private NotificationRestController controller;

    private int userId;
    private int otherUserId;
    private int notification1Id;
    private int notification2Id;
    private Instant creationDate;

    private Notification notificationOwned;
    private Notification notificationOther;
    private NotificationDisplayDTO dtoOwned;
    private NotificationDisplayDTO dtoOther;
    private List<Notification> notifications;
    private List<NotificationDisplayDTO> dtos;

    @BeforeEach
    void setUp() {
        userId = 1;
        otherUserId = 2;
        notification1Id = 3;
        notification2Id = 4;
        creationDate = Instant.now();

        User receiver = User.builder().userName("r").creationDate(Instant.now())
                .birthDate(LocalDate.now().minusYears(30))
                .password("p").email("r@e").firstName("R").secondName("R")
                .gender(null).systemRole(null)
                .deleted(false).acceptingPrivateMsgs(true)
                .postsPublic(true).banned(false)
                .build();
        receiver.setId(userId);

        User otherReceiver = User.builder()
                .userName("a").creationDate(Instant.now())
                .birthDate(LocalDate.now().minusYears(30))
                .password("p").email("a@a")
                .firstName("A").secondName("A")
                .gender(Gender.MALE).systemRole(SystemRole.USER)
                .deleted(false).acceptingPrivateMsgs(true)
                .postsPublic(true).banned(false)
                .build();
        otherReceiver.setId(otherUserId);

        notificationOwned = Notification.builder()
                .receiver(receiver)
                .description("Owned")
                .creationDate(creationDate)
                .build();
        notificationOwned.setId(notification1Id);


        notificationOther = Notification.builder()
                .receiver(otherReceiver)
                .description("Other")
                .creationDate(creationDate.minusSeconds(5))
                .build();
        notificationOther.setId(notification2Id);

        dtoOwned = NotificationDisplayDTO.builder()
                .id(notification1Id)
                .description("Owned")
                .creationDate(creationDate.toString())
                .build();

        dtoOther = NotificationDisplayDTO.builder()
                .id(notification2Id)
                .description("Other")
                .creationDate(creationDate.minusSeconds(5).toString())
                .build();

        notifications = Arrays.asList(notificationOwned, notificationOther);
        dtos = Arrays.asList(dtoOwned, dtoOther);
    }


    @Test
    void getIncoming_positive() {
        when(notificationService.getNotificationsByReceiverId(userId))
                .thenReturn(notifications);

        when(notificationDisplayMapper.toDTO(notificationOwned))
                .thenReturn(dtoOwned);

        when(notificationDisplayMapper.toDTO(notificationOther))
                .thenReturn(dtoOther);

        ResponseEntity<List<NotificationDisplayDTO>> resp =
                controller.getIncoming(userId);

        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(dtos, resp.getBody());
    }

    @Test
    void getIncoming_negative() {
        when(notificationService.getNotificationsByReceiverId(userId))
                .thenThrow(new RuntimeException("DB error"));

        assertThrows(RuntimeException.class,
                () -> controller.getIncoming(userId));
    }

    @Test
    void getAll_positive() {
        when(notificationService.getNotificationsByReceiverId(userId))
                .thenReturn(notifications);

        when(notificationDisplayMapper.toDTO(notificationOwned))
                .thenReturn(dtoOwned);

        when(notificationDisplayMapper.toDTO(notificationOther))
                .thenReturn(dtoOther);

        ResponseEntity<List<NotificationDisplayDTO>> resp =
                controller.getAll(userId);

        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(dtos, resp.getBody());
    }

    @Test
    void getAll_negative() {
        when(notificationService.getNotificationsByReceiverId(userId))
                .thenThrow(new EntityNotFoundException("not found"));

        assertThrows(EntityNotFoundException.class,
                () -> controller.getAll(userId));
    }

    @Test
    void setRead_positive() {
        when(notificationService.getNotificationById(notification1Id))
                .thenReturn(notificationOwned);

        ResponseEntity<Void> resp =
                controller.setRead(notification1Id, true, userId);

        assertEquals(204, resp.getStatusCodeValue());

        verify(notificationService).setRead(notification1Id, true);
    }

    @Test
    void setRead_negative() {
        when(notificationService.getNotificationById(notification1Id))
                .thenReturn(notificationOther);

        when(permissionCheckService.isUserSystemModerator(userId))
                .thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> controller.setRead(notification1Id, false, userId));
    }


    @Test
    void delete_positive() {
        when(notificationService.getNotificationById(notification1Id))
                .thenReturn(notificationOwned);

        ResponseEntity<Void> resp =
                controller.delete(notification1Id, userId);

        assertEquals(204, resp.getStatusCodeValue());

        verify(notificationService).delete(notification1Id);
    }

    @Test
    void delete_negative() {
        when(notificationService.getNotificationById(notification1Id))
                .thenReturn(notificationOther);

        when(permissionCheckService.isUserSystemModerator(userId))
                .thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> controller.delete(notification1Id, userId));
    }
}
