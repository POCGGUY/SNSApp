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
import ru.pocgg.SNSApp.model.ChatMessage;
import ru.pocgg.SNSApp.DTO.display.ChatMessageDisplayDTO;
import ru.pocgg.SNSApp.DTO.create.CreateChatMessageDTO;
import ru.pocgg.SNSApp.DTO.update.UpdateChatMessageDTO;
import ru.pocgg.SNSApp.DTO.mappers.display.ChatMessageDisplayMapper;
import ru.pocgg.SNSApp.services.*;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/chats/{chatId}/messages")
@RequiredArgsConstructor
@Tag(name = "Chat Message", description = "Управление сообщениями в чате")
public class ChatMessageRestController extends TemplateController {

    private final ChatMessageService chatMessageService;
    private final ChatMessageDisplayMapper messageMapper;

    @Operation(summary = "Создать новое сообщение в чате")
    @PreAuthorize("hasRole('USER') and @chatPermissionService.canSendMessage(principal.id, #chatId)")
    @PostMapping
    public ResponseEntity<ChatMessageDisplayDTO> createMessage(@PathVariable int chatId,
                                                               @AuthenticationPrincipal(expression = "id") int userId,
                                                               @Valid @RequestBody CreateChatMessageDTO dto) {
        ChatMessage chatMessage = chatMessageService.createChatMessage(chatId, userId, dto);
        ChatMessageDisplayDTO body = messageMapper.toDTO(chatMessage);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @Operation(summary = "Получить все сообщения чата")
    @PreAuthorize("hasRole('USER') and @chatPermissionService.canViewChatMessages(principal.id, #chatId)")
    @GetMapping
    public ResponseEntity<List<ChatMessageDisplayDTO>> listMessages(@AuthenticationPrincipal(expression = "id") int userId,
                                                                    @PathVariable int chatId) {
        List<ChatMessageDisplayDTO> dtos = getDTOsSortedByCreationDate(chatMessageService.getMessagesByChatId(chatId));
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Получить сообщение по id")
    @PreAuthorize("hasRole('USER') and @chatPermissionService.canViewChatMessage(principal.id, #messageId)")
    @GetMapping("/{messageId}")
    public ResponseEntity<ChatMessageDisplayDTO> getMessage(@PathVariable int messageId) {
        ChatMessage message = chatMessageService.getChatMessageById(messageId);
        ChatMessageDisplayDTO body = messageMapper.toDTO(message);
        return ResponseEntity.ok(body);
    }

    @Operation(summary = "Редактировать сообщение")
    @PreAuthorize("hasRole('USER') and @chatPermissionService.canModifyMessage(principal.id, #messageId)")
    @PatchMapping("/{messageId}")
    public ResponseEntity<Void> updateMessage(@PathVariable int messageId,
                                              @Valid @RequestBody UpdateChatMessageDTO dto){
        chatMessageService.updateChatMessage(messageId, dto);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Удалить сообщение из чата")
    @PreAuthorize("hasRole('USER') and @chatPermissionService.canDeleteMessage(principal.id, #messageId)")
    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(@PathVariable int messageId,
                                              @AuthenticationPrincipal(expression = "id") int userId) {
        chatMessageService.setDeleted(messageId, true);
        return ResponseEntity.noContent().build();
    }

    private List<ChatMessageDisplayDTO> getDTOsSortedByCreationDate(List<ChatMessage> messages) {
        return messages.stream().sorted(Comparator.comparing(ChatMessage::getSendingDate).reversed())
                .map(messageMapper::toDTO)
                .collect(Collectors.toList());
    }
}

