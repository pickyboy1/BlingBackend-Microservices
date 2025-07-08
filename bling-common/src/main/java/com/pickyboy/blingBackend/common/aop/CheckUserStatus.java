package com.pickyboy.blingBackend.common.aop;

import com.pickyboy.blingBackend.common.constants.UserStatusConstants;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解，用于标记需要检查用户状态的方法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckUserStatus {
    /**
     * 需要检查的权限类型，默认为评论权限
     */
    int requiredStatus() default UserStatusConstants.CANNOT_COMMENT;
}
