package ru.pocgg.SNSApp.services.DAO.requests;

public class UserServiceDAORequests {
    public final static String GET_ALL = "SELECT u FROM User u " +
            "WHERE u.deleted = false " +
            "AND u.banned = false";
    public final static String GET_BY_EMAIL = "FROM User WHERE email = :email";
    public final static String GET_BY_USER_NAME = "FROM User WHERE userName = :userName";
}
