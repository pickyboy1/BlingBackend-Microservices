package com.pickyboy.blingBackend.common.config;

import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT配置属性类
 * 只有在启用JWT时才会创建此配置
 *
 * 🎯 主要功能：
 * 提供JWT相关的配置参数
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
 *     auto-refresh: true
 *
 * @author pickyboy
 */
@Data
@Component
@ConditionalOnProperty(prefix = "yuque.jwt", name = "enabled", havingValue = "true", matchIfMissing = false)
@ConfigurationProperties(prefix = "yuque.jwt")
public class JwtProperties {

    /**
     * 是否启用JWT功能
     */
    private boolean enabled = false;

    /**
     * JWT签名密钥
     */
    private String secretKey = "yuque-very-long-secret-key-for-jwt-2025";

    /**
     * Token过期时间（小时）
     */
    private Integer expireHours = 24;

    /**
     * Token刷新阈值（小时）
     * 当Token剩余时间少于此值时，可以刷新Token
     */
    private Integer refreshThresholdHours = 2;

    /**
     * Token前缀
     */
    private String tokenPrefix = "Bearer ";

    /**
     * Token请求头名称
     */
    private String headerName = "Authorization";

    /**
     * 是否启用Token自动刷新
     */
    private Boolean autoRefresh = true;
}