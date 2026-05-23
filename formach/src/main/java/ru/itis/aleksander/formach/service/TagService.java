package ru.itis.aleksander.formach.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itis.aleksander.formach.entity.Tag;
import ru.itis.aleksander.formach.repository.TagRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    @Cacheable("tags")
    public List<Tag> findAll() {
        return tagRepository.findAll();
    }

    public Tag save(Tag newTag) {
        return tagRepository.save(newTag);
    }

    @Transactional
    @CacheEvict(value = "tags", allEntries = true)
    public Set<Tag> resolveAll(List<Long> existingIds, String newTagsString) {
        Set<Tag> tags = new HashSet<>();

        if (existingIds != null && !existingIds.isEmpty()) {
            tags.addAll(tagRepository.findAllById(existingIds));
        }

        if (newTagsString != null && !newTagsString.isBlank()) {
            String[] names = newTagsString.split(",");
            for (String name : names) {
                String trimmed = name.trim();
                if (trimmed.isEmpty()) continue;

                Tag tag = tagRepository.findByName(trimmed)
                        .orElseGet(() -> tagRepository.save(
                                Tag.builder().name(trimmed).build()));
                tags.add(tag);
            }
        }

        return tags;
    }
}