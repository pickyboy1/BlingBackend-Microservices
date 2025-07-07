package com.pickyboy.blingBackend.common.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus自动填充处理器
 * 只有在MyBatis-Plus相关类存在且启用时才会创建
 *
 * 🎯 主要功能：
 * 用于自动设置created_at和updated_at字段
 *
 * 🎯 条件说明：
 * - 需要MetaObjectHandler类在classpath中
 * - 需要配置mybatis-plus.auto-fill.enabled=true（默认true）
 *
 * 🎯 使用说明：
 * 实体类需要使用@TableField(fill = FieldFill.INSERT)或@TableField(fill = FieldFill.INSERT_UPDATE)注解
 * 执行对应update或insert操作时,会自动填充created_at和updated_at字段
 *
 * 🎯 配置示例：
 * mybatis-plus:
 *   auto-fill:
 *     enabled: true
 *
 * @author pickyboy
 */

/*
注意:实体类需要使用@TableField(fill = FieldFill.INSERT)或@TableField(fill = FieldFill.INSERT_UPDATE)注解
执行对应update或insert操作时,会自动填充created_at和updated_at字段

 */
@Slf4j
@Component
@ConditionalOnClass(MetaObjectHandler.class)
@ConditionalOnProperty(prefix = "mybatis-plus.auto-fill", name = "enabled", havingValue = "true", matchIfMissing = true)
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("开始插入填充... 表名: {}", metaObject.getOriginalObject().getClass().getSimpleName());

        // 插入时自动填充创建时间和更新时间
        LocalDateTime now = LocalDateTime.now();

        // 检查字段是否存在
        if (metaObject.hasGetter("createdAt")) {
            this.setFieldValByName("createdAt", now, metaObject);
            log.info("设置 createdAt: {}", now);
        }

        if (metaObject.hasGetter("updatedAt")) {
            this.setFieldValByName("updatedAt", now, metaObject);
            log.info("设置 updatedAt: {}", now);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("开始更新填充... 表名: {}", metaObject.getOriginalObject().getClass().getSimpleName());

        LocalDateTime now = LocalDateTime.now();

        // 检查字段是否存在
        if (metaObject.hasGetter("updatedAt")) {
            // 使用非严格模式，强制设置字段值
            this.setFieldValByName("updatedAt", now, metaObject);
            log.info("设置 updatedAt: {}", now);
        } else {
            log.warn("updatedAt 字段不存在于 {}", metaObject.getOriginalObject().getClass().getSimpleName());
        }
    }
}