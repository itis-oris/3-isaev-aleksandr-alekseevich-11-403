package ru.itis.aleksander.formach.exсeption;

public class AlreadyExistsException extends AppException {

    private final String fieldName;

    public AlreadyExistsException(String fieldName, String displayName, String value) {
        super(displayName + " уже используется: " + value);
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }
}