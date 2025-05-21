package ru.pocgg.SNSApp.services.DAO.requests;

public class CommunityMemberServiceDAORequests {
    public static final String GET_BY_ID =
            "SELECT cm FROM CommunityMember cm " +
                    "JOIN FETCH cm.community JOIN FETCH cm.member " +
                    "WHERE cm.id = :id";
    public static final String GET_ALL =
            "SELECT cm " +
                    "FROM CommunityMember cm " +
                    "JOIN FETCH cm.community " +
                    "JOIN FETCH cm.member";
    public static final String GET_BY_COMMUNITY_ID =
            "SELECT cm " +
                    "FROM CommunityMember cm " +
                    "JOIN FETCH cm.community " +
                    "JOIN FETCH cm.member " +
                    "WHERE cm.community.id = :communityId";
    public static final String GET_BY_MEMBER_ID =
            "SELECT cm.community " +
                    "FROM CommunityMember cm " +
                    "WHERE cm.member.id = :memberId";
}
