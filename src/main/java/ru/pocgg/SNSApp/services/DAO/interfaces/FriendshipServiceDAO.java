package ru.pocgg.SNSApp.services.DAO.interfaces;

import ru.pocgg.SNSApp.model.Friendship;
import ru.pocgg.SNSApp.model.FriendshipId;
import org.hibernate.Session;

import java.util.List;

public interface FriendshipServiceDAO {
    Friendship getFriendshipByEmbeddedId(FriendshipId friendshipId);
    List<Friendship> getFriendshipsByUserId(int userId);
    List<Friendship> getFriendshipsByFriendId(int friendId);
    void addFriendship(Friendship friendship);
    void updateFriendship(Friendship friendship);
    void removeFriendship(Friendship friendship);
    List<Friendship> getAllFriendships();
    void forceFlush();
}
