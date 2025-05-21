package ru.pocgg.SNSApp.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import ru.pocgg.SNSApp.model.exceptions.BadEnumException;

public enum Gender {
    MALE, FEMALE;

    public static final String maleString = "MALE";
    public static final String femaleString = "FEMALE";

    @Override
    public String toString() {
        return switch(this){
            case MALE -> maleString;
            case FEMALE -> femaleString;
        };
    }

    public static Gender fromString(String s) {
        return switch(s){
            case maleString -> MALE;
            case femaleString -> FEMALE;
            default -> throw new BadEnumException("Invalid gender");
        };
    }
}
