package ru.itis.aleksander.formach.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;

@Configuration
@Slf4j
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        try (RedisConnection conn = factory.getConnection()) {
            conn.ping();
            RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(Duration.ofMinutes(10))
                    .disableCachingNullValues();
            log.info("Redis доступен — используется RedisCache (TTL 10 мин)");
            return RedisCacheManager.builder(factory)
                    .withCacheConfiguration("tags", config)
                    .build();
        } catch (Exception e) {
            log.warn("Redis недоступен, переключаемся на in-memory кэш: {}", e.getMessage());
            return new ConcurrentMapCacheManager("tags");
        }
    }
}
