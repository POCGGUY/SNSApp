package ru.pocgg.SNSApp.events.eventHandlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import ru.pocgg.SNSApp.events.events.ChatDeactivatedEvent;
import ru.pocgg.SNSApp.events.events.CommunityDeactivatedEvent;
import ru.pocgg.SNSApp.events.events.UserDeactivatedEvent;
import ru.pocgg.SNSApp.services.ChatInvitationService;
import ru.pocgg.SNSApp.services.CommunityInvitationService;
import ru.pocgg.SNSApp.services.FriendshipRequestService;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class EntityDeactivatedHandler {
    private static final Logger logger = LoggerFactory.getLogger(ChatInvitationsHandler.class);

    private final FriendshipRequestService friendshipRequestService;
    private final ChatInvitationService chatInvitationService;
    private final CommunityInvitationService communityInvitationService;
    private final ObjectMapper mapper;

    @RabbitListener(
            containerFactory = "rabbitListenerContainerFactory",
            bindings = @QueueBinding(
            exchange = @Exchange(
                    name = "app.events.exchange",
                    type = ExchangeTypes.TOPIC,
                    durable = "true"
            ),
            value = @Queue(
                    name = "entity.deactivated.queue",
                    durable = "true"
            ),
            key = {"*.deactivated"}

    ))
    public void handle(Message message,
                       @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKeys){
        String json = new String(message.getBody(), StandardCharsets.UTF_8);
        try {
            switch (routingKeys) {
                case "user.deactivated":
                    onUserDeactivated(mapper.readValue(json, UserDeactivatedEvent.class));
                    break;
                case "community.deactivated":
                    onCommunityDeactivated(mapper.readValue(json, CommunityDeactivatedEvent.class));
                    break;
                case "chat.deactivated":
                    onChatDeactivated(mapper.readValue(json, ChatDeactivatedEvent.class));
                    break;
                default:
            }
        } catch (JsonProcessingException e) {
            logger.error("Failed to deserialize message for routing key='{}': {}", routingKeys, e.getMessage());
        } catch (Exception e) {
            logger.error("Error handling event routing key='{}': {}", routingKeys, e.getMessage(), e);
        }

    }

    private void onUserDeactivated(UserDeactivatedEvent event) {
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

    private void onCommunityDeactivated(CommunityDeactivatedEvent event) {
        communityInvitationService.removeByCommunityId(event.getCommunityId());
        logger.info("all community invitations affiliated with community with id: {} has been removed",
                event.getCommunityId());
    }

    private void onChatDeactivated(ChatDeactivatedEvent event) {
        chatInvitationService.removeByChatId(event.getChatId());
        logger.info("all chat invitations affiliated with chat with id: {} has been removed",
                event.getChatId());
    }
}
