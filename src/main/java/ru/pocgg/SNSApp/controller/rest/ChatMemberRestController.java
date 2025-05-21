package ru.pocgg.SNSApp.controller.rest;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.pocgg.SNSApp.DTO.display.ChatMemberDisplayDTO;
import ru.pocgg.SNSApp.DTO.mappers.ChatMemberDisplayMapper;
import ru.pocgg.SNSApp.model.Chat;
import ru.pocgg.SNSApp.model.ChatMember;
import ru.pocgg.SNSApp.model.ChatMemberId;
import ru.pocgg.SNSApp.services.*;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/chats/{chatId}/members")
@RequiredArgsConstructor
@Tag(name = "Chat Member", description = "Управление участниками чата")
public class ChatMemberRestController extends TemplateController {

    private final ChatMemberService chatMemberService;
    private final ChatMemberDisplayMapper chatMemberDisplayMapper;
    private final PermissionCheckService permissionCheckService;
    private final ChatInvitationService chatInvitationService;
    private final ChatService chatService;

    @Operation(summary = "Вступить в чат")
    @PreAuthorize("hasRole('USER')")
    @PostMapping
    public ResponseEntity<ChatMemberDisplayDTO> addMember(@AuthenticationPrincipal(expression = "id") int userId,
                                                          @PathVariable int chatId) {
        checkIsAlreadyMember(chatId, userId);
        checkCanJoin(chatId, userId);
        ChatMember member = chatMemberService.createChatMember(chatId, userId, Instant.now());
        ChatMemberDisplayDTO dto = chatMemberDisplayMapper.toDTO(member);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @Operation(summary = "Список всех участников чата")
    @PreAuthorize("hasRole('USER')")
    @GetMapping
    public ResponseEntity<List<ChatMemberDisplayDTO>> listMembers(@AuthenticationPrincipal(expression = "id") int userId,
                                                                  @PathVariable int chatId) {
        checkIsMember(chatId, userId);
        List<ChatMemberDisplayDTO> list = getDTOsSortedByEntryDate(chatMemberService.getChatMembersByChatId(chatId));
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "Получить участника по ID")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/{memberId}")
    public ResponseEntity<ChatMemberDisplayDTO> getMember(@AuthenticationPrincipal(expression = "id") int userId,
                                                          @PathVariable int chatId,
                                                          @PathVariable int memberId) {
        checkIsMember(chatId, userId);
        ChatMember member = chatMemberService.getChatMemberById(new ChatMemberId(chatId, memberId));
        return ResponseEntity.ok(chatMemberDisplayMapper.toDTO(member));
    }

    @Operation(summary = "Удалить участника из чата")
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/{memberId}")
    public ResponseEntity<Void> removeMember(@AuthenticationPrincipal(expression = "id") int userId,
                                             @PathVariable int chatId,
                                             @PathVariable int memberId) {
        checkCanRemove(chatId, userId);
        chatMemberService.deleteChatMember(new ChatMemberId(chatId, memberId));
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Покинуть чат")
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/me")
    public ResponseEntity<Void> leaveChat(@AuthenticationPrincipal(expression = "id") int userId,
                                             @PathVariable int chatId) {
        checkIsMember(chatId, userId);
        chatMemberService.deleteChatMember(new ChatMemberId(chatId, userId));
        return ResponseEntity.noContent().build();
    }

    private List<ChatMemberDisplayDTO> getDTOsSortedByEntryDate(List<ChatMember> chatMembers) {
        return chatMembers.stream()
                .sorted(Comparator.comparing(ChatMember::getEntryDate).reversed())
                .map(chatMemberDisplayMapper::toDTO)
                .collect(Collectors.toList());
    }

    private void checkCanJoin(int chatId, int userId) {
        Chat chat = chatService.getChatById(chatId);
        if (chat.isDeleted()) {
            throw new AccessDeniedException("you are not authorized to join this chat");
        } else if(chat.isPrivate() && !(chat.getOwner().getId() == userId)) {
            throw new AccessDeniedException("you are not authorized to join this chat");
        }
    }

    private void checkIsMember(int chatId, int userId) {
        if (!permissionCheckService.isUserChatMember(userId, chatId)) {
            throw new AccessDeniedException("you are not a member of this chat");
        }
    }

    private void checkIsAlreadyMember(int chatId, int userId) {
        if (permissionCheckService.isUserChatMember(userId, chatId)) {
            throw new AccessDeniedException("you are already a member of this chat");
        }
    }

    private void checkCanRemove(int chatId, int userId) {
        if (!permissionCheckService.isUserChatOwnerOrSystemModerator(userId, chatId)) {
            throw new AccessDeniedException("you are not allowed to remove members from this chat");
        }
    }
}


