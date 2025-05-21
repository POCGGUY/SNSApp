package ru.pocgg.SNSApp.events.eventHandlers;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import ru.pocgg.SNSApp.events.events.CommunityInvitationAcceptedEvent;
import ru.pocgg.SNSApp.events.events.CommunityInvitationDeclinedEvent;
import ru.pocgg.SNSApp.events.events.CommunityInvitationCreatedEvent;
import ru.pocgg.SNSApp.services.CommunityMemberService;
import ru.pocgg.SNSApp.services.NotificationService;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class CommunityInvitationsEventHandler {
    private final CommunityMemberService communityMemberService;
    private final NotificationService notificationService;


    @EventListener
    private void onCommunityInvitationCreated(CommunityInvitationCreatedEvent event) {
        notificationService.createNotification(event.getId().getReceiverId(),
                invitationCreatedMessage(event.getId().getCommunityId(), event.getId().getSenderId()), Instant.now());
    }

    public String invitationCreatedMessage(int communityId, int senderId){
        return "You have received invitation in community with id: " + communityId + " from user with id: " + senderId;
    }

    @EventListener
    private void onCommunityInvitationDeclined(CommunityInvitationDeclinedEvent event) {
        notificationService.createNotification(event.getId().getSenderId(),
                invitationDeclinedMessage(event.getId().getCommunityId(),
                        event.getId().getReceiverId()), Instant.now());
    }

    public String invitationDeclinedMessage(int communityId, int receiverId){
        return "Your invitation in community with id: " + communityId + " for user with id: " + receiverId
                + " has been declined";
    }

    @EventListener
    private void onCommunityInvitationAccepted(CommunityInvitationAcceptedEvent event) {
        communityMemberService.createMember(event.getId().getCommunityId(),
                event.getId().getReceiverId(), Instant.now());
        notificationService.createNotification(event.getId().getSenderId(),
                invitationAcceptedMessage(event.getId().getCommunityId(),
                        event.getId().getReceiverId()), Instant.now());
    }

    public String invitationAcceptedMessage(int communityId, int receiverId){
        return "Your invitation in community with id: " + communityId + " for user with id: " + receiverId
                + " has been accepted";
    }
}
