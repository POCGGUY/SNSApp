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
import ru.pocgg.SNSApp.model.ChatInvitation;
import ru.pocgg.SNSApp.model.ChatInvitationId;
import ru.pocgg.SNSApp.DTO.display.ChatInvitationDisplayDTO;
import ru.pocgg.SNSApp.DTO.create.CreateChatInvitationDTO;
import ru.pocgg.SNSApp.DTO.mappers.display.ChatInvitationDisplayMapper;
import ru.pocgg.SNSApp.services.*;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/chatInvitations")
@RequiredArgsConstructor
@Tag(name = "Chat Invitation", description = "Приглашения в чаты")
public class ChatInvitationRestController {

    private final ChatInvitationService chatInvitationService;
    private final UserService userService;
    private final ChatService chatService;
    private final ChatInvitationDisplayMapper chatInvitationDisplayMapper;

    @Operation(summary = "Создать приглашение в чат")
    @PreAuthorize("hasRole('USER') and @chatPermissionService.canInviteToChat(principal.id, #receiverId, #chatId)")
    @PostMapping
    public ResponseEntity<ChatInvitationDisplayDTO> create(@AuthenticationPrincipal(expression = "id") int senderId,
                                                           @RequestParam int chatId,
                                                           @RequestParam int receiverId,
                                                           @Valid @RequestBody CreateChatInvitationDTO dto) {
        ChatInvitation chatInvitation = chatInvitationService.createChatInvitation(senderId, receiverId, chatId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(chatInvitationDisplayMapper.toDTO(chatInvitation));
    }

    @Operation(summary = "Список ваших отправленных приглашений")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/sent")
    public ResponseEntity<List<ChatInvitationDisplayDTO>> sent(@AuthenticationPrincipal(expression = "id")
                                                                   int senderId) {
        return ResponseEntity.ok(getSortedByCreationDateAndMapToDTO(chatInvitationService.getBySender(senderId)));
    }

    @Operation(summary = "Список приглашений, адресованных вам")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/received")
    public ResponseEntity<List<ChatInvitationDisplayDTO>> received(@AuthenticationPrincipal(expression = "id")
                                                                       int receiverId) {
        return ResponseEntity.ok(getSortedByCreationDateAndMapToDTO(chatInvitationService.getByReceiver(receiverId)));
    }

    @Operation(summary = "Список приглашений этого чата")
    @PreAuthorize("hasRole('USER') and @chatPermissionService.canViewInvitations(principal.id, #chatId)")
    @GetMapping("/byChat")
    public ResponseEntity<List<ChatInvitationDisplayDTO>> byChat(@RequestParam int chatId) {
        return ResponseEntity.ok(getSortedByCreationDateAndMapToDTO(chatInvitationService.getByChat(chatId)));
    }

    @Operation(summary = "Удалить ваше приглашение")
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping
    public ResponseEntity<Void> delete(@AuthenticationPrincipal(expression = "id") int senderId,
                                       @RequestParam int chatId,
                                       @RequestParam int receiverId) {
        ChatInvitationId id = ChatInvitationId.builder()
                .senderId(senderId)
                .receiverId(receiverId)
                .chatId(chatId)
                .build();
        chatInvitationService.removeInvitation(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Принять приглашение")
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/accept")
    public ResponseEntity<Void> accept(@AuthenticationPrincipal(expression = "id") int receiverId,
                                       @RequestParam int chatId,
                                       @RequestParam int senderId) {
        ChatInvitationId id = ChatInvitationId.builder()
                .senderId(senderId)
                .receiverId(receiverId)
                .chatId(chatId)
                .build();
        chatInvitationService.acceptInvitation(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Отклонить приглашение")
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/decline")
    public ResponseEntity<Void> decline(@AuthenticationPrincipal(expression = "id") int receiverId,
                                        @RequestParam int chatId,
                                        @RequestParam int senderId) {
        ChatInvitationId id = ChatInvitationId.builder()
                .senderId(senderId)
                .receiverId(receiverId)
                .chatId(chatId)
                .build();
        chatInvitationService.declineInvitation(id);
        return ResponseEntity.noContent().build();
    }

    private List<ChatInvitationDisplayDTO> getSortedByCreationDateAndMapToDTO(List<ChatInvitation> list){
        return list.stream().sorted(Comparator.comparing(ChatInvitation::getCreationDate).reversed())
                .map(chatInvitationDisplayMapper::toDTO).collect(Collectors.toList());
    }
}

