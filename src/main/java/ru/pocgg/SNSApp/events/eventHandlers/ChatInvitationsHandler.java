package ru.pocgg.SNSApp.events.eventHandlers;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import ru.pocgg.SNSApp.events.events.ChatInvitationAcceptedEvent;
import ru.pocgg.SNSApp.events.events.ChatInvitationCreatedEvent;
import ru.pocgg.SNSApp.events.events.ChatInvitationDeclinedEvent;
import ru.pocgg.SNSApp.services.ChatMemberService;
import ru.pocgg.SNSApp.services.NotificationService;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class ChatInvitationsHandler {
    private final ChatMemberService chatMemberService;
    private final NotificationService notificationService;

    @EventListener
    public void onChatInvitationDeclined(ChatInvitationDeclinedEvent event) {
        notificationService.createNotification(event.getId().getSenderId(),
                invitationDeclinedMessage(event.getId().getChatId(), event.getId().getReceiverId()), Instant.now());
    }

    @EventListener
    public void onChatInvitationAccepted(ChatInvitationAcceptedEvent event) {
        chatMemberService.createChatMember(event.getId().getChatId(), event.getId().getReceiverId(), Instant.now());
        notificationService.createNotification(event.getId().getSenderId(),
                invitationAcceptedMessage(event.getId().getChatId(), event.getId().getReceiverId()), Instant.now());
    }

    @EventListener
    public void onChatInvitationCreated(ChatInvitationCreatedEvent event) {
        notificationService.createNotification(event.getId().getReceiverId(),
                invitationCreatedMessage(event.getId().getChatId(), event.getId().getReceiverId()), Instant.now());
    }

    private String invitationCreatedMessage(int chatId, int senderId){
        return "You have received invitation in chat with id: " + chatId + " from user with id: " + senderId;
    }

    private String invitationAcceptedMessage(int chatId, int receiverId){
        return "Your invitation in chat with id: " + chatId + " for user with id: " + receiverId
                + " has been accepted";
    }

    private String invitationDeclinedMessage(int chatId, int receiverId){
        return "Your invitation in chat with id: " + chatId + " for user with id: " + receiverId
                + " has been declined";
    }
}
