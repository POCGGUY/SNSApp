package ru.pocgg.SNSApp.events.eventHandlers;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import ru.pocgg.SNSApp.events.events.FriendshipRequestAcceptedEvent;
import ru.pocgg.SNSApp.events.events.FriendshipRequestDeclinedEvent;
import ru.pocgg.SNSApp.events.events.FriendshipRequestCreatedEvent;
import ru.pocgg.SNSApp.services.FriendshipService;
import ru.pocgg.SNSApp.services.NotificationService;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class FriendshipRequestEventHandler {
    private final FriendshipService friendshipService;
    private final NotificationService notificationService;

    @EventListener
    private void onFriendshipRequestDeclined(FriendshipRequestDeclinedEvent event) {
        notificationService.createNotification(event.getId().getSenderId(),
                requestDeclinedMessage(event.getId().getReceiverId()), Instant.now());
    }

    public String requestDeclinedMessage(int receiverId){
        return "Your friend request to user with id: " + receiverId + " has been declined";
    }

    @EventListener
    private void onFriendshipRequestAccepted(FriendshipRequestAcceptedEvent event) {
        friendshipService.createFriendship(event.getId().getSenderId(), event.getId().getReceiverId(), Instant.now());
        notificationService.createNotification(event.getId().getSenderId(),
                requestAcceptedMessage(event.getId().getReceiverId()), Instant.now());
    }

    public String requestAcceptedMessage(int receiverId){
        return "Your friend request to user with id: " + receiverId + " has been accepted, you are friends now!";
    }

    @EventListener
    private void onFriendshipRequestCreated(FriendshipRequestCreatedEvent event) {
        notificationService.createNotification(event.getId().getReceiverId(),
                requestCreatedMessage(event.getId().getSenderId()), Instant.now());
    }

    public String requestCreatedMessage(int senderId){
        return "You have received a friend request from user with id: " + senderId;
    }
}
