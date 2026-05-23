package ru.itis.aleksander.formach.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itis.aleksander.formach.entity.User;
import ru.itis.aleksander.formach.repository.UserRepository;
import ru.itis.aleksander.formach.security.model.SecurityUser;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = userRepository.findByLogin(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден " + username));

        if (Boolean.TRUE.equals(user.getIsBanned())
                && user.getBannedUntil() != null
                && user.getBannedUntil().isBefore(LocalDateTime.now())) {
            log.info("Авто-разбан пользователя {}: срок бана истёк {}",
                    user.getLogin(), user.getBannedUntil());
            user.setIsBanned(false);
            user.setBanReason(null);
            user.setBannedUntil(null);
            userRepository.save(user);
        }

        return new SecurityUser(user);
    }
}
