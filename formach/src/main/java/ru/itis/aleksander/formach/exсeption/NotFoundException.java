package ru.itis.aleksander.formach.exсeption;

public class NotFoundException extends AppException {
    public NotFoundException(String entity, Object identifier) {
        super(entity + " не найден: " + identifier);
    }
}
