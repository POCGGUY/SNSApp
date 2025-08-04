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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import ru.pocgg.SNSApp.events.events.*;
import ru.pocgg.SNSApp.services.FriendshipService;
import ru.pocgg.SNSApp.services.NotificationService;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class FriendshipRequestEventHandler {
    private static final Logger logger = LoggerFactory.getLogger(FriendshipRequestEventHandler.class);

    private final FriendshipService friendshipService;
    private final NotificationService notificationService;
    private final ObjectMapper mapper;

    @RabbitListener(bindings = @QueueBinding(
            exchange = @Exchange(
                    name = "app.events.exchange",
                    type = ExchangeTypes.TOPIC,
                    durable = "true"
            ),
            value = @Queue(
                    name = "friendship.request",
                    durable = "true"
            ),
            key = {
                    "friendship.request.created",
                    "friendship.request.declined",
                    "friendship.request.accepted"
            }

    ))
    public void handle(Message message,
                       @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKeys){
        String json = new String(message.getBody(), StandardCharsets.UTF_8);
        try {
            switch (routingKeys) {
                case "friendship.request.accepted":
                    onFriendshipRequestAccepted(mapper.readValue(json, FriendshipRequestAcceptedEvent.class));
                    break;
                case "friendship.request.created":
                    onFriendshipRequestCreated(mapper.readValue(json, FriendshipRequestCreatedEvent.class));
                    break;
                case "friendship.request.declined":
                    onFriendshipRequestDeclined(mapper.readValue(json, FriendshipRequestDeclinedEvent.class));
                    break;
                default:
            }
        } catch (JsonProcessingException e) {
            logger.error("Failed to deserialize message for routing key='{}': {}", routingKeys, e.getMessage());
        } catch (Exception e) {
            logger.error("Error handling event routing key='{}': {}", routingKeys, e.getMessage(), e);
        }

    }

    private void onFriendshipRequestDeclined(FriendshipRequestDeclinedEvent event) {
        notificationService.createNotification(event.getId().getSenderId(),
                requestDeclinedMessage(event.getId().getReceiverId()), Instant.now());
    }

    private String requestDeclinedMessage(int receiverId){
        return "Your friend request to user with id: " + receiverId + " has been declined";
    }

    private void onFriendshipRequestAccepted(FriendshipRequestAcceptedEvent event) {
        friendshipService.createFriendship(event.getId().getSenderId(), event.getId().getReceiverId(), Instant.now());
        notificationService.createNotification(event.getId().getSenderId(),
                requestAcceptedMessage(event.getId().getReceiverId()), Instant.now());
    }

    private String requestAcceptedMessage(int receiverId){
        return "Your friend request to user with id: " + receiverId + " has been accepted, you are friends now!";
    }

    private void onFriendshipRequestCreated(FriendshipRequestCreatedEvent event) {
        notificationService.createNotification(event.getId().getReceiverId(),
                requestCreatedMessage(event.getId().getSenderId()), Instant.now());
    }

    private String requestCreatedMessage(int senderId){
        return "You have received a friend request from user with id: " + senderId;
    }
}
