package ru.pocgg.SNSApp.controller.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.pocgg.SNSApp.model.Chat;
import ru.pocgg.SNSApp.DTO.display.ChatDisplayDTO;
import ru.pocgg.SNSApp.DTO.create.CreateChatDTO;
import ru.pocgg.SNSApp.DTO.update.UpdateChatDTO;
import ru.pocgg.SNSApp.DTO.mappers.display.ChatDisplayMapper;
import ru.pocgg.SNSApp.services.ChatService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/chats")
@RequiredArgsConstructor
@Tag(name = "Chat", description = "Управление чатами")
public class ChatRestController extends TemplateController {
    private final ChatService chatService;
    private final ChatDisplayMapper chatMapper;

    @Operation(summary = "Создать новый чат")
    @PreAuthorize("hasRole('USER')")
    @PostMapping
    public ResponseEntity<ChatDisplayDTO> createChat(@AuthenticationPrincipal(expression = "id") int userId,
                                                     @Valid @RequestBody CreateChatDTO dto) {
        Chat chat = chatService.createChat(userId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(chatMapper.toDTO(chat));
    }

    @Operation(summary = "Получить информацию о чате по id")
    @PreAuthorize("hasRole('USER') and @chatPermissionService.canViewChat(#principal.id, id)")
    @GetMapping("/{id}")
    public ResponseEntity<ChatDisplayDTO> getChat(@PathVariable int id) {
        Chat chat = chatService.getChatById(id);
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
    @PreAuthorize("hasRole('USER') and @chatPermissionService.canEditChat(principal.id, #id)")
    @PatchMapping("/{id}/edit")
    public ResponseEntity<Void> editChat(@PathVariable int id,
                                         @Valid @RequestBody UpdateChatDTO dto) {
        chatService.updateChat(id, dto);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Деактивировать чат")
    @PreAuthorize("hasRole('USER') and @chatPermissionService.canDeleteChat(principal.id, #id)")
    @PatchMapping("/{id}/delete")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        chatService.setDeleted(id, true);
        return ResponseEntity.ok().build();
    }

    private List<ChatDisplayDTO> getDTOs(List<Chat> chats) {
        return chats.stream()
                .map(chatMapper::toDTO)
                .collect(Collectors.toList());
    }
}
