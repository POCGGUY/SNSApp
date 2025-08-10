package ru.pocgg.SNSApp.services;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import ru.pocgg.SNSApp.DTO.create.CreatePrivateMessageDTO;
import ru.pocgg.SNSApp.DTO.mappers.update.UpdatePrivateMessageMapper;
import ru.pocgg.SNSApp.DTO.update.UpdatePrivateMessageDTO;
import ru.pocgg.SNSApp.model.PrivateMessage;
import ru.pocgg.SNSApp.model.User;
import ru.pocgg.SNSApp.model.exceptions.EntityNotFoundException;
import ru.pocgg.SNSApp.services.DAO.interfaces.PrivateMessageDAO;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PrivateMessageService extends TemplateService{
    private final PrivateMessageDAO privateMessageDAO;
    private final UserService userService;
    private final UpdatePrivateMessageMapper updatePrivateMessageMapper;

    public PrivateMessage createMessage(int senderId, int receiverId, CreatePrivateMessageDTO dto) {
        User sender = userService.getUserById(senderId);
        User receiver = userService.getUserById(receiverId);
        PrivateMessage message = PrivateMessage.builder()
                .sender(sender)
                .receiver(receiver)
                .creationDate(Instant.now())
                .text(dto.getText())
                .updateDate(null)
                .deleted(false)
                .build();
        privateMessageDAO.add(message);
        privateMessageDAO.forceFlush();
        logger.info("created private message from user with id: {} to user with id: {}", senderId, receiverId);
        return message;
    }

    public void updateMessage(int messageId, UpdatePrivateMessageDTO dto) {
        PrivateMessage message = getMessageByIdOrThrowException(messageId);
        updatePrivateMessageMapper.updateFromDTO(dto, message);
        updateTime(message);
    }

    @Transactional(readOnly = true)
    public PrivateMessage getById(int messageId) {
        return getMessageByIdOrThrowException(messageId);
    }

    @Transactional(readOnly = true)
    public List<PrivateMessage> getAllBySenderAndReceiver(int senderId, int receiverId) {
        return privateMessageDAO.getAllBySenderAndReceiver(senderId, receiverId);
    }

    public void setDeleted(int messageId, boolean value) {
        PrivateMessage message = getMessageByIdOrThrowException(messageId);
        message.setDeleted(value);
        message.setUpdateDate(Instant.now());
        logger.info("message with id: {} has been deleted", messageId);
    }

    private PrivateMessage getMessageByIdOrThrowException(int messageId) {
        PrivateMessage message = privateMessageDAO.getById(messageId);
        if (message == null) {
            throw new EntityNotFoundException("private message with id: " + messageId + " not found");
        }
        return message;
    }

    private void updateTime(PrivateMessage message) {
        message.setUpdateDate(Instant.now());
    }

}
