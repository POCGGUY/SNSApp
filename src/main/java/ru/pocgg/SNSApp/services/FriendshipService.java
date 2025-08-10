package ru.pocgg.SNSApp.services;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import ru.pocgg.SNSApp.model.Friendship;
import ru.pocgg.SNSApp.model.FriendshipId;
import ru.pocgg.SNSApp.model.exceptions.EntityNotFoundException;
import ru.pocgg.SNSApp.services.DAO.interfaces.FriendshipServiceDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class FriendshipService extends TemplateService{
    private final FriendshipServiceDAO friendshipServiceDAO;
    private final UserService userService;

    public Friendship createFriendship(int userId,
                                 int friendId,
                                 Instant creationDate) {
        Friendship friendship = Friendship.builder()
                .user(userService.getUserById(userId))
                .friend(userService.getUserById(friendId))
                .creationDate(creationDate).build();
        friendshipServiceDAO.addFriendship(friendship);
        friendshipServiceDAO.forceFlush();
        logger.info("created friendship between user with id: {} and user with id: {}", userId, friendId);
        return friendship;
    }

    @Transactional(readOnly = true)
    public List<Friendship> getUserFriendships(int userId) {
        List<Friendship> friendships = new ArrayList<>();
        friendships.addAll(friendshipServiceDAO.getFriendshipsByUserId(userId));
        friendships.addAll(friendshipServiceDAO.getFriendshipsByFriendId(userId));
        return friendships;
    }

    public void removeFriendship(int userId, int friendId) {
        Friendship friendship = getFriendshipByEmbeddedId(userId, friendId);
        friendshipServiceDAO.removeFriendship(friendship);
        logger.info("friendship between user with id: {} and user with id: {} has been removed", userId, friendId);
    }

    @Transactional(readOnly = true)
    public Boolean isFriendshipExist(int userId, int friendId) {
        FriendshipId directId = new FriendshipId(userId, friendId);
        FriendshipId reverseId = new FriendshipId(friendId, userId);
        Friendship friendship = friendshipServiceDAO.getFriendshipByEmbeddedId(directId);
        if (friendship == null) {
            friendship = friendshipServiceDAO.getFriendshipByEmbeddedId(reverseId);
        }
        return friendship != null;
    }

    @Transactional(readOnly = true)
    public Friendship getFriendshipByEmbeddedId(int userId, int friendId) {
        FriendshipId directId = new FriendshipId(userId, friendId);
        FriendshipId reverseId = new FriendshipId(friendId, userId);
        Friendship friendship = friendshipServiceDAO.getFriendshipByEmbeddedId(directId);
        if (friendship == null) {
            friendship = friendshipServiceDAO.getFriendshipByEmbeddedId(reverseId);
        }
        if (friendship == null) {
            throw new EntityNotFoundException("Friendship between users with id: "
                    + userId + " and: " + friendId + " not found");
        }
        return friendship;
    }
}
