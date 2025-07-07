package com.pickyboy.blingBackend.common.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.pickyboy.blingBackend.common.utils.JwtUtil;

/**
 * JWT配置类
 * 只有在JWT启用时才会创建相应的Bean
 *
 * 🎯 条件说明：
 * - 需要配置yuque.jwt.enabled=true（默认false）
 *
 * 🎯 配置示例：
 * yuque:
 *   jwt:
 *     enabled: true
 *     secret-key: your-secret-key
 *     expire-hours: 24
 *
 * @author pickyboy
 */
@Configuration
@ConditionalOnProperty(prefix = "yuque.jwt", name = "enabled", havingValue = "true", matchIfMissing = false)
public class JwtConfig {

    /**
     * 注入JwtUtil工具类到Spring容器
     * 依赖于JwtProperties Bean，只有在JWT启用时才创建
     */
    @Bean
    public JwtUtil jwtUtil(JwtProperties jwtProperties) {
        return new JwtUtil(jwtProperties);
    }
}