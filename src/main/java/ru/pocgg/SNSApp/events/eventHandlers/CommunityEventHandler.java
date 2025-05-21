package ru.pocgg.SNSApp.events.eventHandlers;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import ru.pocgg.SNSApp.model.CommunityRole;
import ru.pocgg.SNSApp.events.events.CommunityBecamePublicEvent;
import ru.pocgg.SNSApp.events.events.CommunityCreatedEvent;
import ru.pocgg.SNSApp.services.CommunityInvitationService;
import ru.pocgg.SNSApp.services.CommunityMemberService;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class CommunityEventHandler {
    private final Logger logger = LogManager.getLogger(CommunityEventHandler.class);

    private final CommunityMemberService communityMemberService;
    private final CommunityInvitationService communityInvitationService;

    @EventListener
    public void onCommunityCreation(CommunityCreatedEvent event) {
        communityMemberService
                .createMember(event.getCommunityId(),
                        event.getOwnerId(),
                        Instant.now(),
                        CommunityRole.OWNER);
    }

    @EventListener
    public void onCommunityBecamePublic(CommunityBecamePublicEvent event) {
        communityInvitationService.removeByCommunityId(event.getCommunityId());
        logger.info("all community invitations affiliated with community with id: {} has been removed",
                event.getCommunityId());
    }
}
