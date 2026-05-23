package ru.itis.aleksander.formach.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.itis.aleksander.formach.entity.Subscription;
import ru.itis.aleksander.formach.entity.User;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    boolean existsByFollowerAndFollowing(User follower, User following);

    Optional<Subscription> findByFollowerAndFollowing(User follower, User following);

    List<Subscription> findByFollower(User follower);

    List<Subscription> findByFollowing(User following);

    long countByFollower(User follower);

    long countByFollowing(User following);
}
