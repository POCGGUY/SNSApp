package ru.pocgg.SNSApp.utils;

public class DateTimeFormatter {
    public static final java.time.format.DateTimeFormatter Format = java.time.format.DateTimeFormatter.ISO_INSTANT;
    public static final java.time.format.DateTimeFormatter birthDate = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");
}
