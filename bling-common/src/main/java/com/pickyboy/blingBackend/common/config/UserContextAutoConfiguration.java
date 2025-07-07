package com.pickyboy.blingBackend.common.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.DispatcherServlet;

import com.pickyboy.blingBackend.common.filter.UserContextFilter;

import lombok.extern.slf4j.Slf4j;

/**
 * 用户上下文自动配置类
 * 根据配置决定是否启用用户上下文功能
 *
 * 🎯 主要功能：
 * 1. 根据配置条件注册UserContextFilter过滤器
 * 2. 配置过滤器执行顺序
 * 3. 支持debug模式配置
 *
 * 🎯 配置项：
 * - common.config.filter.enabled: 是否启用用户上下文过滤器（默认true）
 * - common.config.filter.debug: 是否开启debug日志（默认false）
 *
 * 🎯 使用方式：
 * 在需要用户上下文的微服务的配置类中使用 @Import(UserContextAutoConfiguration.class)
 *
 * @author pickyboy
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(UserContextProperties.class)
@ConditionalOnClass(DispatcherServlet.class) // 只有在Servlet环境中才加载
public class UserContextAutoConfiguration {

    /**
     * 注册UserContextFilter过滤器
     * 仅在配置启用时才注册
     */
    @Bean
    @ConditionalOnProperty(
        prefix = "common.config.filter",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true // 默认启用
    )
    public FilterRegistrationBean<UserContextFilter> userContextFilterRegistration(
            UserContextProperties properties) {

        log.info("配置UserContextFilter用户上下文过滤器");
        log.info("过滤器配置 - 启用: {}, Debug模式: {}",
            properties.isEnabled(), properties.isDebug());

        FilterRegistrationBean<UserContextFilter> registration = new FilterRegistrationBean<>();

        // 创建过滤器实例，传入配置
        UserContextFilter filter = new UserContextFilter(properties);
        registration.setFilter(filter);

        // 设置过滤器名称
        registration.setName("userContextFilter");

        // 设置URL匹配模式 - 拦截所有请求
        registration.addUrlPatterns("/*");

        // 设置执行顺序，确保在其他业务过滤器之前执行
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);

        log.info("UserContextFilter注册完成，执行顺序: {}", registration.getOrder());
        log.info("用户上下文功能已启用，业务代码可通过 CurrentHolder.getCurrentUser() 获取用户信息");

        return registration;
    }

}