package com.pickyboy.blingBackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;


import lombok.extern.slf4j.Slf4j;

/**
 * 主服务配置类
 * 负责配置主服务所需的所有组件
 *
 * 🎯 主要功能：
 * 1. 引入用户上下文配置 - 用于从网关获取用户信息
 * 2. 其他主服务特有的配置
 *
 * 🎯 用户上下文功能：
 * - 自动从网关传递的请求头中提取用户信息
 * - 设置到当前线程上下文中
 * - 业务代码可通过 UserContextHelper.getCurrentUserId() 获取用户ID
 * - 业务代码可通过 UserContextHelper.getCurrentUsername() 获取用户名
 *
 * @author pickyboy
 */
@Slf4j
@Configuration
@Import({
})
public class MainServiceConfig {
    // 可以在这里添加其他主服务特有的Bean配置
}