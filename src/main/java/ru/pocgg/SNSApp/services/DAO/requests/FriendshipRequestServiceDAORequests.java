package ru.pocgg.SNSApp.services.DAO.requests;

public class FriendshipRequestServiceDAORequests {
    public static final String GET_BY_ID =
            "SELECT fr FROM FriendshipRequest fr " +
                    "JOIN FETCH fr.sender JOIN FETCH fr.receiver " +
                    "WHERE fr.id = :id";
    public static final String GET_ALL =
            "SELECT fr " +
                    "FROM FriendshipRequest fr " +
                    "JOIN FETCH fr.sender " +
                    "JOIN FETCH fr.receiver";
    public static final String GET_BY_SENDER_ID =
            "SELECT fr " +
                    "FROM FriendshipRequest fr " +
                    "JOIN FETCH fr.sender " +
                    "JOIN FETCH fr.receiver " +
                    "WHERE fr.sender.id = :senderId";
    public static final String GET_BY_RECEIVER_ID =
            "SELECT fr " +
                    "FROM FriendshipRequest fr " +
                    "JOIN FETCH fr.sender " +
                    "JOIN FETCH fr.receiver " +
                    "WHERE fr.receiver.id = :receiverId";
    public static final String DELETE_BY_SENDER_ID =
            "DELETE FROM FriendshipRequest fr " +
                    "WHERE fr.sender.id = :senderId";
    public static final String DELETE_BY_RECEIVER_ID =
            "DELETE FROM FriendshipRequest fr " +
                    "WHERE fr.receiver.id = :receiverId";
}
