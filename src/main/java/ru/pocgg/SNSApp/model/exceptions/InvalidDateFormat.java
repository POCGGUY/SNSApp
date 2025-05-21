package ru.pocgg.SNSApp.model.exceptions;

public class InvalidDateFormat extends RuntimeException {
    public InvalidDateFormat(String message) {
        super(message);
    }
}
