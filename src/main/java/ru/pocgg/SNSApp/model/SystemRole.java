package ru.pocgg.SNSApp.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import ru.pocgg.SNSApp.model.exceptions.BadEnumException;

import java.util.Locale;

public enum SystemRole {
    USER, MODERATOR, ADMIN;

    public static final String userString = "USER";
    public static final String moderatorString = "MODERATOR";
    public static final String adminString = "ADMIN";

    @Override
    public String toString() {
        return switch(this){
            case USER -> userString;
            case MODERATOR -> moderatorString;
            case ADMIN -> adminString;
        };
    }

    public static SystemRole fromString(String s) {
        return switch(s){
            case userString -> USER;
            case moderatorString -> MODERATOR;
            case adminString -> ADMIN;
            default -> throw new BadEnumException("Invalid system role");
        };
    }
}
