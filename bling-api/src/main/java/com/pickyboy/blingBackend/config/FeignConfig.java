package com.pickyboy.blingBackend.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.DispatcherServlet;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Feign配置类
 * 提供Feign客户端的全局配置，包括请求拦截器
 *
 * @author pickyboy
 */
@Slf4j
@Configuration
@ConditionalOnClass(FallbackFactory.class)
public class FeignConfig {

    /**
     * Feign请求拦截器
     * 在Feign调用时自动传递认证信息和其他必要的请求头
     */
    @Bean
    public RequestInterceptor feignRequestInterceptor() {
        return new FeignRequestInterceptor();
    }

    /**
     * Feign请求拦截器实现类
     */
    @Slf4j
    public static class FeignRequestInterceptor implements RequestInterceptor {

        @Override
        public void apply(RequestTemplate template) {
            try {
                // 获取当前请求的上下文
                ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();

                    // 传递Authorization头（JWT Token）
                    String authorization = request.getHeader("Authorization");
                    if (authorization != null && !authorization.isEmpty()) {
                        template.header("Authorization", authorization);
                        log.debug("Feign调用传递Authorization头: {}",
                            authorization.substring(0, Math.min(20, authorization.length())) + "...");
                    }

                    // 传递网关传递的用户信息头
                    String userId = request.getHeader("X-User-Id");
                    String username = request.getHeader("X-Username");

                    if (userId != null && !userId.isEmpty()) {
                        template.header("X-User-Id", userId);
                        log.debug("Feign调用传递用户ID: {}", userId);
                    }

                    if (username != null && !username.isEmpty()) {
                        template.header("X-Username", username);
                        log.debug("Feign调用传递用户名: {}", username);
                    }

                    // 传递请求ID用于链路追踪
                    String requestId = request.getHeader("X-Request-Id");
                    if (requestId != null && !requestId.isEmpty()) {
                        template.header("X-Request-Id", requestId);
                    } else {
                        // 如果没有请求ID，生成一个
                        String generatedRequestId = generateRequestId();
                        template.header("X-Request-Id", generatedRequestId);
                        log.debug("Feign调用生成请求ID: {}", generatedRequestId);
                    }

                    // 传递Content-Type
                    String contentType = request.getHeader("Content-Type");
                    if (contentType != null && !contentType.isEmpty()) {
                        template.header("Content-Type", contentType);
                    }

                } else {
                    log.debug("Feign调用时无法获取当前请求上下文，跳过头部传递");
                }

            } catch (Exception e) {
                log.warn("Feign请求拦截器处理异常: {}", e.getMessage(), e);
            }
        }

        /**
         * 生成请求ID用于链路追踪
         */
        private String generateRequestId() {
            return "feign-" + System.currentTimeMillis() + "-" +
                   Thread.currentThread().getId();
        }
    }
}