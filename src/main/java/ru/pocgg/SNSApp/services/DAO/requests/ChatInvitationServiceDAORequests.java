package ru.pocgg.SNSApp.services.DAO.requests;

public class ChatInvitationServiceDAORequests {
    public static final String GET_BY_ID =
            "SELECT ci " +
                    "FROM ChatInvitation ci " +
                    "JOIN FETCH ci.sender " +
                    "JOIN FETCH ci.receiver " +
                    "JOIN FETCH ci.chat " +
                    "WHERE ci.id = :id";
    public static final String GET_ALL = "FROM ChatInvitation";
    public static final String GET_BY_RECEIVER =
            "FROM ChatInvitation WHERE receiver.id = :receiverId";
    public static final String GET_BY_SENDER =
            "FROM ChatInvitation WHERE sender.id = :senderId";
    public static final String GET_BY_CHAT =
            "FROM ChatInvitation WHERE chat.id = :chatId";
    public static final String DELETE_BY_RECEIVER =
            "DELETE FROM ChatInvitation WHERE receiver.id = :receiverId";
    public static final String DELETE_BY_SENDER =
            "DELETE FROM ChatInvitation WHERE sender.id = :senderId";
    public static final String DELETE_BY_CHAT =
            "DELETE FROM ChatInvitation WHERE chat.id = :chatId";
}
