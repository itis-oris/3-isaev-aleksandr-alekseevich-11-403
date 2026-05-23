package ru.itis.aleksander.formach.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI formachOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Formach Forum API")
                .description("REST API форума Formach: темы, посты, лайки. " +
                        "Для тестирования используйте файл src/test/http/posts.http " +
                        "или Swagger UI ниже. Авторизация — через cookie JSESSIONID (войдите через /login).")
                .contact(new Contact().name("Formach")));
    }
}
