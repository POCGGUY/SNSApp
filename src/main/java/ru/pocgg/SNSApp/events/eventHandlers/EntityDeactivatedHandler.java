package ru.pocgg.SNSApp.events.eventHandlers;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import ru.pocgg.SNSApp.events.events.ChatDeactivatedEvent;
import ru.pocgg.SNSApp.events.events.CommunityDeactivatedEvent;
import ru.pocgg.SNSApp.events.events.UserDeactivatedEvent;
import ru.pocgg.SNSApp.services.ChatInvitationService;
import ru.pocgg.SNSApp.services.CommunityInvitationService;
import ru.pocgg.SNSApp.services.FriendshipRequestService;

@Component
@RequiredArgsConstructor
public class EntityDeactivatedHandler {
    private final Logger logger = LogManager.getLogger(EntityDeactivatedHandler.class);

    private final FriendshipRequestService friendshipRequestService;
    private final ChatInvitationService chatInvitationService;
    private final CommunityInvitationService communityInvitationService;

    @EventListener
    public void onUserDeactivated(UserDeactivatedEvent event) {
        communityInvitationService.removeBySenderId(event.getUserId());
        communityInvitationService.removeByReceiverId(event.getUserId());
        chatInvitationService.removeBySenderId(event.getUserId());
        chatInvitationService.removeByReceiverId(event.getUserId());
        friendshipRequestService.removeBySenderId(event.getUserId());
        friendshipRequestService.removeByReceiverId(event.getUserId());
        logger.info("all chat and community invitations and friend requests affiliated " +
                        "with user with id: {} has been removed",
                event.getUserId());
    }

    @EventListener
    public void onCommunityDeactivated(CommunityDeactivatedEvent event) {
        communityInvitationService.removeByCommunityId(event.getCommunityId());
        logger.info("all community invitations affiliated with community with id: {} has been removed",
                event.getCommunityId());
    }

    @EventListener
    public void onChatDeactivated(ChatDeactivatedEvent event) {
        chatInvitationService.removeByChatId(event.getChatId());
        logger.info("all chat invitations affiliated with chat with id: {} has been removed",
                event.getChatId());
    }
}
