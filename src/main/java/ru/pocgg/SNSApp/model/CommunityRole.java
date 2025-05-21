package ru.pocgg.SNSApp.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import ru.pocgg.SNSApp.model.exceptions.BadEnumException;

public enum CommunityRole {
    MEMBER, MODERATOR, OWNER;

    public static final String memberString = "MEMBER";
    public static final String moderatorString = "MODERATOR";
    public static final String ownerString = "OWNER";

    @Override
    public String toString() {
        return switch(this){
            case MEMBER -> memberString;
            case MODERATOR -> moderatorString;
            case OWNER -> ownerString;
        };
    }

    public static CommunityRole fromString(String s) {
        return switch(s){
            case memberString -> MEMBER;
            case moderatorString -> MODERATOR;
            case ownerString -> OWNER;
            default -> throw new BadEnumException("Invalid community role");
        };
    }
}
