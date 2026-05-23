package ru.itis.aleksander.formach.exсeption;

public abstract class AppException extends RuntimeException {
    public AppException(String message) {
        super(message);
    }
}
