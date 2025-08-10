package ru.pocgg.SNSApp.events.eventHandlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import ru.pocgg.SNSApp.events.events.ChatBecamePublicEvent;
import ru.pocgg.SNSApp.events.events.ChatCreatedEvent;
import ru.pocgg.SNSApp.services.ChatInvitationService;
import ru.pocgg.SNSApp.services.ChatMemberService;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class ChatEventHandler {
    private final Logger logger = LogManager.getLogger(ChatEventHandler.class);

    private final ObjectMapper mapper;
    private final ChatMemberService chatMemberService;
    private final ChatInvitationService chatInvitationService;

    @RabbitListener(
            containerFactory = "rabbitListenerContainerFactory",
            bindings = @QueueBinding(
            exchange = @Exchange(
                    name = "app.events.exchange",
                    type = ExchangeTypes.TOPIC,
                    durable = "true"
            ),
            value = @Queue(
                    name = "chat.queue",
                    durable = "true"
            ),
            key = {
                    "chat.created",
                    "chat.became.public"
            }
    ))
    public void handle(Message message,
                       @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKeys){
        String json = new String(message.getBody(), StandardCharsets.UTF_8);
        try {
            switch (routingKeys) {
                case "chat.created":
                    onChatCreation(mapper.readValue(json, ChatCreatedEvent.class));
                    break;
                case "chat.became.public":
                    onChatBecamePublic(mapper.readValue(json, ChatBecamePublicEvent.class));
                    break;
                default:
            }
        } catch (JsonProcessingException e) {
            logger.error("Failed to deserialize message for routing key='{}': {}", routingKeys, e.getMessage());
        } catch (Exception e) {
            logger.error("Error handling event routing key='{}': {}", routingKeys, e.getMessage(), e);
        }
    }

    private void onChatCreation(ChatCreatedEvent event) {
        chatMemberService.createChatMember(event.getChatId(), event.getOwnerId(), Instant.now());
    }

    private void onChatBecamePublic(ChatBecamePublicEvent event) {
        chatInvitationService.removeByChatId(event.getChatId());
        logger.info("all chat invitations affiliated with chat with id: {} has been removed",
                event.getChatId());
    }
}
