package ru.itis.aleksander.formach.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itis.aleksander.formach.entity.Subscription;
import ru.itis.aleksander.formach.entity.User;
import ru.itis.aleksander.formach.exсeption.NotFoundException;
import ru.itis.aleksander.formach.repository.SubscriptionRepository;
import ru.itis.aleksander.formach.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    public boolean isFollowing(User follower, User following) {
        return subscriptionRepository.existsByFollowerAndFollowing(follower, following);
    }

    public long countFollowers(User user) {
        return subscriptionRepository.countByFollowing(user);
    }

    public long countFollowing(User user) {
        return subscriptionRepository.countByFollower(user);
    }

    public List<User> getFollowers(User user) {
        return subscriptionRepository.findByFollowing(user)
                .stream().map(Subscription::getFollower).toList();
    }

    public List<User> getFollowing(User user) {
        return subscriptionRepository.findByFollower(user)
                .stream().map(Subscription::getFollowing).toList();
    }

    @Transactional
    public void follow(User follower, Long targetId) {
        if (follower.getId().equals(targetId)) return;
        User target = userRepository.findById(targetId)
                .orElseThrow(() -> new NotFoundException("Пользователь", targetId));
        if (!subscriptionRepository.existsByFollowerAndFollowing(follower, target)) {
            subscriptionRepository.save(Subscription.builder()
                    .follower(follower)
                    .following(target)
                    .build());
        }
    }

    @Transactional
    public void unfollow(User follower, Long targetId) {
        User target = userRepository.findById(targetId)
                .orElseThrow(() -> new NotFoundException("Пользователь", targetId));
        subscriptionRepository.findByFollowerAndFollowing(follower, target)
                .ifPresent(subscriptionRepository::delete);
    }
}
