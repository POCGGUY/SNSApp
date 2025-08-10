package ru.pocgg.SNSApp.services.DAO.interfaces;

import ru.pocgg.SNSApp.model.Notification;

import java.util.List;

public interface NotificationServiceDAO {
    Notification getNotificationById(int id);
    List<Notification> getAllNotifications();
    List<Notification> getAllNotificationsByReceiverId(int receiverId);
    List<Notification> getNotSeenNotificationsByReceiverId(int receiverId);
    void addNotification(Notification notification);
    void removeNotification(Notification notification);
    void forceFlush();
}
