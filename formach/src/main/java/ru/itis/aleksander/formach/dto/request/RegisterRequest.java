package ru.itis.aleksander.formach.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import ru.itis.aleksander.formach.entity.Gender;

@Data
public class RegisterRequest {

    @NotBlank(message = "Логин не может быть пустым")
    @Size(min = 3, max = 20, message = "Логин должен содержать от 3 до 20 символов")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Логин может содержать только буквы, цифры и знак подчеркивания")
    private String login;

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Введите корректный email адрес")
    private String email;

    @NotNull(message = "Пожалуйста, выберите ваш пол")
    private Gender gender;

    @NotBlank(message = "Имя не может быть пустым")
    @Size(min = 2, max = 50, message = "Имя должно содержать от 2 до 50 символов")
    @Pattern(regexp = "^[a-zA-Zа-яА-Я]+$", message = "Имя может содержать только буквы")
    private String firstName;

    @NotBlank(message = "Фамилия не может быть пустой")
    @Size(min = 2, max = 50, message = "Фамилия должна содержать от 2 до 50 символов")
    @Pattern(regexp = "^[a-zA-Zа-яА-Я]+$", message = "Фамилия может содержать только буквы")
    private String lastName;

    @NotBlank(message = "Пароль не может быть пустым")
    @Size(min = 8, max = 50, message = "Пароль должен содержать от 8 до 50 символов")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).*$",
            message = "Пароль должен содержать хотя бы одну цифру, одну строчную и одну заглавную букву")
    private String password;
}