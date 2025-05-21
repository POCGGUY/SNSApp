package ru.pocgg.SNSApp.services.DAO.requests;

public class PrivateMessageDAORequests {
    public static final String GET_BY_ID =
            "SELECT pm FROM PrivateMessage pm " +
                    "JOIN FETCH pm.sender JOIN FETCH pm.receiver " +
                    "WHERE pm.id = :id";
    public static final String GET_BY_RECEIVER_ID =
                    "SELECT pm " +
                    "FROM PrivateMessage pm " +
                    "JOIN FETCH pm.sender " +
                    "WHERE pm.receiver.id = :receiverId " +
                    "AND pm.deleted = false";
    public static final String GET_BY_RECEIVER_AND_SENDER_ID =
                    "SELECT pm " +
                    "FROM PrivateMessage pm " +
                    "JOIN FETCH pm.sender " +
                    "WHERE pm.receiver.id = :receiverId " +
                    "AND pm.sender.id = :senderId " +
                    "AND pm.deleted = false";
}
