package com.pickyboy.blingBackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pickyboy.blingBackend.client.UserServiceClient;
import com.pickyboy.blingBackend.common.response.Result;
import com.pickyboy.blingBackend.dto.user.LoginRequest;
import com.pickyboy.blingBackend.dto.user.RegisterRequest;
import com.pickyboy.blingBackend.vo.user.AuthResponse;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

/**
 * 客户端演示控制器
 * 演示如何在不同的微服务中使用Feign客户端调用其他服务
 *
 * 🎯 这是正确的Feign使用示例：
 * - 客户端服务（bling-client-demo）调用主服务（bling-main-service）
 * - 展示真正的跨服务通信
 *
 * @author pickyboy
 */
@Slf4j
@RestController
@RequestMapping("/client-demo")
public class ClientDemoController {

    @Autowired
    private UserServiceClient userServiceClient;

    /**
     * 通过Feign客户端调用主服务的用户注册接口
     * POST /client-demo/register
     *
     * 🎯 演示跨服务调用：
     * 客户端服务 -> Feign -> 网关 -> 主服务
     */
    @PostMapping("/register")
    public Result<String> callRegister(@Valid @RequestBody RegisterRequest registerRequest) {
        log.info("客户端服务调用主服务注册接口: registerType={}", registerRequest.getRegisterType());

        try {
            // 使用Feign客户端跨服务调用
            Result<Void> result = userServiceClient.register(registerRequest);

            log.info("Feign跨服务调用注册成功: {}", result.getMessage());
            return Result.success("客户端调用成功: " + result.getMessage());

        } catch (Exception e) {
            log.error("Feign跨服务调用注册失败", e);
            return Result.error("客户端调用失败: " + e.getMessage());
        }
    }

    /**
     * 通过Feign客户端调用主服务的用户登录接口
     * POST /client-demo/login
     *
     * 🎯 演示跨服务调用：
     * 客户端服务 -> Feign -> 网关 -> 主服务
     */
    @PostMapping("/login")
    public Result<AuthResponse> callLogin(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("客户端服务调用主服务登录接口: loginType={}", loginRequest.getLoginType());

        try {
            // 使用Feign客户端跨服务调用
            Result<AuthResponse> result = userServiceClient.login(loginRequest);

            log.info("Feign跨服务调用登录成功: {}", result.getMessage());

            // 可以对返回结果进行处理
            if (result.getCode() == 200 && result.getData() != null) {
                AuthResponse authResponse = result.getData();
                log.info("获取到JWT Token，用户: {}", authResponse.getUsername());
            }

            return result;

        } catch (Exception e) {
            log.error("Feign跨服务调用登录失败", e);
            return Result.error("客户端调用失败: " + e.getMessage());
        }
    }
}