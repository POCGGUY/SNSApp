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
import ru.pocgg.SNSApp.DTO.mappers.ChatMessageDisplayMapper;
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
    private final PermissionCheckService permissionCheckService;

    @Operation(summary = "Создать новое сообщение в чате")
    @PreAuthorize("hasRole('USER')")
    @PostMapping
    public ResponseEntity<ChatMessageDisplayDTO> createMessage(@PathVariable int chatId,
                                                               @AuthenticationPrincipal(expression = "id") int userId,
                                                               @Valid @RequestBody CreateChatMessageDTO dto) {
        checkCanCreate(userId, chatId);
        ChatMessage chatMessage = chatMessageService.createChatMessage(chatId, userId, dto);
        ChatMessageDisplayDTO body = messageMapper.toDTO(chatMessage);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @Operation(summary = "Получить все сообщения чата")
    @PreAuthorize("hasRole('USER')")
    @GetMapping
    public ResponseEntity<List<ChatMessageDisplayDTO>> listMessages(@AuthenticationPrincipal(expression = "id") int userId,
                                                                    @PathVariable int chatId) {
        checkCanView(userId, chatId);
        List<ChatMessageDisplayDTO> dtos = getDTOsSortedByCreationDate(chatMessageService.getMessagesByChatId(chatId));
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Получить сообщение по id")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/{messageId}")
    public ResponseEntity<ChatMessageDisplayDTO> getMessage(@AuthenticationPrincipal(expression = "id") int userId,
                                                            @PathVariable int messageId) {
        ChatMessage message = chatMessageService.getChatMessageById(messageId);
        checkCanView(userId, message.getChat().getId());
        ChatMessageDisplayDTO body = messageMapper.toDTO(message);
        return ResponseEntity.ok(body);
    }

    @Operation(summary = "Редактировать сообщение")
    @PreAuthorize("hasRole('USER')")
    @PatchMapping("/{messageId}")
    public ResponseEntity<Void> updateMessage(@PathVariable int messageId,
                                              @Valid @RequestBody UpdateChatMessageDTO dto,
                                              @AuthenticationPrincipal(expression = "id") int userId) {
        checkCanModify(userId, messageId);
        chatMessageService.updateChatMessage(messageId, dto);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Удалить сообщение из чата")
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(@PathVariable int messageId,
                                              @AuthenticationPrincipal(expression = "id") int userId) {
        checkCanDelete(userId, messageId);
        chatMessageService.setDeleted(messageId, true);
        return ResponseEntity.noContent().build();
    }

    private List<ChatMessageDisplayDTO> getDTOsSortedByCreationDate(List<ChatMessage> messages) {
        return messages.stream().sorted(Comparator.comparing(ChatMessage::getSendingDate).reversed())
                .map(messageMapper::toDTO)
                .collect(Collectors.toList());
    }

    private void checkCanCreate(int userId, int chatId) {
        if (!permissionCheckService.canUserCreateMessageInChat(userId, chatId)) {
            throw new AccessDeniedException("You are not authorized to create messages in this chat");
        }
    }

    private void checkCanView(int userId, int chatId) {
        if (!permissionCheckService.canUserViewMessagesInChat(userId, chatId)) {
            throw new AccessDeniedException("You are not authorized to view messages in this chat");
        }
    }

    private void checkCanModify(int userId, int messageId) {
        if (!permissionCheckService.canUserModifyChatMessage(userId, messageId)) {
            throw new AccessDeniedException("You are not authorized to modify this message");
        }
    }

    private void checkCanDelete(int userId, int messageId) {
        if (!permissionCheckService.canUserDeleteChatMessage(userId, messageId)) {
            throw new AccessDeniedException("You are not authorized to delete this message");
        }
    }
}

