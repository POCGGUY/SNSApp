package ru.pocgg.SNSApp.services.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.pocgg.SNSApp.model.*;
import ru.pocgg.SNSApp.services.*;

@Service
@RequiredArgsConstructor
public class PrivateMessagePermissionService {
    private final PrivateMessageService privateMessageService;
    private final UserService userService;
    private final FriendshipService friendshipService;

    public boolean canSendPrivateMessage(int senderId, int receiverId) {
        if (!isActive(receiverId)) return false;
        boolean accepts = userService.getUserById(receiverId).getAcceptingPrivateMsgs();
        return accepts
                || friendshipService.isFriendshipExist(senderId, receiverId);
    }

    public boolean canReadMessage(int userId, int messageId) {
        PrivateMessage message = privateMessageService.getById(messageId);
        return message.getSender().getId() == userId
                || message.getReceiver().getId() == userId;
    }

    public boolean canModifyMessage(int userId, int messageId) {
        return privateMessageService.getById(messageId)
                .getSender().getId() == userId;
    }

    public boolean canDeleteMessage(int userId, int messageId) {
        return canModifyMessage(userId, messageId)
                || isSystemModerator(userId);
    }

    private boolean isActive(int userId) {
        User user = userService.getUserById(userId);
        return !user.getDeleted() && !user.getBanned();
    }

    private boolean isSystemModerator(int userId) {
        return userService.getUserById(userId).isModerator();
    }
}
