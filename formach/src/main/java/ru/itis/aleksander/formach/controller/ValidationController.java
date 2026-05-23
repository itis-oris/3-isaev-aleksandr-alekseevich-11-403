package ru.itis.aleksander.formach.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.itis.aleksander.formach.service.UserService;

import java.util.Map;

@RestController
@RequestMapping("/api/check")
@RequiredArgsConstructor
public class ValidationController {
    private final UserService userService;

    @GetMapping("/login")
    public ResponseEntity<Map<String, Boolean>> checkLogin(@RequestParam String value) {
        boolean exists = userService.isLoginTaken(value);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    @GetMapping("/email")
    public ResponseEntity<Map<String, Boolean>> checkEmail(@RequestParam String value) {
        boolean exists = userService.isEmailTaken(value);
        return ResponseEntity.ok(Map.of("exists", exists));
    }
}
