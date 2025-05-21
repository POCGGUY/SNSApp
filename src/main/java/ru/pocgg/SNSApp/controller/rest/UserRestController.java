package ru.pocgg.SNSApp.controller.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import ru.pocgg.SNSApp.DTO.create.CreateUserDTO;
import ru.pocgg.SNSApp.DTO.create.CreateUserWithRoleDTO;
import ru.pocgg.SNSApp.DTO.display.UserDisplayAllDTO;
import ru.pocgg.SNSApp.DTO.display.UserDisplayDTO;
import ru.pocgg.SNSApp.DTO.mappers.UserDisplayAllMapper;
import ru.pocgg.SNSApp.DTO.mappers.UserDisplayMapper;
import ru.pocgg.SNSApp.DTO.update.UpdateUserDTO;
import ru.pocgg.SNSApp.model.Gender;
import ru.pocgg.SNSApp.model.User;
import ru.pocgg.SNSApp.model.SystemRole;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.pocgg.SNSApp.model.exceptions.BadRequestException;
import ru.pocgg.SNSApp.services.PermissionCheckService;
import ru.pocgg.SNSApp.services.UserService;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "Управление пользователями")
public class UserRestController extends TemplateController {
    private final UserService userService;
    private final UserDisplayMapper userDisplayMapper;
    private final PermissionCheckService permissionCheckService;
    private final UserDisplayAllMapper userDisplayAllMapper;


    @Operation(summary = "Регистрация пользователя")
    @PostMapping("/register")
    public ResponseEntity<UserDisplayDTO> register(@Valid @RequestBody CreateUserDTO dto) {
        User user = userService.createUser(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(userDisplayMapper.toDTO(user));
    }

    @Operation(summary = "Регистрация пользователя с заданием системной роли вручную")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/registerWithRole")
    public ResponseEntity<UserDisplayDTO> createWithSystemRole(@Valid @RequestBody CreateUserWithRoleDTO dto) {
        User user = userService.createUserWithSystemRole(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(userDisplayMapper.toDTO(user));
    }

    @Operation(summary = "Просмотр своего профиля")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/me")
    public UserDisplayDTO getMyProfile(@AuthenticationPrincipal(expression = "id") int userId) {
        return userDisplayMapper.toDTO(userService.getUserById(userId));
    }

    @Operation(summary = "Просмотр своего полного профиля")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/me/full")
    public UserDisplayAllDTO getMyProfileFull(@AuthenticationPrincipal(expression = "id") int userId) {
        return userDisplayAllMapper.toDTO(userService.getUserById(userId));
    }

    @Operation(summary = "Просмотр чужого профиля")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/{targetUserId}")
    public UserDisplayDTO getProfileById(@AuthenticationPrincipal(expression = "id") int userId,
                                         @PathVariable int targetUserId) {
        checkCanViewUserProfile(userId, targetUserId);
        return userDisplayMapper.toDTO(userService.getUserById(targetUserId));
    }

    @Operation(summary = "Просмотр полного профиля другого пользователя")
    @PreAuthorize("hasRole('MODERATOR')")
    @GetMapping("/{id}/full")
    public UserDisplayAllDTO getFullById(@PathVariable int id) {
        return userDisplayAllMapper.toDTO(userService.getUserById(id));
    }

    @Operation(summary = "Редактирование своего профиля")
    @PreAuthorize("hasRole('USER')")
    @PatchMapping("/me")
    public ResponseEntity<Void> updateMe(@AuthenticationPrincipal(expression = "id") int userId,
                                         @Valid @RequestBody UpdateUserDTO dto) {
        userService.updateUser(userId, dto);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Редактирование профиля любого пользователя")
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateByAdmin(@PathVariable int id,
                                              @Valid @RequestBody UpdateUserDTO dto) {
        userService.updateUser(id, dto);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Изменение системной роли пользователя")
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/role")
    @Validated
    public ResponseEntity<Void> changeRole(@AuthenticationPrincipal(expression = "id") int userId,
                                           @PathVariable int id,
                                           @RequestParam(required = false) SystemRole systemRole) {
        userService.setSystemRole(id, systemRole);
        logger.info("user with id: {} changed systemRole to: {} for user with id: {}", userId,
                systemRole.toString(), id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Заблокировать/разблокировать пользователя")
    @PreAuthorize("hasRole('MODERATOR')")
    @PatchMapping("/{id}/banned")
    public ResponseEntity<Void> setBanned(@AuthenticationPrincipal(expression = "id") int id,
                                          @RequestParam int userId,
                                          @RequestParam boolean banned) {
        checkNotSelf(id, userId);
        userService.setIsBanned(userId, banned);
        logger.info("user with id: {} changed banned status to: {} for user with id: {}", id, banned, userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Активировать/деактивировать свой аккаунт")
    @PreAuthorize("hasRole('USER')")
    @PatchMapping("/me/deleted")
    public ResponseEntity<Void> setSelfDeleted(@AuthenticationPrincipal(expression = "id") int userId,
                                               @RequestParam boolean deleted) {
        userService.setIsDeleted(userId, deleted);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Поиск пользователей по имени, фамилии, возрасту и полу")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/search")
    @Validated
    public ResponseEntity<List<UserDisplayDTO>> searchUsers(@RequestParam(required = false) String firstName,
                                                            @RequestParam(required = false) String secondName,
                                                            @RequestParam(required = false) Integer age,
                                                            @RequestParam(required = false) Gender gender) {
        List<User> result = userService.searchUsers(firstName, secondName, age, gender);
        List<UserDisplayDTO> dtos = result.stream().map(userDisplayMapper::toDTO).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    private void checkCanViewUserProfile(int userId, int targetUserId) {
        if (!permissionCheckService.canViewUserProfile(userId, targetUserId)) {
            throw new AccessDeniedException("you are not authorized to view this profile");
        }
    }

    private void checkNotSelf(int userId, int targetUserId) {
        if (userId == targetUserId) {
            throw new BadRequestException("you can't do this to yourself");
        }
    }
}
