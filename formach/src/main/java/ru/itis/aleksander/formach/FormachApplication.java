package ru.itis.aleksander.formach;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class FormachApplication {

	public static void main(String[] args) {
		SpringApplication.run(FormachApplication.class, args);
	}

}
