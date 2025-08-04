package ru.pocgg.SNSApp.events.eventHandlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import ru.pocgg.SNSApp.events.events.*;
import ru.pocgg.SNSApp.model.CommunityRole;
import ru.pocgg.SNSApp.services.CommunityInvitationService;
import ru.pocgg.SNSApp.services.CommunityMemberService;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class CommunityEventHandler {
    private static final Logger logger = LoggerFactory.getLogger(CommunityEventHandler.class);

    private final CommunityMemberService communityMemberService;
    private final CommunityInvitationService communityInvitationService;
    private final ObjectMapper mapper;

    @RabbitListener(bindings = @QueueBinding(
            exchange = @Exchange(
                    name = "app.events.exchange",
                    type = ExchangeTypes.TOPIC,
                    durable = "true"
            ),
            value = @Queue(
                    name = "community.queue",
                    durable = "true"
            ),
            key = {
                    "community.created",
                    "community.became.public"
            }

    ))
    public void handle(Message message,
                       @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKeys){
        String json = new String(message.getBody(), StandardCharsets.UTF_8);
        try {
            switch (routingKeys) {
                case "community.created":
                    onCommunityCreation(mapper.readValue(json, CommunityCreatedEvent.class));
                    break;
                case "community.became.public":
                    onCommunityBecamePublic(mapper.readValue(json, CommunityBecamePublicEvent.class));
                    break;
                default:
            }
        } catch (JsonProcessingException e) {
            logger.error("Failed to deserialize message for routing key='{}': {}", routingKeys, e.getMessage());
        } catch (Exception e) {
            logger.error("Error handling event routing key='{}': {}", routingKeys, e.getMessage(), e);
        }

    }

    private void onCommunityCreation(CommunityCreatedEvent event) {
        communityMemberService
                .createMember(event.getCommunityId(),
                        event.getOwnerId(),
                        Instant.now(),
                        CommunityRole.OWNER);
    }

    private void onCommunityBecamePublic(CommunityBecamePublicEvent event) {
        communityInvitationService.removeByCommunityId(event.getCommunityId());
        logger.info("all community invitations affiliated with community with id: {} has been removed",
                event.getCommunityId());
    }
}
