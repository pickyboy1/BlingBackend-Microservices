package com.pickyboy.blingBackend.client;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.pickyboy.blingBackend.common.response.Result;
import com.pickyboy.blingBackend.config.FeignConfig;
import com.pickyboy.blingBackend.dto.user.LoginRequest;
import com.pickyboy.blingBackend.dto.user.RegisterRequest;
import com.pickyboy.blingBackend.vo.user.AuthResponse;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

/**
 * 用户服务Feign客户端
 * 用于调用主服务中的用户相关接口
 *
 * @author pickyboy
 */
@FeignClient(
    name = "bling-main-service",
    configuration = FeignConfig.class,
    fallbackFactory = UserServiceClient.UserServiceFallbackFactory.class
)
public interface UserServiceClient {

    /**
     * 用户注册
     *
     * @param registerRequest 注册请求
     * @return 注册结果
     */
    @PostMapping("/auth/register")
    Result<Void> register(@Valid @RequestBody RegisterRequest registerRequest);

    /**
     * 用户登录
     *
     * @param loginRequest 登录请求
     * @return 登录响应，包含JWT Token
     */
    @PostMapping("/auth/login")
    Result<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest);

    /**
     * Feign调用失败时的降级处理工厂
     */
    @Component
    @ConditionalOnClass(FallbackFactory.class)
    class UserServiceFallbackFactory implements FallbackFactory<UserServiceClient> {

        @Override
        public UserServiceClient create(Throwable cause) {
            return new UserServiceFallback(cause);
        }
    }

    /**
     * Feign调用失败时的降级处理实现
     */
    @Slf4j
    class UserServiceFallback implements UserServiceClient {

        private final Throwable cause;

        public UserServiceFallback(Throwable cause) {
            this.cause = cause;
        }

        @Override
        public Result<Void> register(RegisterRequest registerRequest) {
            log.error("用户注册服务调用失败，执行降级处理", cause);
            return Result.error("用户注册服务暂时不可用，请稍后重试");
        }

        @Override
        public Result<AuthResponse> login(LoginRequest loginRequest) {
            log.error("用户登录服务调用失败，执行降级处理", cause);
            return Result.error("用户登录服务暂时不可用，请稍后重试");
        }
    }
}