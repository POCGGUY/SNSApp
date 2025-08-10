package ru.pocgg.SNSApp.services.DAO.requests;

public class NotificationServiceDAORequests {
    public static final String GET_BY_ID =
            "SELECT n FROM Notification n " +
                    "JOIN FETCH n.receiver " +
                    "WHERE n.id = :id";
    public static final String GET_ALL =
            "SELECT n " +
                    "FROM Notification n " +
                    "JOIN FETCH n.receiver";

    public static final String GET_BY_RECEIVER_ID =
            "SELECT n " +
                    "FROM Notification n " +
                    "JOIN FETCH n.receiver " +
                    "WHERE n.receiver.id = :receiverId";
    public static final String GET_NOT_SEEN_BY_RECEIVER_ID =
            "SELECT n " +
                    "FROM Notification n " +
                    "JOIN FETCH n.receiver " +
                    "WHERE n.receiver.id = :receiverId AND n.read = false";
}
