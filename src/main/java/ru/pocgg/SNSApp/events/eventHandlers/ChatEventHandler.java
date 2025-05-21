package ru.pocgg.SNSApp.events.eventHandlers;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import ru.pocgg.SNSApp.events.events.ChatBecamePublicEvent;
import ru.pocgg.SNSApp.events.events.ChatCreatedEvent;
import ru.pocgg.SNSApp.services.ChatInvitationService;
import ru.pocgg.SNSApp.services.ChatMemberService;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class ChatEventHandler {
    private final Logger logger = LogManager.getLogger(ChatEventHandler.class);

    private final ChatMemberService chatMemberService;
    private final ChatInvitationService chatInvitationService;

    @EventListener
    public void onChatCreation(ChatCreatedEvent event) {
        chatMemberService.createChatMember(event.getChatId(), event.getOwnerId(), Instant.now());
    }

    @EventListener
    public void onChatBecamePublic(ChatBecamePublicEvent event) {
        chatInvitationService.removeByChatId(event.getChatId());
        logger.info("all chat invitations affiliated with chat with id: {} has been removed",
                event.getChatId());
    }
}
