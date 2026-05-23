package ru.itis.aleksander.formach.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.ModelAndView;
import ru.itis.aleksander.formach.exсeption.AccessDeniedException;
import ru.itis.aleksander.formach.exсeption.AlreadyExistsException;
import ru.itis.aleksander.formach.exсeption.NotFoundException;

import java.util.Map;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Object handleFileTooLarge(HttpServletRequest request) {
        if (isAjax(request)) {
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                    .body(Map.of("error", "Файл слишком большой. Максимальный размер — 10 МБ."));
        }
        ModelAndView mav = new ModelAndView("error");
        mav.setStatus(HttpStatus.PAYLOAD_TOO_LARGE);
        mav.addObject("message", "Файл слишком большой. Максимальный размер — 10 МБ.");
        return mav;
    }

    @ExceptionHandler(NotFoundException.class)
    public Object handleNotFound(NotFoundException ex, HttpServletRequest request) {
        log.warn("Не найдено: {}", ex.getMessage());

        if (isAjax(request)) {
            return ResponseEntity.status(404)
                    .body(Map.of("error", ex.getMessage()));
        }

        ModelAndView mav = new ModelAndView("error/404");
        mav.addObject("message", ex.getMessage());
        return mav;
    }
    @ExceptionHandler(AlreadyExistsException.class)
    public Object handleConflict(AlreadyExistsException ex, HttpServletRequest request) {
        log.warn("Конфликт данных: {}", ex.getMessage());

        if (isAjax(request)) {
            return ResponseEntity.status(409)
                    .body(Map.of("error", ex.getMessage()));
        }

        ModelAndView mav = new ModelAndView("error/409");
        mav.addObject("message", ex.getMessage());
        return mav;
    }
    @ExceptionHandler(Exception.class)
    public Object handleGeneral(Exception ex, HttpServletRequest request) {
        log.error("Непредвиденная ошибка: {}", ex.getMessage(), ex);

        if (isAjax(request)) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Внутренняя ошибка сервера"));
        }

        ModelAndView mav = new ModelAndView("error");
        mav.addObject("message", "Что-то пошло не так");
        return mav;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Object handleBadRequest(IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("Некорректные данные: {}", ex.getMessage());

        if (isAjax(request)) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }

        ModelAndView mav = new ModelAndView("error");
        mav.setStatus(HttpStatus.BAD_REQUEST);
        mav.addObject("message", ex.getMessage());
        return mav;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public Object handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Доступ запрещён: {}", ex.getMessage());

        if (isAjax(request)) {
            return ResponseEntity.status(403)
                    .body(Map.of("error", ex.getMessage()));
        }

        ModelAndView mav = new ModelAndView("error");
        mav.addObject("message", ex.getMessage());
        return mav;
    }

    private boolean isAjax(HttpServletRequest request) {
        return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
    }
}