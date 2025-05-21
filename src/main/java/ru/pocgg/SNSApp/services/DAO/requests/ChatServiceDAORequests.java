package ru.pocgg.SNSApp.services.DAO.requests;

public class ChatServiceDAORequests {
    public static final String GET_BY_ID =
            "SELECT c FROM Chat c " +
                    "JOIN FETCH c.owner " +
                    "WHERE c.id = :id";
    public static final String GET_ALL =
            "SELECT c " +
                    "FROM Chat c " +
                    "JOIN FETCH c.owner " +
                    "WHERE c.deleted = false";
}
