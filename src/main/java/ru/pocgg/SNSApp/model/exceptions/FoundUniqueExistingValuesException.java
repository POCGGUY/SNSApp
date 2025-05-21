package ru.pocgg.SNSApp.model.exceptions;

public class FoundUniqueExistingValuesException extends RuntimeException {
    public FoundUniqueExistingValuesException(String message) {
        super(message);
    }
}
