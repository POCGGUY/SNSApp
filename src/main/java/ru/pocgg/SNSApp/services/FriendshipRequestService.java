package ru.pocgg.SNSApp.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import ru.pocgg.SNSApp.model.FriendshipRequest;
import ru.pocgg.SNSApp.model.FriendshipRequestId;
import ru.pocgg.SNSApp.events.events.FriendshipRequestAcceptedEvent;
import ru.pocgg.SNSApp.events.events.FriendshipRequestDeclinedEvent;
import ru.pocgg.SNSApp.events.events.FriendshipRequestCreatedEvent;
import ru.pocgg.SNSApp.model.exceptions.EntityNotFoundException;
import ru.pocgg.SNSApp.services.DAO.interfaces.FriendshipRequestServiceDAO;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class FriendshipRequestService extends TemplateService {
    private final FriendshipRequestServiceDAO friendshipRequestServiceDAO;
    private final UserService userService;
    private final ApplicationEventPublisher eventPublisher;

    public FriendshipRequest createRequest(int senderId, int receiverId, Instant creationDate) {
        FriendshipRequest request = FriendshipRequest.builder()
                .sender(userService.getUserById(senderId))
                .receiver(userService.getUserById(receiverId))
                .creationDate(creationDate).build();
        friendshipRequestServiceDAO.addRequest(request);
        eventPublisher.publishEvent(FriendshipRequestCreatedEvent.builder()
                .id(request.getId())
                .build());
        logger.info("created friendship request from user with id: {} to user with id: {}",
                senderId, receiverId);
        return request;
    }

    public List<FriendshipRequest> getRequestsBySenderId(int senderId) {
        return friendshipRequestServiceDAO.getRequestsBySenderId(senderId);
    }

    public List<FriendshipRequest> getRequestsByReceiverId(int receiverId) {
        return friendshipRequestServiceDAO.getRequestsByReceiverId(receiverId);
    }

    public void removeBySenderId(int senderId) {
        friendshipRequestServiceDAO.removeBySenderId(senderId);
        logger.info("all friend requests by sender with id: {} has been removed", senderId);
    }

    public void removeByReceiverId(int receiverId) {
        friendshipRequestServiceDAO.removeByReceiverId(receiverId);
        logger.info("all friend requests to receiver with id: {} has been removed", receiverId);
    }

    public List<FriendshipRequest> getAllRequests() {
        return friendshipRequestServiceDAO.getAllRequests();
    }

    public FriendshipRequest getRequestById(FriendshipRequestId id) {
        return getRequestByIdOrThrowException(id);
    }

    public boolean isRequestExists(FriendshipRequestId id) {
        return friendshipRequestServiceDAO.getRequestById(FriendshipRequestId.builder()
                .senderId(id.getSenderId())
                .receiverId(id.getReceiverId()).build()) != null ||
                friendshipRequestServiceDAO.getRequestById(FriendshipRequestId.builder()
                        .senderId(id.getReceiverId())
                        .receiverId(id.getSenderId()).build()) != null;
    }

    public void acceptRequest(FriendshipRequestId id) {
        FriendshipRequest request = getRequestByIdOrThrowException(id);
        friendshipRequestServiceDAO.removeRequest(request);
        eventPublisher.publishEvent(FriendshipRequestAcceptedEvent.builder()
                .id(id)
                .build());
        logger.info("friendship request from user with id: {} to user with id: {} was accepted",
                id.getSenderId(), id.getReceiverId());
    }

    public void declineRequest(FriendshipRequestId id) {
        FriendshipRequest request = getRequestByIdOrThrowException(id);
        friendshipRequestServiceDAO.removeRequest(request);
        eventPublisher.publishEvent(FriendshipRequestDeclinedEvent.builder()
                .id(id)
                .build());
        logger.info("friendship request from user with id: {} to user with id: {} was declined",
                id.getSenderId(), id.getReceiverId());
    }

    public void deleteRequest(FriendshipRequestId id) {
        FriendshipRequest request = getRequestByIdOrThrowException(id);
        friendshipRequestServiceDAO.removeRequest(request);
        logger.info("friendship request from user with id: {} to user with id: {} was deleted",
                id.getSenderId(), id.getReceiverId());
    }

    private FriendshipRequest getRequestByIdOrThrowException(FriendshipRequestId id) {
        FriendshipRequest request = friendshipRequestServiceDAO.getRequestById(id);
        if (request == null) {
            throw new EntityNotFoundException("Friendship with receiver with id: "
                    + id.getReceiverId() + " and with sender with id: " + id.getSenderId() + " was not found");
        }
        return request;
    }
}
