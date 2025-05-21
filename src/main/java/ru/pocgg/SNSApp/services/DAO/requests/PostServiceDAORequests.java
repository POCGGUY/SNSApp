package ru.pocgg.SNSApp.services.DAO.requests;

public class PostServiceDAORequests {
    public static final String GET_BY_ID =
            "SELECT p FROM Post p " +
                    "LEFT JOIN FETCH p.ownerUser LEFT JOIN FETCH p.ownerCommunity " +
                    "JOIN FETCH p.author " +
                    "WHERE p.id = :id";

    public static final String GET_ALL =
            "SELECT p " +
                    "FROM Post p " +
                    " JOIN FETCH p.author " +
                    " LEFT JOIN FETCH p.ownerUser " +
                    " LEFT JOIN FETCH p.ownerCommunity " +
                    "WHERE p.deleted = false";

    public static final String GET_BY_COMMUNITY_OWNER_ID =
            "SELECT p " +
                    "FROM Post p " +
                    "JOIN FETCH p.author " +
                    "LEFT JOIN FETCH p.ownerUser " +
                    "LEFT JOIN FETCH p.ownerCommunity " +
                    "WHERE p.ownerCommunity.id = :ownerId " +
                    "AND p.deleted = false";

    public static final String GET_BY_USER_OWNER_ID =
            "SELECT p " +
                    "FROM Post p " +
                    "JOIN FETCH p.author " +
                    "LEFT JOIN FETCH p.ownerUser " +
                    "LEFT JOIN FETCH p.ownerCommunity " +
                    "WHERE p.ownerUser.id = :ownerId " +
                    " AND p.deleted = false";

    public static final String GET_BY_AUTHOR_ID =
            "SELECT p " +
                    "FROM Post p " +
                    "JOIN FETCH p.author " +
                    "LEFT JOIN FETCH p.ownerUser " +
                    "LEFT JOIN FETCH p.ownerCommunity " +
                    "WHERE p.author.id = :authorId " +
                    "AND p.deleted = false";
}
