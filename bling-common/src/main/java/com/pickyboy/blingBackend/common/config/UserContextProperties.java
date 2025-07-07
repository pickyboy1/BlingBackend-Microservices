package com.pickyboy.blingBackend.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * 用户上下文配置属性
 *
 * 🎯 配置项说明：
 * - enabled: 是否启用用户上下文过滤器，默认true
 * - debug: 是否开启debug日志模式，默认false
 *
 * 🎯 配置示例：
 * common:
 *   config:
 *     filter:
 *       enabled: true
 *       debug: true
 *
 * @author pickyboy
 */
@Data
@ConfigurationProperties(prefix = "common.config.filter")
public class UserContextProperties {

    /**
     * 是否启用用户上下文过滤器
     * 默认: true
     */
    private boolean enabled = true;

    /**
     * 是否开启debug日志模式
     * 在debug模式下，会输出详细的用户信息提取和上下文设置日志
     * 默认: false
     */
    private boolean debug = false;

}