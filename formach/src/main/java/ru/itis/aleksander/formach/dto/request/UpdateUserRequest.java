package ru.itis.aleksander.formach.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import ru.itis.aleksander.formach.entity.Gender;

@Data
public class UpdateUserRequest {

    @NotBlank(message = "Логин не может быть пустым")
    @Size(min = 3, max = 20)
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Логин может содержать только буквы, цифры и _")
    private String login;

    @NotNull(message = "Выберите пол")
    private Gender gender;

    @NotBlank(message = "Имя не может быть пустым")
    @Size(min = 2, max = 50)
    @Pattern(regexp = "^[a-zA-Zа-яА-Я]+$")
    private String firstName;

    @NotBlank(message = "Фамилия не может быть пустой")
    @Size(min = 2, max = 50)
    @Pattern(regexp = "^[a-zA-Zа-яА-Я]+$")
    private String lastName;
}