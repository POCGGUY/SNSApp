package services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.pocgg.SNSApp.DTO.create.CreateUserDTO;
import ru.pocgg.SNSApp.DTO.create.CreateUserWithRoleDTO;
import ru.pocgg.SNSApp.DTO.mappers.update.UpdateUserMapper;
import ru.pocgg.SNSApp.DTO.update.UpdateUserDTO;
import ru.pocgg.SNSApp.model.Gender;
import ru.pocgg.SNSApp.model.SystemRole;
import ru.pocgg.SNSApp.model.User;
import ru.pocgg.SNSApp.events.events.UserDeactivatedEvent;
import ru.pocgg.SNSApp.model.exceptions.EntityNotFoundException;
import ru.pocgg.SNSApp.model.exceptions.FoundUniqueExistingValuesException;
import ru.pocgg.SNSApp.services.DAO.interfaces.UserServiceDAO;
import ru.pocgg.SNSApp.services.UserService;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserServiceDAO dao;
    @Mock
    private PasswordEncoder encoder;
    @Mock
    private UpdateUserMapper updateUserMapper;
    @Mock
    private RabbitTemplate rabbitTemplate;
    @InjectMocks
    private UserService userService;

    private CreateUserDTO basicDto;
    private CreateUserWithRoleDTO roleDto;
    private UpdateUserDTO updateDto;

    @BeforeEach
    void setUp() {
        basicDto = CreateUserDTO.builder()
                .userName("alice")
                .email("a@x.com")
                .password("pw")
                .birthDate("1990-01-01")
                .firstName("A")
                .secondName("B")
                .thirdName(null)
                .gender("FEMALE")
                .description("desc")
                .build();

        roleDto = CreateUserWithRoleDTO.builder()
                .userName("bob")
                .email("b@x.com")
                .password("pw2")
                .birthDate("1991-02-02")
                .firstName("C")
                .secondName("D")
                .thirdName(null)
                .gender("MALE")
                .systemRole("ADMIN")
                .description("desc2")
                .build();

        updateDto = UpdateUserDTO.builder()
                .birthDate("1992-03-03")
                .email("new@x.com")
                .firstName("NewA")
                .secondName("NewB")
                .thirdName("NewC")
                .gender("MALE")
                .description("newdesc")
                .acceptingPrivateMsgs(false)
                .postsPublic(false)
                .build();
    }



    @Test
    void getUserById_positive() {
        User user = User.builder()
                .userName("u").creationDate(Instant.now())
                .birthDate(LocalDate.now()).password("p")
                .email("e@e").firstName("F").secondName("S")
                .gender(Gender.MALE).systemRole(SystemRole.USER)
                .deleted(false).acceptingPrivateMsgs(true)
                .postsPublic(true).banned(false)
                .build();
        user.setId(5);
        when(dao.getUserById(5)).thenReturn(user);

        assertSame(user, userService.getUserById(5));
    }

    @Test
    void getUserById_negative() {
        when(dao.getUserById(99)).thenReturn(null);
        assertThrows(EntityNotFoundException.class, () -> userService.getUserById(99));
    }

    @Test
    void getUserByUserName_positive() {
        User u = User.builder()
                .userName("alice").creationDate(Instant.now())
                .birthDate(LocalDate.now()).password("p")
                .email("e@e").firstName("F").secondName("S")
                .gender(Gender.FEMALE).systemRole(SystemRole.USER)
                .deleted(false).acceptingPrivateMsgs(true)
                .postsPublic(true).banned(false)
                .build();
        when(dao.getUserByUserName("alice")).thenReturn(u);

        assertSame(u, userService.getUserByUserName("alice"));
    }

    @Test
    void getUserByUserName_negative() {
        when(dao.getUserByUserName("nope")).thenReturn(null);
        assertThrows(EntityNotFoundException.class, () -> userService.getUserByUserName("nope"));
    }

    @Test
    void createUser_positive() {
        when(dao.getUserByUserName("alice")).thenReturn(null);
        when(dao.getUserByEmail("a@x.com")).thenReturn(null);
        when(encoder.encode("pw")).thenReturn("ENC");
        ArgumentCaptor<User> cap = ArgumentCaptor.forClass(User.class);

        User out = userService.createUser(basicDto);

        verify(dao).addUser(cap.capture());
        verify(dao).forceFlush();
        User saved = cap.getValue();
        assertEquals("alice", saved.getUserName());
        assertEquals("ENC", saved.getPassword());
        assertFalse(saved.getDeleted());
        assertEquals(saved, out);
    }

    @Test
    void createUser_negative() {
        when(dao.getUserByUserName("alice")).thenReturn(User.builder().build());
        assertThrows(FoundUniqueExistingValuesException.class,
                () -> userService.createUser(basicDto));
    }

    @Test
    void createUserWithSystemRole_positive() {
        when(dao.getUserByUserName("bob")).thenReturn(null);
        when(dao.getUserByEmail("b@x.com")).thenReturn(null);
        when(encoder.encode("pw2")).thenReturn("ENC2");
        ArgumentCaptor<User> cap = ArgumentCaptor.forClass(User.class);

        userService.createUserWithSystemRole(roleDto);

        verify(dao).addUser(cap.capture());
        assertEquals(SystemRole.ADMIN, cap.getValue().getSystemRole());
        assertEquals("ENC2", cap.getValue().getPassword());
    }

    @Test
    void createUserWithSystemRole_negative() {
        when(dao.getUserByUserName("bob")).thenReturn(null);
        when(dao.getUserByEmail("b@x.com")).thenReturn(User.builder().build());
        assertThrows(FoundUniqueExistingValuesException.class,
                () -> userService.createUserWithSystemRole(roleDto));
    }

    @Test
    void searchUsers_positive() {
        List<User> list = List.of(User.builder()
                .userName("u").creationDate(Instant.now())
                .birthDate(LocalDate.now().minusYears(30)).password("p")
                .email("e@e").firstName("F").secondName("S")
                .gender(Gender.MALE).systemRole(SystemRole.USER)
                .deleted(false).acceptingPrivateMsgs(true)
                .postsPublic(true).banned(false)
                .build()
        );
        when(dao.searchUsers("F","S",30,Gender.MALE)).thenReturn(list);

        assertEquals(list, userService.searchUsers("F","S",30,Gender.MALE));
    }

    @Test
    void updateUser_positive() {
        User u = User.builder()
                .userName("u").creationDate(Instant.now())
                .birthDate(LocalDate.parse("2000-01-01"))
                .password("p").email("e@e")
                .firstName("F").secondName("S")
                .gender(Gender.MALE).systemRole(SystemRole.USER)
                .deleted(false).acceptingPrivateMsgs(true)
                .postsPublic(true).banned(false)
                .build();
        u.setId(7);
        when(dao.getUserById(7)).thenReturn(u);

        userService.updateUser(7, updateDto);

        verify(updateUserMapper).updateFromDTO(updateDto, u);
    }

    @Test
    void updateUser_negative() {
        when(dao.getUserById(8)).thenReturn(null);
        assertThrows(EntityNotFoundException.class,
                () -> userService.updateUser(8, updateDto));
    }

    @Test
    void setSystemRole_positive() {
        User u = User.builder()
                .userName("u").creationDate(Instant.now())
                .birthDate(LocalDate.now())
                .password("p").email("e@e")
                .firstName("F").secondName("S")
                .gender(Gender.MALE).systemRole(SystemRole.USER)
                .deleted(false).acceptingPrivateMsgs(true)
                .postsPublic(true).banned(false)
                .build();
        u.setId(9);
        when(dao.getUserById(9)).thenReturn(u);

        userService.setSystemRole(9, SystemRole.ADMIN);
        assertEquals(SystemRole.ADMIN, u.getSystemRole());
    }

    @Test
    void setSystemRole_negative() {
        when(dao.getUserById(10)).thenReturn(null);
        assertThrows(EntityNotFoundException.class,
                () -> userService.setSystemRole(10, SystemRole.USER));
    }

    @Test
    void setIsBanned_positive() {
        User u = User.builder()
                .userName("u").creationDate(Instant.now())
                .birthDate(LocalDate.now())
                .password("p").email("e@e")
                .firstName("F").secondName("S")
                .gender(Gender.MALE).systemRole(SystemRole.USER)
                .deleted(false).acceptingPrivateMsgs(true)
                .postsPublic(true).banned(false)
                .build();
        u.setId(11);
        when(dao.getUserById(11)).thenReturn(u);

        userService.setIsBanned(11, true);
        assertTrue(u.getBanned());
    }

    @Test
    void setIsBanned_negative() {
        when(dao.getUserById(12)).thenReturn(null);
        assertThrows(EntityNotFoundException.class,
                () -> userService.setIsBanned(12, true));
    }

    @Test
    void setIsDeleted_positive() {
        User u = User.builder()
                .userName("u").creationDate(Instant.now())
                .birthDate(LocalDate.now())
                .password("p").email("e@e")
                .firstName("F").secondName("S")
                .gender(Gender.MALE).systemRole(SystemRole.USER)
                .deleted(false).acceptingPrivateMsgs(true)
                .postsPublic(true).banned(false)
                .build();
        u.setId(13);
        when(dao.getUserById(13)).thenReturn(u);

        userService.setIsDeleted(13, true);
        assertTrue(u.getDeleted());
    }

    @Test
    void setIsDeleted_negative() {
        when(dao.getUserById(14)).thenReturn(null);
        assertThrows(EntityNotFoundException.class,
                () -> userService.setIsDeleted(14, true));
    }
}
