package ru.pocgg.SNSApp.services.DAO.requests;

public class ChatMemberServiceDAORequests {
    public static final String GET_BY_ID =
            "SELECT cm FROM ChatMember cm " +
                    "JOIN FETCH cm.chat JOIN FETCH cm.member " +
                    "WHERE cm.id = :id";
    public static final String GET_ALL =
            "SELECT cm " +
                    "FROM ChatMember cm " +
                    "JOIN FETCH cm.chat " +
                    "JOIN FETCH cm.member";
    public static final String GET_BY_CHAT_ID =
            "SELECT cm " +
                    "FROM ChatMember cm " +
                    "JOIN FETCH cm.chat " +
                    "JOIN FETCH cm.member " +
                    "WHERE cm.chat.id = :chatId";
    public static final String GET_CHATS_BY_MEMBER_ID =
            "SELECT cm.chat " +
                    "FROM ChatMember cm " +
                    "WHERE cm.member.id = :memberId";
}
