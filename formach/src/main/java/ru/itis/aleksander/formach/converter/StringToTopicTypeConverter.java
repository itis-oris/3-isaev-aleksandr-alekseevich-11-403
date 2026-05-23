package ru.itis.aleksander.formach.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.itis.aleksander.formach.entity.TopicType;

@Component
public class StringToTopicTypeConverter implements Converter<String, TopicType> {

    @Override
    public TopicType convert(String source) {
        if (source == null || source.isBlank()) return null;
        return TopicType.valueOf(source.trim().toUpperCase());
    }
}
