// /bling-main-service/src/main/java/com/pickyboy/blingBackend/aop/UserStatusAspect.java
package com.pickyboy.blingBackend.aop;

import com.pickyboy.blingBackend.common.aop.CheckUserStatus;
import com.pickyboy.blingBackend.common.constants.UserStatusConstants;
import com.pickyboy.blingBackend.common.utils.CurrentHolder;
import com.pickyboy.blingBackend.common.exception.BusinessException;
import com.pickyboy.blingBackend.common.exception.ErrorCode;
import com.pickyboy.blingBackend.entity.Users;
import com.pickyboy.blingBackend.mapper.UsersMapper; // 引入 UsersMapper
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Aspect
@Component
@Slf4j
public class UserStatusAspect {

    // 移除 Feign 客户端，改为直接注入 Mapper
    @Autowired
    private UsersMapper usersMapper;

    /**
     * 在执行被 @CheckUserStatus 注解标记的方法前，执行此校验
     * @param checkUserStatus 注解实例，可以从中获取参数
     */
    @Before("@annotation(checkUserStatus)")
    public void checkStatus(CheckUserStatus checkUserStatus) {
        Long userId = CurrentHolder.getCurrentUserId();
        if (userId == null) {
            // 如果是需要登录的接口，前面的认证过滤器应该已经拦截了
            // 这里作为双重保险
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // 通过 Mapper 直接从数据库获取最新的用户信息
        Users user = usersMapper.findUserStatusById(userId);
        if (user == null) {
            // 在微服务场景下，用户可能在用户中心被删除，但主服务数据未同步，因此抛出用户不存在异常更合适
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        Integer status = user.getStatus();
        if (status == null || Objects.equals(status, UserStatusConstants.NORMAL)) {
            return; // 状态正常，直接放行
        }

        // 检查封禁是否已到期
       if (user.getBlockEndtime() != null && LocalDateTime.now().isAfter(user.getBlockEndtime())) {
            // 封禁已过，自动解封 (在真实场景中，可以由定时任务处理，这里做即时处理)
            log.info("用户ID {} 的封禁已到期，自动解封。", userId);
            user.setStatus(UserStatusConstants.NORMAL);
            usersMapper.updateById(user);
            return; // 解封后放行
        }

        // 根据注解参数和用户状态判断是否拦截
        int requiredStatus = checkUserStatus.requiredStatus();
        if (Objects.equals(status, requiredStatus)) {
            throwBlockedException(user);
        }

        // 特殊处理：如果用户被限制登录，那么任何需要登录的操作都应被禁止
        if (Objects.equals(status, UserStatusConstants.CANNOT_LOGIN)) {
            throwBlockedException(user);
        }
    }

    private void throwBlockedException(Users user) {
        String message;
        switch (user.getStatus()) {
            case UserStatusConstants.CANNOT_LOGIN:
                message = "您的账号已被限制登录";
                break;
            case UserStatusConstants.CANNOT_COMMENT:
                message = "您已被禁言，无法执行此操作";
                break;
            default:
                message = "您的账号状态异常，操作受限";
                break;
        }

        String reason = user.getBlockReason();
        if (reason != null && !reason.isEmpty()) {
            message += "，原因：" + reason;
        }

        LocalDateTime endTime = user.getBlockEndtime();
        if (endTime != null) {
            message += "。解封时间：" + endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } else {
            message += "。此为永久封禁";
        }

        throw new BusinessException(ErrorCode.FORBIDDEN_OPERATION, message);
    }
}
