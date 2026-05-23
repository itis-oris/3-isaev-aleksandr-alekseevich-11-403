package ru.itis.aleksander.formach.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.itis.aleksander.formach.entity.User;
import ru.itis.aleksander.formach.repository.UserRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class MentionService {

    private static final Pattern MENTION = Pattern.compile("@([a-zA-Z0-9_]{3,20})");

    private final UserRepository userRepository;

    public List<User> extract(String text) {
        if (text == null || text.isEmpty()) return List.of();
        Set<String> logins = new HashSet<>();
        Matcher m = MENTION.matcher(text);
        while (m.find()) {
            logins.add(m.group(1));
        }
        if (logins.isEmpty()) return List.of();
        List<User> result = new ArrayList<>();
        for (String login : logins) {
            userRepository.findByLogin(login).ifPresent(result::add);
        }
        return result;
    }
}
