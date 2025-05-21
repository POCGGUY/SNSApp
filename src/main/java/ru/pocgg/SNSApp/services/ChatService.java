package ru.pocgg.SNSApp.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import ru.pocgg.SNSApp.DTO.create.CreateChatDTO;
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
    private final ApplicationEventPublisher eventPublisher;

    public Chat createChat(int ownerId, CreateChatDTO dto) {
        Chat chat = Chat.builder()
                .owner(userService.getUserById(ownerId))
                .name(dto.getName())
                .description(dto.getDescription())
                .creationDate(Instant.now())
                .isPrivate(dto.getIsPrivate()).build();
        chatServiceDAO.addChat(chat);
        chatServiceDAO.forceFlush();
        eventPublisher.publishEvent(new ChatCreatedEvent(chat.getId(), ownerId));
        logger.info("created chat with id: {}", chat.getId());
        return chat;
    }

    public void updateChat(int chatId, UpdateChatDTO dto) {
        Chat chat = getChatByIdOrThrowException(chatId);
        updateName(chat, dto.getName());
        updateDescription(chat, dto.getDescription());
        updatePrivate(chat, dto.getIsPrivate());
    }

    public List<Chat> getAllChats() {
        return chatServiceDAO.getAllChats();
    }

    public Chat getChatById(int id) {
        return getChatByIdOrThrowException(id);
    }

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
                eventPublisher.publishEvent(new ChatDeactivatedEvent(id));
            }
            logger.info("chat with id: {} now has property deleted set to: {}", id, value);
        }
    }

    private void updateName(Chat chat, String name) {
        if (name != null) {
            chat.setName(name);
            logger.info("chat with id: {} has updated name to: {}", chat.getId(), name);
        }
    }

    private void updateDescription(Chat chat, String description) {
        if (description != null) {
            chat.setDescription(description);
            logger.info("chat with id: {} has updated description to: {}", chat.getId(), description);
        }
    }

    private void updatePrivate(Chat chat, Boolean value) {
        if (value != null) {
            chat.setPrivate(value);
            if (!value) {
                eventPublisher.publishEvent(ChatBecamePublicEvent.builder()
                        .chatId(chat.getId())
                        .build());
            }
            logger.info("chat with id: {} has updated private to: {}", chat.getId(), value);
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
