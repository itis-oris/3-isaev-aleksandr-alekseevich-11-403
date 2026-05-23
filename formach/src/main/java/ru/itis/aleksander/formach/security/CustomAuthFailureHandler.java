package ru.itis.aleksander.formach.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import ru.itis.aleksander.formach.entity.User;
import ru.itis.aleksander.formach.repository.UserRepository;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAuthFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final UserRepository userRepository;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception)
            throws IOException, ServletException {

        String username = request.getParameter("username");
        String target;

        if (exception instanceof LockedException) {
            User banned = userRepository.findByLogin(username).orElse(null);
            String reason = banned != null ? banned.getBanReason() : null;
            String until = (banned != null && banned.getBannedUntil() != null)
                    ? banned.getBannedUntil().format(
                            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
                    : null;

            StringBuilder params = new StringBuilder("/login?error=banned");
            if (username != null && !username.isBlank()) {
                params.append("&login=").append(URLEncoder.encode(username, StandardCharsets.UTF_8));
            }
            if (reason != null && !reason.isBlank()) {
                params.append("&reason=").append(URLEncoder.encode(reason, StandardCharsets.UTF_8));
            }
            if (until != null) {
                params.append("&until=").append(URLEncoder.encode(until, StandardCharsets.UTF_8));
            }
            target = params.toString();
            log.warn("Заблокированный пользователь пытался войти: {}", username);
        } else if (exception instanceof DisabledException) {
            target = "/login?error=notverified";
            log.warn("Не верифицированный пользователь пытался войти: {}", username);
        } else {
            target = "/login?error=invalid";
            log.info("Неудачный вход: {}", username);
        }

        setDefaultFailureUrl(target);
        super.onAuthenticationFailure(request, response, exception);
    }
}
