package ru.pocgg.SNSApp.services.DAO.interfaces;

import ru.pocgg.SNSApp.model.FriendshipRequest;
import ru.pocgg.SNSApp.model.FriendshipRequestId;

import java.util.List;

public interface FriendshipRequestServiceDAO {
    FriendshipRequest getRequestById(FriendshipRequestId id);
    List<FriendshipRequest> getRequestsBySenderId(int senderId);
    List<FriendshipRequest> getRequestsByReceiverId(int receiverId);
    List<FriendshipRequest> getAllRequests();
    void addRequest(FriendshipRequest request);
    void removeRequest(FriendshipRequest request);
    void removeBySenderId(int senderId);
    void removeByReceiverId(int receiverId);
    void forceFlush();
}
