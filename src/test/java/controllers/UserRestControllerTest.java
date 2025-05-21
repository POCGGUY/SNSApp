package controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import ru.pocgg.SNSApp.DTO.create.CreateUserDTO;
import ru.pocgg.SNSApp.DTO.create.CreateUserWithRoleDTO;
import ru.pocgg.SNSApp.DTO.display.UserDisplayAllDTO;
import ru.pocgg.SNSApp.DTO.display.UserDisplayDTO;
import ru.pocgg.SNSApp.DTO.mappers.UserDisplayAllMapper;
import ru.pocgg.SNSApp.DTO.mappers.UserDisplayMapper;
import ru.pocgg.SNSApp.DTO.update.UpdateUserDTO;
import ru.pocgg.SNSApp.controller.rest.UserRestController;
import ru.pocgg.SNSApp.model.Gender;
import ru.pocgg.SNSApp.model.SystemRole;
import ru.pocgg.SNSApp.model.User;
import ru.pocgg.SNSApp.model.exceptions.BadRequestException;
import ru.pocgg.SNSApp.model.exceptions.EntityNotFoundException;
import ru.pocgg.SNSApp.services.PermissionCheckService;
import ru.pocgg.SNSApp.services.UserService;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRestControllerTest {

    @Mock
    private UserService userService;
    @Mock
    private UserDisplayMapper mapper;
    @Mock
    private PermissionCheckService permService;
    @Mock
    private UserDisplayAllMapper allMapper;
    @InjectMocks
    private UserRestController controller;

    private Instant creationDate;
    private CreateUserDTO createDto;
    private CreateUserWithRoleDTO createWithRoleDto;
    private UpdateUserDTO updateDto;
    private User user;
    private UserDisplayDTO displayDto;
    private UserDisplayAllDTO displayAllDto;

    @BeforeEach
    void setUp() {
        creationDate = Instant.now();

        createDto = CreateUserDTO.builder()
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

        createWithRoleDto = CreateUserWithRoleDTO.builder()
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

        user = User.builder()
                .userName("a").creationDate(Instant.now())
                .birthDate(LocalDate.now().minusYears(30))
                .password("p").email("a@a")
                .firstName("A").secondName("A")
                .gender(Gender.MALE).systemRole(SystemRole.USER)
                .deleted(false).acceptingPrivateMsgs(true)
                .postsPublic(true).banned(false)
                .build();
        user.setId(42);

        displayDto = UserDisplayDTO.builder()
                .id(42)
                .firstName("u")
                .secondName("d")
                .build();

        displayAllDto = UserDisplayAllDTO.builder()
                .id(42)
                .userName("u")
                .firstName("u")
                .secondName("d")
                .build();
    }

    @Test
    void register_positive() {
        when(userService.createUser(createDto)).thenReturn(user);
        when(mapper.toDTO(user)).thenReturn(displayDto);

        ResponseEntity<UserDisplayDTO> resp = controller.register(createDto);

        assertEquals(201, resp.getStatusCodeValue());
        assertEquals(displayDto, resp.getBody());
    }

    @Test
    void register_negative() {
        when(userService.createUser(createDto)).thenThrow(new RuntimeException("fail"));

        assertThrows(RuntimeException.class, () -> controller.register(createDto));
    }

    @Test
    void createWithSystemRole_positive() {
        when(userService.createUserWithSystemRole(createWithRoleDto)).thenReturn(user);
        when(mapper.toDTO(user)).thenReturn(displayDto);

        ResponseEntity<UserDisplayDTO> resp = controller.createWithSystemRole(createWithRoleDto);

        assertEquals(201, resp.getStatusCodeValue());
        assertEquals(displayDto, resp.getBody());
    }

    @Test
    void createWithSystemRole_negative() {
        when(userService.createUserWithSystemRole(createWithRoleDto))
                .thenThrow(new RuntimeException("fail"));

        assertThrows(RuntimeException.class, () -> controller.createWithSystemRole(createWithRoleDto));
    }

    @Test
    void getMyProfile_positive() {
        when(userService.getUserById(42)).thenReturn(user);
        when(mapper.toDTO(user)).thenReturn(displayDto);

        UserDisplayDTO dto = controller.getMyProfile(42);

        assertEquals(displayDto, dto);
    }

    @Test
    void getMyProfile_negative() {
        when(userService.getUserById(42)).thenThrow(new RuntimeException());

        assertThrows(RuntimeException.class, () -> controller.getMyProfile(42));
    }

    @Test
    void getMyProfileFull_positive() {
        when(userService.getUserById(42)).thenReturn(user);
        when(allMapper.toDTO(user)).thenReturn(displayAllDto);

        UserDisplayAllDTO dto = controller.getMyProfileFull(42);

        assertEquals(displayAllDto, dto);
    }

    @Test
    void getMyProfileFull_negative() {
        when(userService.getUserById(42)).thenThrow(new RuntimeException());

        assertThrows(RuntimeException.class, () -> controller.getMyProfileFull(42));
    }

    @Test
    void getProfileById_positive() {
        when(permService.canViewUserProfile(1, 2)).thenReturn(true);
        when(userService.getUserById(2)).thenReturn(user);
        when(mapper.toDTO(user)).thenReturn(displayDto);

        UserDisplayDTO dto = controller.getProfileById(1, 2);

        assertEquals(displayDto, dto);
    }

    @Test
    void getProfileById_negative() {
        when(permService.canViewUserProfile(1, 2)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> controller.getProfileById(1, 2));
    }

    @Test
    void getFullById_positive() {
        when(userService.getUserById(5)).thenReturn(user);
        when(allMapper.toDTO(user)).thenReturn(displayAllDto);

        UserDisplayAllDTO dto = controller.getFullById(5);

        assertEquals(displayAllDto, dto);
    }

    @Test
    void getFullById_negative() {
        when(userService.getUserById(5)).thenThrow(new EntityNotFoundException("not found"));

        assertThrows(EntityNotFoundException.class, () -> controller.getFullById(5));
    }

    @Test
    void updateMe_positive() {
        doNothing().when(userService).updateUser(42, updateDto);

        ResponseEntity<Void> resp = controller.updateMe(42, updateDto);

        assertEquals(204, resp.getStatusCodeValue());
    }

    @Test
    void updateMe_negative() {
        doThrow(new EntityNotFoundException("not found")).when(userService).updateUser(42, updateDto);

        assertThrows(EntityNotFoundException.class, () -> controller.updateMe(42, updateDto));
    }

    @Test
    void updateByAdmin_positive() {
        doNothing().when(userService).updateUser(7, updateDto);

        ResponseEntity<Void> resp = controller.updateByAdmin(7, updateDto);

        assertEquals(204, resp.getStatusCodeValue());
    }

    @Test
    void updateByAdmin_negative() {
        doThrow(new EntityNotFoundException("not found")).when(userService).updateUser(7, updateDto);

        assertThrows(EntityNotFoundException.class, () -> controller.updateByAdmin(7, updateDto));
    }

    @Test
    void changeRole_positive() {
        doNothing().when(userService).setSystemRole(9, SystemRole.MODERATOR);

        ResponseEntity<Void> resp = controller.changeRole(1, 9, SystemRole.MODERATOR);

        assertEquals(204, resp.getStatusCodeValue());
    }

    @Test
    void changeRole_negative() {
        doThrow(new EntityNotFoundException("not found")).when(userService).setSystemRole(9, SystemRole.USER);

        assertThrows(EntityNotFoundException.class, () -> controller.changeRole(1, 9, SystemRole.USER));
    }

    @Test
    void setBanned_positive() {
        doNothing().when(userService).setIsBanned(8, true);

        ResponseEntity<Void> resp = controller.setBanned(1, 8, true);

        assertEquals(204, resp.getStatusCodeValue());
    }

    @Test
    void setBanned_negative() {
        assertThrows(BadRequestException.class, () -> controller.setBanned(5, 5, false));
    }

    @Test
    void setSelfDeleted_positive() {
        doNothing().when(userService).setIsDeleted(10, true);

        ResponseEntity<Void> resp = controller.setSelfDeleted(10, true);

        assertEquals(204, resp.getStatusCodeValue());
    }

    @Test
    void setSelfDeleted_negative() {
        doThrow(new EntityNotFoundException("not found")).when(userService).setIsDeleted(10, false);

        assertThrows(EntityNotFoundException.class, () -> controller.setSelfDeleted(10, false));
    }

    @Test
    void searchUsers_positive() {
        when(userService.searchUsers("F", "L", 30, Gender.MALE))
                .thenReturn(List.of(user));
        when(mapper.toDTO(user)).thenReturn(displayDto);

        ResponseEntity<List<UserDisplayDTO>> resp =
                controller.searchUsers("F", "L", 30, Gender.MALE);

        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(displayDto, resp.getBody().get(0));
    }

    @Test
    void searchUsers_negative() {
        when(userService.searchUsers(null, null, null, null))
                .thenThrow(new EntityNotFoundException("not found"));

        assertThrows(EntityNotFoundException.class,
                () -> controller.searchUsers(null, null, null, null));
    }
}
