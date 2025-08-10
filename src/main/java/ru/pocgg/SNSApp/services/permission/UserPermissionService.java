package ru.pocgg.SNSApp.services.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.pocgg.SNSApp.model.User;
import ru.pocgg.SNSApp.services.UserService;

@Service
@RequiredArgsConstructor
public class UserPermissionService {
    private final UserService userService;

    public boolean canBanUser(int userId, int targetUserId){
        return userId != targetUserId;
    }

    public boolean canViewUserProfile(int userId, int targetUserId) {
        return isUserActive(targetUserId) || isUserSystemModerator(userId);
    }

    private boolean isUserActive(int userId) {
        User user = userService.getUserById(userId);
        return !user.getDeleted() && !user.getBanned();
    }

    private boolean isUserSystemModerator(int userId){
        return userService.getUserById(userId).isModerator();
    }
}
