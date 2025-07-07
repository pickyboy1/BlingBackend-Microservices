package com.pickyboy.blingBackend.common.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.pickyboy.blingBackend.common.utils.RedisUtil;

/**
 * Redis配置类
 * 只有在Redis相关类存在且启用时才会创建相应的Bean
 *
 * 🎯 条件说明：
 * - 需要RedisTemplate类在classpath中
 * - 需要配置spring.data.redis.enabled=true（默认true）
 *
 * 🎯 配置示例：
 * spring:
 *   data:
 *     redis:
 *       enabled: true
 *       host: localhost
 *       port: 6379
 *       database: 0
 *
 * @author pickyboy
 */
@Configuration
@ConditionalOnClass({RedisTemplate.class, RedisConnectionFactory.class})
@ConditionalOnProperty(prefix = "spring.data.redis", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RedisConfig {

    /**
     * 自定义RedisTemplate配置
     * 设置合适的序列化器，避免乱码问题
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 使用StringRedisSerializer来序列化和反序列化redis的key值
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // 使用GenericJackson2JsonRedisSerializer来序列化和反序列化redis的value值
        // 它能将Java对象转为JSON格式存储，并从JSON转回Java对象
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * 注入RedisUtil工具类到Spring容器
     * 依赖于RedisTemplate Bean，只有在RedisTemplate存在时才创建
     */
    @Bean
    public RedisUtil redisUtil(RedisTemplate<String, Object> redisTemplate) {
        return new RedisUtil(redisTemplate);
    }
}