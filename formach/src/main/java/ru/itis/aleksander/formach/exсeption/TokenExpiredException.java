package ru.itis.aleksander.formach.exсeption;

public class TokenExpiredException extends AppException {
    public TokenExpiredException() {
        super("Срок действия ссылки истёк. Запросите новое письмо.");
    }
}