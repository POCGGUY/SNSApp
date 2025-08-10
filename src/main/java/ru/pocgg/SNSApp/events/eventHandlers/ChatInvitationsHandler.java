package ru.pocgg.SNSApp.events.eventHandlers;

import org.slf4j.Logger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
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
import ru.pocgg.SNSApp.events.events.ChatInvitationAcceptedEvent;
import ru.pocgg.SNSApp.events.events.ChatInvitationCreatedEvent;
import ru.pocgg.SNSApp.events.events.ChatInvitationDeclinedEvent;
import ru.pocgg.SNSApp.services.ChatMemberService;
import ru.pocgg.SNSApp.services.NotificationService;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class ChatInvitationsHandler {
    private static final Logger logger = LoggerFactory.getLogger(ChatInvitationsHandler.class);

    private final ChatMemberService chatMemberService;
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
                    name = "chat.invitation.queue",
                    durable = "true"
            ),
            key = {
                    "chat.invitation.created",
                    "chat.invitation.declined",
                    "chat.invitation.accepted"
            }

    ))
    public void handle(Message message,
                       @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKeys) {
        String json = new String(message.getBody(), StandardCharsets.UTF_8);
        try {
            switch (routingKeys) {
                case "chat.invitation.accepted":
                    onChatInvitationAccepted(mapper.readValue(json, ChatInvitationAcceptedEvent.class));
                    break;
                case "chat.invitation.created":
                    onChatInvitationCreated(mapper.readValue(json, ChatInvitationCreatedEvent.class));
                    break;
                case "chat.invitation.declined":
                    onChatInvitationDeclined(mapper.readValue(json, ChatInvitationDeclinedEvent.class));
                    break;
                default:
            }
        } catch (JsonProcessingException e) {
            logger.error("Failed to deserialize message for routing key='{}': {}", routingKeys, e.getMessage());
        } catch (Exception e) {
            logger.error("Error handling event routing key='{}': {}", routingKeys, e.getMessage(), e);
        }

    }


    private void onChatInvitationDeclined(ChatInvitationDeclinedEvent event) {
        notificationService.createNotification(event.getId().getSenderId(),
                invitationDeclinedMessage(event.getId().getChatId(), event.getId().getReceiverId()), Instant.now());
    }

    private void onChatInvitationAccepted(ChatInvitationAcceptedEvent event) {
        chatMemberService.createChatMember(event.getId().getChatId(), event.getId().getReceiverId(), Instant.now());
        notificationService.createNotification(event.getId().getSenderId(),
                invitationAcceptedMessage(event.getId().getChatId(), event.getId().getReceiverId()), Instant.now());
    }

    private void onChatInvitationCreated(ChatInvitationCreatedEvent event) {
        notificationService.createNotification(event.getId().getReceiverId(),
                invitationCreatedMessage(event.getId().getChatId(), event.getId().getReceiverId()), Instant.now());
    }

    private String invitationCreatedMessage(int chatId, int senderId) {
        return "You have received invitation in chat with id: " + chatId + " from user with id: " + senderId;
    }

    private String invitationAcceptedMessage(int chatId, int receiverId) {
        return "Your invitation in chat with id: " + chatId + " for user with id: " + receiverId
                + " has been accepted";
    }

    private String invitationDeclinedMessage(int chatId, int receiverId) {
        return "Your invitation in chat with id: " + chatId + " for user with id: " + receiverId
                + " has been declined";
    }
}
