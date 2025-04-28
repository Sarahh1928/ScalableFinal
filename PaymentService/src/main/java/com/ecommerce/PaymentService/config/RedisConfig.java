package com.ecommerce.ProductService.config;

import com.ecommerce.ProductService.Dto.UserSessionDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    // 👇 This creates the actual Redis connection using your properties
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisHost);
        config.setPort(redisPort);
        return new LettuceConnectionFactory(config);
    }

    // 👇 Your RedisTemplate bean (for doing CRUD ops in Redis easily)
    @Bean
    public RedisTemplate<String, UserSessionDTO> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, UserSessionDTO> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        Jackson2JsonRedisSerializer<UserSessionDTO> serializer = new Jackson2JsonRedisSerializer<>(UserSessionDTO.class);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);

        return template;
    }

    // 👇 Your CacheManager bean (for Spring Cache abstraction)
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(90))
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
