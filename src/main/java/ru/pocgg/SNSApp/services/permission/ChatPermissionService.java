package ru.pocgg.SNSApp.services.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.pocgg.SNSApp.model.*;
import ru.pocgg.SNSApp.services.*;

@Service
@RequiredArgsConstructor
public class ChatPermissionService {
    private final ChatService chatService;
    private final ChatMemberService chatMemberService;
    private final ChatMessageService chatMessageService;
    private final UserService userService;
    private final ChatInvitationService chatInvitationService;

    public boolean canViewChat(int userId, int chatId) {
        Chat chat = chatService.getChatById(chatId);
        return !chat.isDeleted()
                && (!chat.isPrivate() || isChatMember(userId, chatId) || isSystemModerator(userId));
    }

    public boolean canDeleteChat(int userId, int chatId) {
        return isChatActive(chatId) && (isChatOwner(userId, chatId) || isSystemModerator(userId));
    }

    public boolean canViewChatMessages(int userId, int chatId) {
        return isChatActive(chatId) && (isChatMember(userId, chatId) || isSystemModerator(userId));
    }

    public boolean canViewChatMessage(int userId, int messageId) {
        ChatMessage message = chatMessageService.getChatMessageById(messageId);
        return canViewChatMessages(userId, message.getChat().getId());
    }

    public boolean canLeaveChat(int userId, int chatId) {
        return isChatMember(userId, chatId) && !isChatOwner(userId, chatId);
    }

    public boolean canViewChatMembers(int userId, int chatId) {
        return canViewChat(userId, chatId);
    }

    public boolean canRemoveChatMember(int userId, int chatId, int memberId) {
        return (isSystemModerator(userId) || isChatOwner(userId, chatId)) && !isChatOwner(memberId, chatId);
    }

    public boolean canEditChat(int userId, int chatId) {
        return isChatActive(chatId) && (isChatOwner(userId, chatId) || isSystemModerator(userId));
    }

    public boolean canViewInvitations(int userId, int chatId) {
        return isSystemModerator(userId)
                || (isChatOwner(userId, chatId) && isChatMember(userId, chatId));
    }

    public boolean canSendMessage(int userId, int chatId) {
        return isChatActive(chatId) && isChatMember(userId, chatId);
    }

    public boolean canJoinChat(int userId, int chatId) {
        return isChatActive(chatId) && !isChatPrivate(chatId) && !isChatMember(userId, chatId);
    }

    public boolean canModifyMessage(int userId, int messageId) {
        ChatMessage message = chatMessageService.getChatMessageById(messageId);
        int chatId = message.getChat().getId();
        return isChatActive(chatId)
                && isChatMember(userId, chatId)
                && message.getSender().getId() == userId;
    }

    public boolean canInviteToChat(int senderId, int receiverId, int chatId) {
        return isChatActive(chatId) &&
                isChatOwner(senderId, chatId) &&
                isNotSelf(senderId, receiverId) &&
                isUserNotAChatMemberAlready(chatId, receiverId) &&
                !isInvitationExist(senderId, receiverId, chatId) &&
                isChatPrivate(chatId) &&
                isUserActive(receiverId);
    }

    public boolean canDeleteMessage(int userId, int messageId) {
        ChatMessage message = chatMessageService.getChatMessageById(messageId);
        int chatId = message.getChat().getId();
        return canModifyMessage(userId, messageId)
                || (isChatActive(chatId) && (isChatOwner(userId, chatId) || isSystemModerator(userId)));
    }

    private boolean isChatMember(int userId, int chatId) {
        return chatMemberService.isChatMemberExist(new ChatMemberId(chatId, userId));
    }

    private boolean isChatOwner(int userId, int chatId) {
        return chatService.getChatById(chatId).getOwner().getId() == userId;
    }

    private boolean isChatActive(int chatId) {
        return !chatService.getChatById(chatId).isDeleted();
    }

    private boolean isSystemModerator(int userId) {
        return userService.getUserById(userId).isModerator();
    }

    private boolean checkIsChatDeleted(int chatId) {
        Chat chat = chatService.getChatById(chatId);
        return chat.isDeleted();
    }

    private boolean checkIsReceiverActive(int receiverId) {
        return isUserActive(receiverId);
    }

    private boolean isNotSelf(int senderId, int receiverId) {
        return senderId != receiverId;
    }

    private boolean isUserNotAChatMemberAlready(int chatId, int receiverId) {
        return !chatMemberService.isChatMemberExist(ChatMemberId.builder().chatId(chatId).memberId(receiverId).build());
    }

    private boolean isChatPrivate(int chatId) {
        Chat chat = chatService.getChatById(chatId);
        return chat.isPrivate();
    }

    private boolean isInvitationExist(int senderId, int receiverId, int chatId) {
        ChatInvitationId id = ChatInvitationId.builder()
                .senderId(senderId)
                .receiverId(receiverId)
                .chatId(chatId)
                .build();
        return chatInvitationService.isInvitationExist(id);
    }

    public boolean isUserActive(int userId) {
        User user = userService.getUserById(userId);
        return !user.getDeleted() && !user.getBanned();
    }
}
