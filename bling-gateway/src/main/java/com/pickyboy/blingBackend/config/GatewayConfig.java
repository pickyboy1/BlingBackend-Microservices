package com.pickyboy.blingBackend.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 网关配置类
 * 负责扫描和配置网关所需的组件
 *
 * 🎯 主要功能：
 * 1. 扫描通用模块的组件（JWT工具类、配置类等）
 * 2. 扫描网关特有的组件（过滤器等）
 *
 * @author pickyboy
 */
@Configuration
@ComponentScan()
public class GatewayConfig {
    // 自动扫描即可，无需额外Bean配置
}