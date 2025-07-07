package com.pickyboy.blingBackend.common.config;

import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 认证过滤器配置属性
 * 只有在启用认证功能时才会创建此配置
 *
 * 🎯 主要功能：
 * 提供认证相关的配置参数，如排除路径等
 *
 * 🎯 条件说明：
 * - 需要配置pickyboy.auth.enabled=true（默认false）
 *
 * 🎯 配置示例：
 * pickyboy:
 *   auth:
 *     enabled: true
 *     exclude-paths:
 *       - /api/auth/login
 *       - /api/auth/register
 *       - /api/public/**
 *
 * @author pickyboy
 */
@Data
@Component
@ConditionalOnProperty(prefix = "pickyboy.auth", name = "enabled", havingValue = "true", matchIfMissing = false)
@ConfigurationProperties(prefix = "pickyboy.auth")
public class AuthProperties {

    /**
     * 是否启用认证功能
     */
    private boolean enabled = false;

    /**
     * 排除路径列表
     * 这些路径不需要进行认证检查
     */
    private List<String> excludePaths;
}