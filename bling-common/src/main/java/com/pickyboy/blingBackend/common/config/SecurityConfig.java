package com.pickyboy.blingBackend.common.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Security配置类
 * 只有在Spring Security相关类存在且启用时才会创建相应的Bean
 *
 * 🎯 主要功能：
 * 提供密码加密器等Security相关的Bean
 *
 * 🎯 条件说明：
 * - 需要PasswordEncoder类在classpath中
 * - 需要配置spring.security.enabled=true（默认true，如果有security依赖）
 *
 * 🎯 配置示例：
 * spring:
 *   security:
 *     enabled: true
 *
 * @author pickyboy
 */
@Configuration
@ConditionalOnClass(PasswordEncoder.class)
@ConditionalOnProperty(prefix = "spring.security", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SecurityConfig {

    /**
     * 密码加密器Bean
     * 使用BCrypt加密算法，安全性高且不可逆
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}