package ru.pocgg.SNSApp.services.DAO.requests;

public class CommunityServiceDAORequests {
    public static final String GET_BY_ID =
            "SELECT c FROM Community c " +
                    "JOIN FETCH c.owner " +
                    "WHERE c.id = :id";
    public static final String GET_ALL =
            "SELECT c " +
                    "FROM Community c " +
                    "JOIN FETCH c.owner ";
}
