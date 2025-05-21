package ru.pocgg.SNSApp.services.DAO.requests;

public class CommunityInvitationServiceDAORequests {
    public static final String GET_BY_ID =
            "SELECT ci FROM CommunityInvitation ci " +
                    "JOIN FETCH ci.sender JOIN FETCH ci.receiver JOIN FETCH ci.community " +
                    "WHERE ci.id = :id";
    public static final String GET_ALL =
            "SELECT ci " +
                    "FROM CommunityInvitation ci " +
                    "JOIN FETCH ci.sender " +
                    "JOIN FETCH ci.receiver " +
                    "JOIN FETCH ci.community";
    public static final String GET_BY_SENDER_ID =
            "SELECT ci " +
                    "FROM CommunityInvitation ci " +
                    "JOIN FETCH ci.sender " +
                    "JOIN FETCH ci.receiver " +
                    "JOIN FETCH ci.community " +
                    "WHERE ci.sender.id = :senderId";
    public static final String GET_BY_RECEIVER_ID =
            "SELECT ci " +
                    "FROM CommunityInvitation ci " +
                    "JOIN FETCH ci.sender " +
                    "JOIN FETCH ci.receiver " +
                    "JOIN FETCH ci.community " +
                    "WHERE ci.receiver.id = :receiverId";
    public static final String GET_BY_COMMUNITY_ID =
            "SELECT ci " +
                    "FROM CommunityInvitation ci " +
                    "JOIN FETCH ci.sender " +
                    "JOIN FETCH ci.receiver " +
                    "JOIN FETCH ci.community " +
                    "WHERE ci.community.id = :communityId";
    public static final String GET_BY_COMMUNITY_AND_RECEIVER_ID =
            "SELECT ci " +
                    "FROM CommunityInvitation ci " +
                    "JOIN FETCH ci.sender " +
                    "JOIN FETCH ci.receiver " +
                    "JOIN FETCH ci.community " +
                    "WHERE ci.community.id = :communityId " +
                    "AND ci.receiver.id = :receiverId";
    public static final String DELETE_BY_SENDER_ID =
            "DELETE FROM CommunityInvitation ci " +
                    "WHERE ci.sender.id = :senderId";
    public static final String DELETE_BY_RECEIVER_ID =
            "DELETE FROM CommunityInvitation ci " +
                    "WHERE ci.receiver.id = :receiverId";
    public static final String DELETE_BY_COMMUNITY_ID =
            "DELETE FROM CommunityInvitation ci " +
                    "WHERE ci.community.id = :communityId";
    public static final String DELETE_BY_COMMUNITY_AND_SENDER =
            "DELETE FROM CommunityInvitation ci " +
                    "WHERE ci.community.id = :communityId " +
                    "  AND ci.sender.id = :senderId";
}
