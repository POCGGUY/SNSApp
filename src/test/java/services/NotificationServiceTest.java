package services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.pocgg.SNSApp.model.Gender;
import ru.pocgg.SNSApp.model.Notification;
import ru.pocgg.SNSApp.model.SystemRole;
import ru.pocgg.SNSApp.model.User;
import ru.pocgg.SNSApp.services.*;
import ru.pocgg.SNSApp.model.exceptions.EntityNotFoundException;
import ru.pocgg.SNSApp.services.DAO.interfaces.NotificationServiceDAO;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationServiceDAO dao;
    @Mock
    private UserService userService;
    @InjectMocks
    private NotificationService service;

    private User user;
    private Notification notification;
    private Instant now;

    @BeforeEach
    void setUp() {
        now = Instant.now();
        user = User.builder()
                .userName("a").creationDate(now)
                .birthDate(LocalDate.now().minusYears(30))
                .password("p").email("a@a")
                .firstName("A").secondName("A")
                .gender(Gender.MALE).systemRole(SystemRole.USER)
                .deleted(false).acceptingPrivateMsgs(true)
                .postsPublic(true).banned(false)
                .build();
        user.setId(1);

        notification = Notification.builder()
                .receiver(user)
                .description("desc")
                .creationDate(Instant.now())
                .read(false)
                .build();
        notification.setId(10);

        lenient().when(userService.getUserById(1)).thenReturn(user);
    }

    @Test
    void createNotification_positive() {
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);

        Notification outNotification = service.createNotification(1, "hello", now);

        verify(dao).addNotification(captor.capture());
        verify(dao).forceFlush();
        Notification saved = captor.getValue();
        assertEquals(user, saved.getReceiver());
        assertEquals("hello", saved.getDescription());
        assertEquals(now, saved.getCreationDate());
        assertFalse(saved.getRead());
        assertSame(saved, outNotification);
    }

    @Test
    void getAllNotifications_positive() {
        List<Notification> list = List.of(notification);
        when(dao.getAllNotifications()).thenReturn(list);

        List<Notification> outNotification = service.getAllNotifications();

        assertEquals(list, outNotification);
    }

    @Test
    void getNotificationsByReceiverId_positive() {
        List<Notification> list = List.of(notification);
        when(dao.getNotificationsByReceiverId(1)).thenReturn(list);

        List<Notification> outNotification = service.getNotificationsByReceiverId(1);

        assertEquals(list, outNotification);
    }

    @Test
    void getNotificationById_positive() {
        when(dao.getNotificationById(10)).thenReturn(notification);

        Notification outNotification = service.getNotificationById(10);

        assertSame(notification, outNotification);
    }

    @Test
    void getNotificationById_negative() {
        when(dao.getNotificationById(99)).thenReturn(null);

        assertThrows(EntityNotFoundException.class, () -> service.getNotificationById(99));
    }

    @Test
    void setRead_positive() {
        when(dao.getNotificationById(10)).thenReturn(notification);

        service.setRead(10, true);

        assertTrue(notification.getRead());
    }

    @Test
    void setRead_negative() {
        when(dao.getNotificationById(11)).thenReturn(null);

        assertThrows(EntityNotFoundException.class, () -> service.setRead(11, true));
    }

    @Test
    void delete_positive() {
        when(dao.getNotificationById(10)).thenReturn(notification);

        service.delete(10);

        verify(dao).removeNotification(notification);
    }

    @Test
    void delete_negative() {
        when(dao.getNotificationById(12)).thenReturn(null);

        assertThrows(EntityNotFoundException.class, () -> service.delete(12));
    }
}
