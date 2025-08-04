package controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.*;
import org.springframework.test.web.servlet.MockMvc;
import ru.pocgg.SNSApp.DTO.create.CreateUserDTO;
import ru.pocgg.SNSApp.DTO.create.CreateUserWithRoleDTO;
import ru.pocgg.SNSApp.DTO.display.UserDisplayAllDTO;
import ru.pocgg.SNSApp.DTO.display.UserDisplayDTO;
import ru.pocgg.SNSApp.DTO.mappers.display.UserDisplayAllMapper;
import ru.pocgg.SNSApp.DTO.mappers.display.UserDisplayMapper;
import ru.pocgg.SNSApp.DTO.update.UpdateUserDTO;
import ru.pocgg.SNSApp.controller.rest.UserRestController;
import ru.pocgg.SNSApp.model.Gender;
import ru.pocgg.SNSApp.model.User;
import ru.pocgg.SNSApp.services.permission.UserPermissionService;
import ru.pocgg.SNSApp.services.UserService;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserRestController.class)
class UserRestControllerMVCTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Mock
    private UserService userService;

    @Mock
    private UserPermissionService userPermissionService;

    @Mock
    private UserDisplayMapper userDisplayMapper;

    @Mock
    private UserDisplayAllMapper userDisplayAllMapper;

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
                .userName("u")
                .creationDate(creationDate)
                .birthDate(LocalDate.now().minusYears(30))
                .build();

        displayDto = UserDisplayDTO.builder()
                .id(42)
                .firstName("U")
                .secondName("U")
                .build();

        displayAllDto = UserDisplayAllDTO.builder()
                .id(42)
                .userName("u")
                .firstName("U")
                .secondName("U")
                .build();
    }

    @Test
    void register_positive() throws Exception {
        given(userService.createUser(any(CreateUserDTO.class))).willReturn(user);

        given(userDisplayMapper.toDTO(user)).willReturn(displayDto);

        mvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(42));
    }

    @Test
    void register_negative() throws Exception {
        given(userService.createUser(any(CreateUserDTO.class))).willThrow(new RuntimeException("fail"));

        mvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createWithSystemRole_positive() throws Exception {
        given(userService.createUserWithSystemRole(any(CreateUserWithRoleDTO.class))).willReturn(user);

        given(userDisplayMapper.toDTO(user)).willReturn(displayDto);

        mvc.perform(post("/users/registerWithRole")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createWithRoleDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(42));
    }

    @Test
    void createWithSystemRole_negative() throws Exception {
        mvc.perform(post("/users/registerWithRole")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createWithRoleDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER", username = "42")
    void getMyProfile_positive() throws Exception {
        given(userService.getUserById(42)).willReturn(user);

        given(userDisplayMapper.toDTO(user)).willReturn(displayDto);

        mvc.perform(get("/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(42));
    }

    @Test
    void getMyProfile_negative() throws Exception {
        mvc.perform(get("/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER", username = "42")
    void getMyProfileFull_positive() throws Exception {
        given(userService.getUserById(42)).willReturn(user);

        given(userDisplayAllMapper.toDTO(user)).willReturn(displayAllDto);

        mvc.perform(get("/users/me/full"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(42));
    }

    @Test
    void getMyProfileFull_negative() throws Exception {
        mvc.perform(get("/users/me/full"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER", username = "1")
    void getProfileById_positive() throws Exception {
        given(userPermissionService.canViewUserProfile(1, 2)).willReturn(true);

        given(userService.getUserById(2)).willReturn(user);

        given(userDisplayMapper.toDTO(user)).willReturn(displayDto);

        mvc.perform(get("/users/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(42));
    }

    @Test
    @WithMockUser(roles = "USER", username = "1")
    void getProfileById_negative() throws Exception {
        given(userPermissionService.canViewUserProfile(1, 2)).willReturn(false);

        mvc.perform(get("/users/2"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MODERATOR")
    void getFullById_positive() throws Exception {
        given(userService.getUserById(5)).willReturn(user);

        given(userDisplayAllMapper.toDTO(user)).willReturn(displayAllDto);

        mvc.perform(get("/users/5/full"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(42));
    }

    @Test
    void getFullById_negative() throws Exception {
        mvc.perform(get("/users/5/full"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER", username = "42")
    void updateMe_positive() throws Exception {
        mvc.perform(patch("/users/me")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateMe_negative() throws Exception {
        mvc.perform(patch("/users/me")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateByAdmin_positive() throws Exception {
        mvc.perform(patch("/users/7")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateByAdmin_negative() throws Exception {
        mvc.perform(patch("/users/7")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void changeRole_positive() throws Exception {
        mvc.perform(patch("/users/9/role")
                        .with(csrf())
                        .param("systemRole", "MODERATOR"))
                .andExpect(status().isNoContent());
    }

    @Test
    void changeRole_negative() throws Exception {
        mvc.perform(patch("/users/9/role")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "MODERATOR")
    void setBanned_positive() throws Exception {
        mvc.perform(patch("/users/8/banned")
                        .with(csrf())
                        .param("targetUserId", "8")
                        .param("banned", "true"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "MODERATOR", username = "5")
    void setBanned_negative() throws Exception {
        mvc.perform(patch("/users/5/banned")
                        .with(csrf())
                        .param("targetUserId", "5")
                        .param("banned", "true"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER", username = "10")
    void setSelfDeleted_positive() throws Exception {
        mvc.perform(patch("/users/me/deleted")
                        .with(csrf())
                        .param("deleted", "true"))
                .andExpect(status().isNoContent());
    }

    @Test
    void setSelfDeleted_negative() throws Exception {
        mvc.perform(patch("/users/me/deleted")
                        .with(csrf())
                        .param("deleted", "true"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void searchUsers_positive() throws Exception {
        given(userService.searchUsers("F", "L", 30, Gender.MALE)).willReturn(List.of(user));

        given(userDisplayMapper.toDTO(user)).willReturn(displayDto);
    }
}
