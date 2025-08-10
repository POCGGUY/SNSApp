package ru.pocgg.SNSApp.services;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import ru.pocgg.SNSApp.model.Notification;
import ru.pocgg.SNSApp.model.exceptions.EntityNotFoundException;
import ru.pocgg.SNSApp.services.DAO.interfaces.NotificationServiceDAO;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService extends TemplateService{
    private final NotificationServiceDAO notificationServiceDAO;
    private final UserService userService;

    public Notification createNotification(int receiverId, String description, Instant creationDate) {
        Notification notification = Notification.builder()
                .receiver(userService.getUserById(receiverId))
                .description(description)
                .creationDate(creationDate)
                .read(false).build();
        notificationServiceDAO.addNotification(notification);
        notificationServiceDAO.forceFlush();
        logger.info("created notification for receiver with id: {}, notification id: {}",
                receiverId, notification.getId());
        return notification;
    }

    @Transactional(readOnly = true)
    public List<Notification> getAllNotifications() {
        return notificationServiceDAO.getAllNotifications();
    }

    @Transactional(readOnly = true)
    public List<Notification> getAllNotificationsByReceiverId(int receiverId) {
        return notificationServiceDAO.getAllNotificationsByReceiverId(receiverId);
    }

    @Transactional(readOnly = true)
    public List<Notification> getNotSeenNotificationsByReceiverId(int receiverId) {
        return notificationServiceDAO.getNotSeenNotificationsByReceiverId(receiverId);
    }

    @Transactional(readOnly = true)
    public Notification getNotificationById(int id) {
        return getNotificationByIdOrThrowException(id);
    }

    public void setRead(int id, boolean value) {
        Notification notification = getNotificationByIdOrThrowException(id);
        notification.setRead(value);
        logger.info("notification with id: {} has been set to read", id);
    }

    public void delete(int id) {
        Notification notification = getNotificationByIdOrThrowException(id);
        notificationServiceDAO.removeNotification(notification);
        logger.info("notification with id: {} has been deleted", id);
    }

    private Notification getNotificationByIdOrThrowException(int id) {
        Notification notification = notificationServiceDAO.getNotificationById(id);
        if (notification == null) {
            throw new EntityNotFoundException("notification with id " + id + " not found");
        }
        return notification;
    }
}
