package ru.pocgg.SNSApp.services.DAO.requests;

public class FriendshipServiceDAORequests {
    public static final String GET_BY_ID =
            "SELECT f FROM Friendship f " +
                    "JOIN FETCH f.user JOIN FETCH f.friend " +
                    "WHERE f.id = :id";
    public static final String GET_ALL =
            "SELECT f " +
                    "FROM Friendship f " +
                    "JOIN FETCH f.user " +
                    "JOIN FETCH f.friend";
    public static final String GET_BY_USER_ID =
            "SELECT f " +
                    "FROM Friendship f " +
                    "JOIN FETCH f.user " +
                    "JOIN FETCH f.friend " +
                    "WHERE f.user.id = :userId";
    public static final String GET_BY_FRIEND_ID =
            "SELECT f " +
                    "FROM Friendship f " +
                    "JOIN FETCH f.user " +
                    "JOIN FETCH f.friend " +
                    "WHERE f.friend.id = :friendId";
}
