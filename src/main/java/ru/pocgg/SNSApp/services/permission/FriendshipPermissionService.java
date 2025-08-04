package ru.pocgg.SNSApp.services.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.pocgg.SNSApp.model.FriendshipRequestId;
import ru.pocgg.SNSApp.model.User;
import ru.pocgg.SNSApp.services.FriendshipRequestService;
import ru.pocgg.SNSApp.services.FriendshipService;
import ru.pocgg.SNSApp.services.UserService;

@Service
@RequiredArgsConstructor
public class FriendshipPermissionService {
    private final FriendshipService friendshipService;
    private final UserService userService;
    private final FriendshipRequestService friendshipRequestService;

    public boolean canSendFriendRequest(int userId, int friendId) {
        return isUserActive(friendId) &&
                userId != friendId &&
                !isFriendshipExist(userId, friendId) &&
                !isFriendshipRequestExist(userId, friendId);
    }

    public boolean canDeleteFriendship(int userId, int friendId) {
        return isFriendshipExist(userId, friendId);
    }

    private boolean isFriendshipRequestExist(int userId, int friendId) {
        FriendshipRequestId id = FriendshipRequestId.builder()
                .senderId(userId)
                .receiverId(friendId)
                .build();
        return friendshipRequestService.isRequestExists(id);
    }

    private boolean isFriendshipExist(int userId, int friendId) {
        return friendshipService.isFriendshipExist(userId, friendId);
    }

    private boolean isUserActive(int userId) {
        User user = userService.getUserById(userId);
        return !user.getDeleted() && !user.getBanned();
    }
}
