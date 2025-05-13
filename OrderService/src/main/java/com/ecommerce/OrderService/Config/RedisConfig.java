package com.ecommerce.OrderService.Config;

import com.ecommerce.OrderService.Dto.UserSessionDTO;
import com.ecommerce.OrderService.models.Cart;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
@Configuration
public class RedisConfig {

    @Bean
    @Primary
    @Qualifier("cartRedisTemplate")
    public RedisTemplate<String, Cart> cartRedisTemplate(@Qualifier("orderCacheConnectionFactory") RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Cart> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(Cart.class));

        return template;
    }

    @Bean
    @Qualifier("userSessionDTORedisTemplate")
    public RedisTemplate<String, UserSessionDTO> userSessionDTORedisTemplate(@Qualifier("redisCacheConnectionFactory") RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, UserSessionDTO> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(UserSessionDTO.class));

        return template;
    }

    // Redis connection factory for Redis Cache
    @Bean
    public RedisConnectionFactory redisCacheConnectionFactory() {
        String redisHost = System.getenv("SPRING_REDIS_HOST");
        String redisPort = System.getenv("SPRING_REDIS_PORT");
        return new LettuceConnectionFactory(redisHost, Integer.parseInt(redisPort));
    }

    // Redis connection factory for Order Cache
    @Bean
    public RedisConnectionFactory orderCacheConnectionFactory() {
        String redisHost = System.getenv("SPRING_REDIS_ORDER_HOST");
        String redisPort = System.getenv("SPRING_REDIS_ORDER_PORT");
        return new LettuceConnectionFactory(redisHost, Integer.parseInt(redisPort));
    }
}
