package ru.pocgg.SNSApp.services;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import ru.pocgg.SNSApp.model.ChatMessage;
import ru.pocgg.SNSApp.DTO.create.CreateChatMessageDTO;
import ru.pocgg.SNSApp.DTO.update.UpdateChatMessageDTO;
import ru.pocgg.SNSApp.DTO.mappers.update.UpdateChatMessageMapper;
import ru.pocgg.SNSApp.model.exceptions.EntityNotFoundException;
import ru.pocgg.SNSApp.services.DAO.interfaces.ChatMessageServiceDAO;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatMessageService extends TemplateService{

    private final ChatMessageServiceDAO chatMessageServiceDAO;
    private final ChatService chatService;
    private final UserService userService;
    private final UpdateChatMessageMapper updateChatMessageMapper;

    public ChatMessage createChatMessage(int chatId,
                                  int senderId,
                                  CreateChatMessageDTO dto) {
        ChatMessage chatMessage = ChatMessage.builder()
                .chat(chatService.getChatById(chatId))
                .sender(userService.getUserById(senderId))
                .sendingDate(Instant.now())
                .updateDate(null)
                .text(dto.getText()).build();
        chatMessageServiceDAO.addChatMessage(chatMessage);
        chatMessageServiceDAO.forceFlush();
        logger.info("created chat message with id: {} in chat with id: {} by sender with id: {}"
                , chatMessage.getId(), chatId, senderId);
        return chatMessage;
    }

    @Transactional(readOnly = true)
    public List<ChatMessage> getMessagesByChatId(int chatId) {
        return chatMessageServiceDAO.getMessagesByChatId(chatId);
    }

    @Transactional(readOnly = true)
    public List<ChatMessage> getMessagesByChatIdAndSenderId(int chatId, int senderId) {
        return chatMessageServiceDAO.getMessagesByChatIdAndSenderId(chatId, senderId);
    }

    @Transactional(readOnly = true)
    public ChatMessage getChatMessageById(int messageId) {
        return getChatMessageByIdOrThrowException(messageId);
    }

    public void setDeleted(int messageId, boolean value) {
        ChatMessage message = getChatMessageById(messageId);
        message.setDeleted(value);
        logger.info("message with id: {} now has property deleted set to: {}", messageId, value);
    }

    public void updateChatMessage(int messageId, UpdateChatMessageDTO dto) {
        ChatMessage chatMessage = getChatMessageById(messageId);
        updateChatMessageMapper.updateFromDTO(dto, chatMessage);
        updateDate(chatMessage);
        logger.info("message with id: {} has been updated", messageId);
    }

    private ChatMessage getChatMessageByIdOrThrowException(int messageId) {
        ChatMessage message = chatMessageServiceDAO.getChatMessageById(messageId);
        if (message == null) {
            throw new EntityNotFoundException("Message with id " + messageId + " not found");
        }
        return message;
    }

    private void updateDate(ChatMessage chatMessage) {
        chatMessage.setUpdateDate(Instant.now());
    }

}
