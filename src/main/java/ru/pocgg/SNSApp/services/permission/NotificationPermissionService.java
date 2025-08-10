package ru.pocgg.SNSApp.services.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.pocgg.SNSApp.model.Notification;
import ru.pocgg.SNSApp.services.NotificationService;
import ru.pocgg.SNSApp.services.UserService;

@Service
@RequiredArgsConstructor
public class NotificationPermissionService {
    private final NotificationService notificationService;
    private final UserService userService;

    public boolean canMarkRead(int userId, int notificationId) {
        return canEditNotification(userId, notificationId);
    }

    public boolean canDelete(int userId, int notificationId) {
        return canEditNotification(userId, notificationId);
    }

    private boolean canEditNotification(int userId, int notificationId) {
        return isNotificationOwner(userId, notificationId) || isUserSystemModerator(userId);
    }

    private boolean isNotificationOwner(int userId, int notificationId) {
        Notification notification = notificationService.getNotificationById(notificationId);
        return notification.getReceiver().getId() == userId;
    }

    public boolean isUserSystemModerator(int userId){
        return userService.getUserById(userId).isModerator();
    }
}
