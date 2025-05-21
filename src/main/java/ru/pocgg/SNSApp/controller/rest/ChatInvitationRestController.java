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
import ru.pocgg.SNSApp.model.ChatInvitation;
import ru.pocgg.SNSApp.model.ChatInvitationId;
import ru.pocgg.SNSApp.DTO.display.ChatInvitationDisplayDTO;
import ru.pocgg.SNSApp.DTO.create.CreateChatInvitationDTO;
import ru.pocgg.SNSApp.DTO.mappers.ChatInvitationDisplayMapper;
import ru.pocgg.SNSApp.model.exceptions.BadRequestException;
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
    private final PermissionCheckService permissionCheckService;
    private final UserService userService;
    private final ChatService chatService;
    private final ChatInvitationDisplayMapper chatInvitationDisplayMapper;
    private final ChatMemberService chatMemberService;

    @Operation(summary = "Создать приглашение в чат")
    @PreAuthorize("hasRole('USER')")
    @PostMapping
    public ResponseEntity<ChatInvitationDisplayDTO> create(@AuthenticationPrincipal(expression = "id") int senderId,
                                                           @RequestParam int chatId,
                                                           @RequestParam int receiverId,
                                                           @Valid @RequestBody CreateChatInvitationDTO dto) {
        checkCanUserCreateInvitation(senderId, chatId);
        checkNotSelf(senderId, receiverId);
        checkIsUserNotAChatMemberAlready(chatId, receiverId);
        checkIfInvitationExistAlready(senderId, receiverId, chatId);
        checkIsPublic(chatId);
        checkIsReceiverActive(receiverId);
        checkIsChatDeleted(chatId);
        userService.getUserById(receiverId);
        chatService.getChatById(chatId);
        ChatInvitation chatInvitation = chatInvitationService.createChatInvitation(senderId, receiverId, chatId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(chatInvitationDisplayMapper.toDTO(chatInvitation));
    }

    @Operation(summary = "Список ваших отправленных приглашений")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/sent")
    public ResponseEntity<List<ChatInvitationDisplayDTO>> sent(@AuthenticationPrincipal(expression = "id")
                                                                   int senderId) {
        return ResponseEntity.ok(getSortedByCreationDateAndMappedToDTO(chatInvitationService.getBySender(senderId)));
    }

    @Operation(summary = "Список приглашений, адресованных вам")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/received")
    public ResponseEntity<List<ChatInvitationDisplayDTO>> received(@AuthenticationPrincipal(expression = "id")
                                                                       int receiverId) {
        return ResponseEntity.ok(getSortedByCreationDateAndMappedToDTO
                (chatInvitationService.getByReceiver(receiverId)));
    }

    @Operation(summary = "Список приглашений этого чата")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/byChat")
    public ResponseEntity<List<ChatInvitationDisplayDTO>> byChat(@AuthenticationPrincipal(expression = "id") int userId,
                                                       @RequestParam int chatId) {
        checkCanUserViewInvitationsInChat(userId, chatId);
        return ResponseEntity.ok(getSortedByCreationDateAndMappedToDTO(chatInvitationService.getByChat(chatId)));
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

    private void checkIsChatDeleted(int chatId) {
        Chat chat = chatService.getChatById(chatId);
        if(chat.isDeleted()){
            throw new BadRequestException("You cant invite user to this chat");
        }
    }

    private void checkIsReceiverActive(int receiverId) {
        if(!permissionCheckService.isUserActive(receiverId)) {
            throw new BadRequestException("You cant invite deactivated user");
        }
    }

    private void checkNotSelf(int senderId, int receiverId) {
        if(senderId == receiverId) {
            throw new BadRequestException("You cant invite yourself");
        }
    }

    private List<ChatInvitationDisplayDTO> getSortedByCreationDateAndMappedToDTO(List<ChatInvitation> list){
        return list.stream().sorted(Comparator.comparing(ChatInvitation::getCreationDate).reversed())
                .map(chatInvitationDisplayMapper::toDTO).collect(Collectors.toList());
    }

    private void checkIsUserNotAChatMemberAlready(int chatId, int receiverId) {
        if(permissionCheckService.isUserChatMember(receiverId, chatId)){
            throw new BadRequestException("This user is already member of this chat");
        }
    }

    private void checkCanUserCreateInvitation(int senderId, int chatId){
        if(!permissionCheckService.isUserChatOwner(senderId, chatId)) {
            throw new AccessDeniedException("You are not allowed to invite to this chat");
        }
    }

    private void checkIsPublic(int chatId){
        Chat chat = chatService.getChatById(chatId);
        if(!chat.isPrivate()){
            throw new AccessDeniedException("you cant send invitations in public chat");
        }
    }

    private void checkIfInvitationExistAlready(int senderId, int receiverId, int chatId) {
        ChatInvitationId id = ChatInvitationId.builder()
                .senderId(senderId)
                .receiverId(receiverId)
                .chatId(chatId)
                .build();
        if(chatInvitationService.isInvitationExist(id)){
            throw new BadRequestException("this user already invited in this chat");
        }
    }

    private void checkCanUserViewInvitationsInChat(int userId, int chatId){
        if(!permissionCheckService.canUserViewInvitationsInChat(userId, chatId)) {
            throw new AccessDeniedException("You are not allowed to view invitations for this chat");
        }
    }
}

