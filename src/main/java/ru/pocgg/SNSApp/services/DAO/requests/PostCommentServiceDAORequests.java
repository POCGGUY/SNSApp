package ru.pocgg.SNSApp.services.DAO.requests;

public class PostCommentServiceDAORequests {
    public static final String GET_BY_ID =
            "SELECT pc FROM PostComment pc " +
                    "JOIN FETCH pc.post JOIN FETCH pc.author " +
                    "WHERE pc.id = :id";
    public static final String GET_ALL =
            "SELECT pc " +
                    "FROM PostComment pc " +
                    "JOIN FETCH pc.author " +
                    "WHERE pc.deleted = false";

    public static final String GET_BY_POST_ID =
            "SELECT pc " +
                    "FROM PostComment pc " +
                    "JOIN FETCH pc.author " +
                    "WHERE pc.post.id = :postId " +
                    "AND pc.deleted = false";
}
