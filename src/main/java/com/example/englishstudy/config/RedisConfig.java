package com.example.englishstudy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    /**
     * 配置 RedisTemplate
     * @param redisConnectionFactory Redis 连接工厂
     * @return 配置好的 RedisTemplate
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        // 设置 Redis 连接工厂
        template.setConnectionFactory(redisConnectionFactory);

        // 设置键的序列化器为 StringRedisSerializer，方便在 Redis 中以字符串形式存储键
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // 设置值的序列化器为 GenericJackson2JsonRedisSerializer，将对象序列化为 JSON 字符串存储在 Redis 中
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        // 初始化 RedisTemplate
        template.afterPropertiesSet();
        return template;
    }
}