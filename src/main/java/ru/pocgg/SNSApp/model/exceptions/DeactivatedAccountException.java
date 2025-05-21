package ru.pocgg.SNSApp.model.exceptions;

public class DeactivatedAccountException extends RuntimeException {
    public DeactivatedAccountException(String message) {
        super(message);
    }
}
