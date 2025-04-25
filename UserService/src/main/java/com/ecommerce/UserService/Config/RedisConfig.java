package com.ecommerce.UserService.Config;

import com.ecommerce.UserService.models.UserSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class RedisConfig {

    // Define a RedisTemplate bean for managing Redis operations
    @Bean
    public RedisTemplate<String, UserSession> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, UserSession> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        // Set up the key and value serializers for RedisTemplate
        Jackson2JsonRedisSerializer<UserSession> serializer = new Jackson2JsonRedisSerializer<>(UserSession.class);
        template.setValueSerializer(serializer);
        template.setKeySerializer(new Jackson2JsonRedisSerializer<>(String.class));

        return template;
    }

    // Define the cache manager bean
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(90)) // Default TTL for all caches
                .disableCachingNullValues()
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new Jackson2JsonRedisSerializer<>(Object.class)));

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        cacheConfigurations.put("user_session",
                defaultConfig.serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new Jackson2JsonRedisSerializer<>(UserSession.class))));

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}
