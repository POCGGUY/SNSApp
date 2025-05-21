package ru.pocgg.SNSApp.model.exceptions;

public class UnknownEntityException extends RuntimeException {
    public UnknownEntityException(String message) {
        super(message);
    }
}
