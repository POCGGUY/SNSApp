package ru.pocgg.SNSApp.services;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import ru.pocgg.SNSApp.model.ChatInvitation;
import ru.pocgg.SNSApp.model.ChatInvitationId;
import ru.pocgg.SNSApp.DTO.create.CreateChatInvitationDTO;
import ru.pocgg.SNSApp.events.events.ChatInvitationAcceptedEvent;
import ru.pocgg.SNSApp.events.events.ChatInvitationDeclinedEvent;
import ru.pocgg.SNSApp.events.events.ChatInvitationCreatedEvent;
import ru.pocgg.SNSApp.model.exceptions.EntityNotFoundException;
import ru.pocgg.SNSApp.services.DAO.interfaces.ChatInvitationServiceDAO;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatInvitationService extends TemplateService{
    private final ChatInvitationServiceDAO chatInvitationServiceDAO;
    private final UserService userService;
    private final ChatService chatService;
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.events.exchange}")
    private String exchangeName;

    public ChatInvitation createChatInvitation(int senderId,
                                     int receiverId,
                                     int chatId,
                                     CreateChatInvitationDTO dto) {
        ChatInvitation chatInvitation = ChatInvitation.builder()
                .sender(userService.getUserById(senderId))
                .receiver(userService.getUserById(receiverId))
                .chat(chatService.getChatById(chatId))
                .creationDate(Instant.now())
                .description(dto.getDescription()).build();
        chatInvitationServiceDAO.addChatInvitation(chatInvitation);
        chatInvitationServiceDAO.forceFlush();
        ChatInvitationCreatedEvent event = ChatInvitationCreatedEvent.builder()
                .id(chatInvitation.getId())
                .build();
        rabbitTemplate.convertAndSend(exchangeName, "chat.invitation.created", event);
        logger.info("created chat invitation from user with id: {} to user with id: {} in chat with id: {}"
                , senderId, receiverId, chatId);
        return chatInvitation;
    }

    @Transactional(readOnly = true)
    public ChatInvitation getById(ChatInvitationId id) {
        return getChatInvitationByIdOrThrowException(id);
    }

    @Transactional(readOnly = true)
    public List<ChatInvitation> getByReceiver(int receiverId) {
        return chatInvitationServiceDAO.getByReceiverId(receiverId);
    }

    @Transactional(readOnly = true)
    public List<ChatInvitation> getBySender(int senderId) {
        return chatInvitationServiceDAO.getBySenderId(senderId);
    }

    @Transactional(readOnly = true)
    public List<ChatInvitation> getByChat(int chatId) {
        return chatInvitationServiceDAO.getByChatId(chatId);
    }

    @Transactional(readOnly = true)
    public List<ChatInvitation> getAllChatInvitations() {
        return chatInvitationServiceDAO.getAllChatInvitations();
    }

    @Transactional(readOnly = true)
    public boolean isInvitationExist(ChatInvitationId id) {
        return chatInvitationServiceDAO.getChatInvitationById(id) != null;
    }

    public void removeInvitation(ChatInvitationId id) {
        chatInvitationServiceDAO.removeChatInvitation(getChatInvitationByIdOrThrowException(id));
    }

    public void acceptInvitation(ChatInvitationId id) {
        ChatInvitation chatInvitation = getChatInvitationByIdOrThrowException(id);
        ChatInvitationAcceptedEvent event = ChatInvitationAcceptedEvent.builder()
                .id(chatInvitation.getId())
                .build();
        rabbitTemplate.convertAndSend(exchangeName, "chat.invitation.accepted", event);
        chatInvitationServiceDAO.removeChatInvitation(chatInvitation);
    }

    public void declineInvitation(ChatInvitationId id) {
        ChatInvitation chatInvitation = getChatInvitationByIdOrThrowException(id);
        ChatInvitationDeclinedEvent event = ChatInvitationDeclinedEvent.builder()
                .id(chatInvitation.getId())
                .build();
        rabbitTemplate.convertAndSend(exchangeName, "chat.invitation.declined", event);
        chatInvitationServiceDAO.removeChatInvitation(chatInvitation);
    }

    public void removeBySenderId(int senderId) {
        chatInvitationServiceDAO.removeBySenderId(senderId);
        logger.info("all chat invitations sent by sender with id: {} has been removed", senderId);
    }

    public void removeByReceiverId(int receiverId) {
        chatInvitationServiceDAO.removeByReceiverId(receiverId);
        logger.info("all chat invitations sent to receiver with id: {} has been removed", receiverId);
    }

    public void removeByChatId(int chatId) {
        chatInvitationServiceDAO.removeByChatId(chatId);
        logger.info("all chat invitations in chat with id: {} has been removed", chatId);
    }

    private ChatInvitation getChatInvitationByIdOrThrowException(ChatInvitationId id) {
        ChatInvitation chatInvitation = chatInvitationServiceDAO.getChatInvitationById(id);
        if (chatInvitation != null) {
            return chatInvitation;
        } else {
            throw new EntityNotFoundException("Chat invitation with receiver id: " + id.getReceiverId()
                    + " where sender id: " + id.getSenderId() + " and where chat id: " + id.getChatId() + " not found");
        }
    }

}
