package ru.pocgg.SNSApp.services;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import ru.pocgg.SNSApp.DTO.create.CreateChatDTO;
import ru.pocgg.SNSApp.DTO.mappers.update.UpdateChatMapper;
import ru.pocgg.SNSApp.model.Chat;
import ru.pocgg.SNSApp.DTO.update.UpdateChatDTO;
import ru.pocgg.SNSApp.events.events.ChatBecamePublicEvent;
import ru.pocgg.SNSApp.events.events.ChatCreatedEvent;
import ru.pocgg.SNSApp.events.events.ChatDeactivatedEvent;
import ru.pocgg.SNSApp.model.exceptions.EntityNotFoundException;
import ru.pocgg.SNSApp.services.DAO.interfaces.ChatMemberServiceDAO;
import ru.pocgg.SNSApp.services.DAO.interfaces.ChatServiceDAO;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatService extends TemplateService {
    private final ChatServiceDAO chatServiceDAO;
    private final ChatMemberServiceDAO chatMemberServiceDAO;
    private final UserService userService;
    private final UpdateChatMapper updateChatMapper;
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.events.exchange}")
    private String exchangeName;

    public Chat createChat(int ownerId, CreateChatDTO dto) {
        Chat chat = Chat.builder()
                .owner(userService.getUserById(ownerId))
                .name(dto.getName())
                .description(dto.getDescription())
                .creationDate(Instant.now())
                .isPrivate(dto.getIsPrivate()).build();
        chatServiceDAO.addChat(chat);
        chatServiceDAO.forceFlush();
        ChatCreatedEvent event = ChatCreatedEvent.builder()
                .chatId(chat.getId())
                .ownerId(ownerId)
                .build();
        rabbitTemplate.convertAndSend(exchangeName, "chat.created", event);
        logger.info("created chat with id: {}", chat.getId());
        return chat;
    }

    public void updateChat(int chatId, UpdateChatDTO dto) {
        Chat chat = getChatByIdOrThrowException(chatId);
        updateChatMapper.updateFromDTO(dto, chat);
    }

    @Transactional(readOnly = true)
    public List<Chat> getAllChats() {
        return chatServiceDAO.getAllChats();
    }

    @Transactional(readOnly = true)
    public Chat getChatById(int id) {
        return getChatByIdOrThrowException(id);
    }

    @Transactional(readOnly = true)
    public List<Chat> getChatsByMemberId(int memberId) {
        return chatMemberServiceDAO.getChatsByMemberId(memberId);
    }

    public void setDeleted(int id, boolean value) {
        Chat chat = getChatByIdOrThrowException(id);
        if (chat.isDeleted() == value) {
            logger.info("chat with id: {} already has property deleted set to: {}", id, value);
        } else {
            chat.setDeleted(value);
            if (value) {
                ChatDeactivatedEvent event = ChatDeactivatedEvent.builder()
                        .chatId(id)
                        .build();
                rabbitTemplate.convertAndSend(exchangeName, "chat.deactivated", event);
            }
            logger.info("chat with id: {} now has property deleted set to: {}", id, value);
        }
    }

    private Chat getChatByIdOrThrowException(int id) {
        Chat chat = chatServiceDAO.getChatById(id);
        if (chat == null) {
            throw new EntityNotFoundException("chat with id: " + id + " not found");
        }
        return chat;
    }
}
