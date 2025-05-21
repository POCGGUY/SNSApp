package services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.pocgg.SNSApp.DTO.create.CreatePrivateMessageDTO;
import ru.pocgg.SNSApp.DTO.update.UpdatePrivateMessageDTO;
import ru.pocgg.SNSApp.model.Gender;
import ru.pocgg.SNSApp.model.PrivateMessage;
import ru.pocgg.SNSApp.model.SystemRole;
import ru.pocgg.SNSApp.model.User;
import ru.pocgg.SNSApp.model.exceptions.EntityNotFoundException;
import ru.pocgg.SNSApp.services.PrivateMessageService;
import ru.pocgg.SNSApp.services.DAO.interfaces.PrivateMessageDAO;
import ru.pocgg.SNSApp.services.UserService;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrivateMessageServiceTest {

    @Mock
    PrivateMessageDAO dao;
    @Mock
    UserService userService;
    @InjectMocks
    PrivateMessageService service;

    private User sender;
    private User receiver;
    private CreatePrivateMessageDTO createDto;
    private UpdatePrivateMessageDTO updateDto;

    @BeforeEach
    void setUp() {
        sender = User.builder()
                .userName("a").creationDate(Instant.now())
                .birthDate(LocalDate.now().minusYears(30))
                .password("p").email("a@a")
                .firstName("A").secondName("A")
                .gender(Gender.MALE).systemRole(SystemRole.USER)
                .deleted(false).acceptingPrivateMsgs(true)
                .postsPublic(true).banned(false)
                .build();
        sender.setId(1);

        receiver = User.builder().userName("r").creationDate(Instant.now())
                .birthDate(LocalDate.now().minusYears(30))
                .password("p").email("r@e").firstName("R").secondName("R")
                .gender(null).systemRole(null)
                .deleted(false).acceptingPrivateMsgs(true)
                .postsPublic(true).banned(false)
                .build();
        receiver.setId(2);

        createDto = CreatePrivateMessageDTO.builder()
                .text("hello").build();
        updateDto = UpdatePrivateMessageDTO.builder()
                .text("edited").build();
    }

    @Test
    void createMessage_positive() {
        when(userService.getUserById(1)).thenReturn(sender);
        when(userService.getUserById(2)).thenReturn(receiver);

        PrivateMessage message = service.createMessage(1, 2, createDto);

        verify(dao).add(argThat(m ->
                m.getSender() == sender &&
                        m.getReceiver() == receiver &&
                        !m.getDeleted() &&
                        "hello".equals(m.getText())
        ));
        verify(dao).forceFlush();
        assertEquals("hello", message.getText());
        assertFalse(message.getDeleted());
    }

    @Test
    void createMessage_negative() {
        when(userService.getUserById(1))
                .thenThrow(new EntityNotFoundException("not found"));
        assertThrows(EntityNotFoundException.class,
                () -> service.createMessage(1, 2, createDto));
    }

    @Test
    void getById_positive() {
        PrivateMessage privateMessage = PrivateMessage.builder()
                .sender(sender).receiver(receiver)
                .creationDate(Instant.now())
                .updateDate(null).deleted(false)
                .text("x").build();
        privateMessage.setId(5);
        when(dao.getById(5)).thenReturn(privateMessage);

        assertSame(privateMessage, service.getById(5));
    }

    @Test
    void getById_negative() {
        when(dao.getById(99)).thenReturn(null);

        assertThrows(EntityNotFoundException.class,
                () -> service.getById(99));
    }

    @Test
    void updateMessage_positive() {
        PrivateMessage privateMessage = PrivateMessage.builder()
                .sender(sender).receiver(receiver)
                .creationDate(Instant.now())
                .updateDate(null).deleted(false)
                .text("old").build();
        privateMessage.setId(7);
        when(dao.getById(7)).thenReturn(privateMessage);

        service.updateMessage(7, updateDto);

        assertEquals("edited", privateMessage.getText());
        assertNotNull(privateMessage.getUpdateDate());
    }

    @Test
    void updateMessage_negative() {
        when(dao.getById(8)).thenReturn(null);

        assertThrows(EntityNotFoundException.class,
                () -> service.updateMessage(8, updateDto));
    }

    @Test
    void getAllBySenderAndReceiver_positive() {
        List<PrivateMessage> list = List.of(
                PrivateMessage.builder()
                        .sender(sender).receiver(receiver)
                        .creationDate(Instant.now())
                        .updateDate(null).deleted(false)
                        .text("1").build()
        );
        when(dao.getAllBySenderAndReceiver(1, 2)).thenReturn(list);

        assertEquals(list, service.getAllBySenderAndReceiver(1, 2));
    }

    @Test
    void getAllBySenderAndReceiver_negative() {
        when(dao.getAllBySenderAndReceiver(1, 2))
                .thenThrow(new RuntimeException());

        assertThrows(RuntimeException.class,
                () -> service.getAllBySenderAndReceiver(1, 2));
    }

    @Test
    void setDeleted_positive() {
        PrivateMessage privateMessage = PrivateMessage.builder()
                .sender(sender).receiver(receiver)
                .creationDate(Instant.now())
                .updateDate(null).deleted(false)
                .text("t").build();
        privateMessage.setId(11);
        when(dao.getById(11)).thenReturn(privateMessage);

        service.setDeleted(11, true);

        assertTrue(privateMessage.getDeleted());
        assertNotNull(privateMessage.getUpdateDate());
    }

    @Test
    void setDeleted_negative() {
        when(dao.getById(12)).thenReturn(null);

        assertThrows(EntityNotFoundException.class,
                () -> service.setDeleted(12, false));
    }
}
