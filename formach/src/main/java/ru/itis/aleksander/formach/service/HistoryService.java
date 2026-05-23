package ru.itis.aleksander.formach.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itis.aleksander.formach.dto.response.TopicResponse;
import ru.itis.aleksander.formach.entity.User;
import ru.itis.aleksander.formach.repository.TopicRepository;
import ru.itis.aleksander.formach.repository.TopicViewRepository;

@Service
@RequiredArgsConstructor
public class HistoryService {

    private final TopicViewRepository topicViewRepository;
    private final TopicRepository topicRepository;
    private final TopicService topicService;

    @Transactional(readOnly = true)
    public Page<TopicResponse> myHistory(User user, int page, int size) {
        return topicViewRepository
                .findByUserOrderByViewedAtDesc(user, PageRequest.of(page, size))
                .map(tv -> topicService.toResponse(tv.getTopic()));
    }

    @Transactional(readOnly = true)
    public Page<TopicResponse> myParticipated(User user, int page, int size) {
        return topicRepository
                .findTopicsWhereUserPosted(user, PageRequest.of(page, size))
                .map(topicService::toResponse);
    }
}
