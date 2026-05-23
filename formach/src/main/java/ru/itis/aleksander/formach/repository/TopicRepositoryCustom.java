package ru.itis.aleksander.formach.repository;

import ru.itis.aleksander.formach.entity.Topic;
import ru.itis.aleksander.formach.entity.TopicType;

import java.time.LocalDateTime;
import java.util.List;

public interface TopicRepositoryCustom {

    List<Topic> advancedSearch(String titleContains,
                               TopicType type,
                               Boolean isClosed,
                               LocalDateTime createdAfter);
}
