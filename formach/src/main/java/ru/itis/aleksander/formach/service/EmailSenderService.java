package ru.itis.aleksander.formach.service;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class EmailSenderService {

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${brevo.api.key}")
    private String apiKey;

    @Value("${brevo.sender.email}")
    private String senderEmail;

    @Value("${brevo.sender.name}")
    private String senderName;

    private final OkHttpClient httpClient = new OkHttpClient();

    public void sendVerificationEmail(String toEmail, String token) {
        String verificationUrl = baseUrl + "/auth/verify?token=" + token;

        String jsonBody = """
                {
                    "sender": {"name": "%s", "email": "%s"},
                    "to": [{"email": "%s"}],
                    "subject": "Подтвердите вашу почту",
                    "htmlContent": "<h3>Добро пожаловать!</h3><p>Перейдите по ссылке для подтверждения:</p><a href='%s'>Подтвердить email</a><p>Ссылка действительна 24 часа.</p>"
                }
                """.formatted(senderName, senderEmail, toEmail, verificationUrl);

        RequestBody body = RequestBody.create(
                jsonBody, MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url("https://api.brevo.com/v3/smtp/email")
                .post(body)
                .addHeader("api-key", apiKey)
                .addHeader("Accept", "application/json")
                .build();

        sendRaw(request, toEmail);
    }

    public void sendPasswordChangeCode(String toEmail, String code) {
        String jsonBody = """
                {
                    "sender": {"name": "%s", "email": "%s"},
                    "to": [{"email": "%s"}],
                    "subject": "Код подтверждения смены пароля",
                    "htmlContent": "<h3>Смена пароля</h3><p>Ваш код подтверждения:</p><h2 style='letter-spacing:6px'>%s</h2><p>Код действителен 15 минут. Если вы не запрашивали смену пароля — проигнорируйте письмо.</p>"
                }
                """.formatted(senderName, senderEmail, toEmail, code);

        RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url("https://api.brevo.com/v3/smtp/email")
                .post(body)
                .addHeader("api-key", apiKey)
                .addHeader("Accept", "application/json")
                .build();

        sendRaw(request, toEmail);
    }

    private void sendRaw(Request request, String toEmail) {
        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body().string();

            if (response.isSuccessful()) {
                log.info("Письмо отправлено на {}", toEmail);
            } else {
                log.error("Ошибка Brevo: {} {}", response.code(), responseBody);
                throw new RuntimeException("Не удалось отправить письмо");
            }
        } catch (IOException e) {
            log.error("Ошибка соединения с Brevo", e);
            throw new RuntimeException("Ошибка отправки письма", e);
        }
    }
}