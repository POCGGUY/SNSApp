package ru.pocgg.SNSApp.services.DAO.requests;

public class ChatMessageServiceDAORequests {
    public static final String GET_BY_ID =
            "SELECT m FROM ChatMessage m " +
                    "JOIN FETCH m.chat JOIN FETCH m.sender " +
                    "WHERE m.id = :id";
    public static final String GET_ALL =
            "SELECT m " +
                    "FROM ChatMessage m " +
                    "JOIN FETCH m.chat " +
                    "JOIN FETCH m.sender " +
                    "WHERE m.deleted = false";
    public static final String GET_BY_CHAT_ID =
            "SELECT m " +
                    "FROM ChatMessage m " +
                    "JOIN FETCH m.chat " +
                    "JOIN FETCH m.sender " +
                    "WHERE m.chat.id = :chatId " +
                    "  AND m.deleted = false";
    public static final String GET_BY_SENDER_ID =
            "SELECT m " +
                    "FROM ChatMessage m " +
                    "JOIN FETCH m.chat " +
                    "JOIN FETCH m.sender " +
                    "WHERE m.sender.id = :senderId " +
                    "  AND m.deleted = false";
    public static final String GET_BY_CHAT_ID_AND_SENDER_ID =
            "SELECT m " +
                    "FROM ChatMessage m " +
                    "JOIN FETCH m.chat " +
                    "JOIN FETCH m.sender " +
                    "WHERE m.chat.id = :chatId " +
                    "  AND m.sender.id = :senderId " +
                    "  AND m.deleted = false";
}
