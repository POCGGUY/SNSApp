package ru.pocgg.SNSApp.controller.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.pocgg.SNSApp.model.Chat;
import ru.pocgg.SNSApp.DTO.display.ChatDisplayDTO;
import ru.pocgg.SNSApp.DTO.create.CreateChatDTO;
import ru.pocgg.SNSApp.DTO.update.UpdateChatDTO;
import ru.pocgg.SNSApp.DTO.mappers.ChatDisplayMapper;
import ru.pocgg.SNSApp.model.ChatMemberId;
import ru.pocgg.SNSApp.services.ChatMemberService;
import ru.pocgg.SNSApp.services.ChatService;
import ru.pocgg.SNSApp.services.PermissionCheckService;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/chats")
@RequiredArgsConstructor
@Tag(name = "Chat", description = "Управление чатами")
public class ChatRestController extends TemplateController {
    private final ChatService chatService;
    private final ChatDisplayMapper chatMapper;
    private final PermissionCheckService permissionCheckService;

    @Operation(summary = "Создать новый чат")
    @PreAuthorize("hasRole('USER')")
    @PostMapping
    public ResponseEntity<ChatDisplayDTO> createChat(@AuthenticationPrincipal(expression = "id") int userId,
                                                     @Valid @RequestBody CreateChatDTO dto) {
        Chat chat = chatService.createChat(userId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(chatMapper.toDTO(chat));
    }

    @Operation(summary = "Получить информацию о чате по id")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/{id}")
    public ResponseEntity<ChatDisplayDTO> getChat(@AuthenticationPrincipal(expression = "id") int userId,
                                                  @PathVariable int id) {
        Chat chat = chatService.getChatById(id);
        checkCanViewChat(userId, chat);
        return ResponseEntity.ok(chatMapper.toDTO(chat));
    }

    @Operation(summary = "Получить список всех чатов (только модераторы и выше)")
    @PreAuthorize("hasRole('MODERATOR')")
    @GetMapping
    public ResponseEntity<List<ChatDisplayDTO>> listChats() {
        List<ChatDisplayDTO> DTOs = getDTOs(chatService.getAllChats());
        return ResponseEntity.ok(DTOs);
    }

    @Operation(summary = "Редактировать чат")
    @PreAuthorize("hasRole('USER')")
    @PatchMapping("/{id}/edit")
    public ResponseEntity<Void> editChat(@AuthenticationPrincipal(expression = "id") int userId,
                                         @PathVariable int id,
                                         @Valid @RequestBody UpdateChatDTO dto) {
        checkCanEditChat(userId, id);
        chatService.updateChat(id, dto);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Деактивировать чат")
    @PreAuthorize("hasRole('USER')")
    @PatchMapping("/{id}/delete")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal(expression = "id") int userId,
                                       @PathVariable int id) {
        checkCanDeleteChat(userId, id);
        chatService.setDeleted(id, true);
        return ResponseEntity.ok().build();
    }

    private List<ChatDisplayDTO> getDTOs(List<Chat> chats) {
        return chats.stream()
                .map(chatMapper::toDTO)
                .collect(Collectors.toList());
    }

    private void checkCanDeleteChat(int userId, int chatId) {
        if (!permissionCheckService.isUserChatOwnerOrSystemModerator(userId, chatId)) {
            throw new AccessDeniedException("You are not allowed to delete this chat");
        }
    }

    private void checkCanViewChat(int userId, Chat chat) {
        if (!permissionCheckService.canViewChat(userId, chat)) {
            throw new AccessDeniedException("You are not allowed to view this chat");
        }
    }

    private void checkCanEditChat(int userId, int chatId) {
        if (!permissionCheckService.canEditChat(userId, chatId)) {
            throw new AccessDeniedException("You are not allowed to edit this chat");
        }
    }
}
