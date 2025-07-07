package com.pickyboy.blingBackend.common.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus配置类
 * 只有在MyBatis-Plus相关类存在且启用时才会创建相应的Bean
 *
 * 🎯 主要功能：
 * 配置MyBatis-Plus分页插件和其他增强功能
 *
 * 🎯 条件说明：
 * - 需要MybatisPlusInterceptor类在classpath中
 * - 需要配置mybatis-plus.enabled=true（默认true）
 *
 * 🎯 配置示例：
 * mybatis-plus:
 *   enabled: true
 *   configuration:
 *     map-underscore-to-camel-case: true
 *     log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
 *
 * @author pickyboy
 */
@Configuration
@ConditionalOnClass(MybatisPlusInterceptor.class)
@ConditionalOnProperty(prefix = "mybatis-plus", name = "enabled", havingValue = "true", matchIfMissing = true)
public class MybatisPlusConfig {

    /**
     * MyBatis-Plus拦截器配置
     * 主要用于分页功能
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 添加分页插件，指定数据库类型为MySQL
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}