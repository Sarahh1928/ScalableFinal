package com.ecommerce.ProductService.config;

import com.ecommerce.ProductService.Dto.UserSessionDTO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class RedisConfig {

    // Define a RedisTemplate bean for managing Redis operations
    @Bean
    public RedisTemplate<String, UserSessionDTO> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, UserSessionDTO> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        // Use Jackson2JsonRedisSerializer for the value
        Jackson2JsonRedisSerializer<UserSessionDTO> serializer = new Jackson2JsonRedisSerializer<>(UserSessionDTO.class);

        // Set the key serializer to StringRedisSerializer since keys are typically strings
        template.setKeySerializer(new StringRedisSerializer());

        // Set value serializer to Jackson2JsonRedisSerializer for UserSession objects
        template.setValueSerializer(serializer);

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
                                new Jackson2JsonRedisSerializer<>(UserSessionDTO.class))));

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}
