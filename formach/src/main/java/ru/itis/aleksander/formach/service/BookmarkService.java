package ru.itis.aleksander.formach.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itis.aleksander.formach.dto.response.TopicResponse;
import ru.itis.aleksander.formach.entity.Bookmark;
import ru.itis.aleksander.formach.entity.Topic;
import ru.itis.aleksander.formach.entity.User;
import ru.itis.aleksander.formach.exсeption.NotFoundException;
import ru.itis.aleksander.formach.repository.BookmarkRepository;
import ru.itis.aleksander.formach.repository.TopicRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final TopicRepository topicRepository;
    private final TopicService topicService;

    @Transactional
    public boolean toggle(User user, Long topicId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new NotFoundException("Тема", topicId));

        Optional<Bookmark> existing = bookmarkRepository.findByUserAndTopic(user, topic);
        if (existing.isPresent()) {
            bookmarkRepository.delete(existing.get());
            log.info("{} убрал из сохранённых тему #{}", user.getLogin(), topicId);
            return false;
        }
        bookmarkRepository.save(Bookmark.builder().user(user).topic(topic).build());
        log.info("{} сохранил тему #{}", user.getLogin(), topicId);
        return true;
    }

    @Transactional(readOnly = true)
    public boolean isBookmarked(User user, Long topicId) {
        return topicRepository.findById(topicId)
                .map(t -> bookmarkRepository.existsByUserAndTopic(user, t))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public List<TopicResponse> findAllOf(User user) {
        return bookmarkRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(Bookmark::getTopic)
                .map(topicService::toResponse)
                .toList();
    }
}
