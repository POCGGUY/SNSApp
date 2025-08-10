package ru.pocgg.SNSApp.events.eventHandlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
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
import ru.pocgg.SNSApp.services.CommunityMemberService;
import ru.pocgg.SNSApp.services.NotificationService;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class CommunityInvitationsEventHandler {
    private static final Logger logger = LoggerFactory.getLogger(CommunityInvitationsEventHandler.class);

    private final CommunityMemberService communityMemberService;
    private final NotificationService notificationService;
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
                    name = "community.invitation.queue",
                    durable = "true"
            ),
            key = {
                    "community.invitation.created",
                    "community.invitation.declined",
                    "community.invitation.accepted"
            }

    ))
    public void handle(Message message,
                       @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKeys){
        String json = new String(message.getBody(), StandardCharsets.UTF_8);
        try {
            switch (routingKeys) {
                case "community.invitation.accepted":
                    onCommunityInvitationAccepted(mapper.readValue(json, CommunityInvitationAcceptedEvent.class));
                    break;
                case "community.invitation.created":
                    onCommunityInvitationCreated(mapper.readValue(json, CommunityInvitationCreatedEvent.class));
                    break;
                case "community.invitation.declined":
                    onCommunityInvitationDeclined(mapper.readValue(json, CommunityInvitationDeclinedEvent.class));
                    break;
                default:
            }
        } catch (JsonProcessingException e) {
            logger.error("Failed to deserialize message for routing key='{}': {}", routingKeys, e.getMessage());
        } catch (Exception e) {
            logger.error("Error handling event routing key='{}': {}", routingKeys, e.getMessage(), e);
        }

    }


    private void onCommunityInvitationCreated(CommunityInvitationCreatedEvent event) {
        notificationService.createNotification(event.getId().getReceiverId(),
                invitationCreatedMessage(event.getId().getCommunityId(), event.getId().getSenderId()), Instant.now());
    }

    private String invitationCreatedMessage(int communityId, int senderId){
        return "You have received invitation in community with id: " + communityId + " from user with id: " + senderId;
    }

    private void onCommunityInvitationDeclined(CommunityInvitationDeclinedEvent event) {
        notificationService.createNotification(event.getId().getSenderId(),
                invitationDeclinedMessage(event.getId().getCommunityId(),
                        event.getId().getReceiverId()), Instant.now());
    }

    private String invitationDeclinedMessage(int communityId, int receiverId){
        return "Your invitation in community with id: " + communityId + " for user with id: " + receiverId
                + " has been declined";
    }

    private void onCommunityInvitationAccepted(CommunityInvitationAcceptedEvent event) {
        communityMemberService.createMember(event.getId().getCommunityId(),
                event.getId().getReceiverId(), Instant.now());
        notificationService.createNotification(event.getId().getSenderId(),
                invitationAcceptedMessage(event.getId().getCommunityId(),
                        event.getId().getReceiverId()), Instant.now());
    }

    private String invitationAcceptedMessage(int communityId, int receiverId){
        return "Your invitation in community with id: " + communityId + " for user with id: " + receiverId
                + " has been accepted";
    }
}
