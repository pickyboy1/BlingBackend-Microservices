package com.pickyboy.blingBackend.filter;

import com.pickyboy.blingBackend.common.response.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pickyboy.blingBackend.common.config.AuthProperties;
import com.pickyboy.blingBackend.common.context.UserContext;
import com.pickyboy.blingBackend.common.exception.JwtException;

import com.pickyboy.blingBackend.common.utils.JwtUtil;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 网关全局认证过滤器
 * 负责在网关层面进行JWT认证，并将用户信息传递给下游服务
 *
 * 🎯 主要功能：
 * 1. 验证JWT Token
 * 2. 解析用户信息
 * 3. 将用户信息添加到请求头
 * 4. 处理Token刷新
 * 5. 排除无需认证的路径
 *
 * @author pickyboy
 */
@Slf4j
@Component
public class GatewayAuthenticationFilter implements GlobalFilter, Ordered {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthProperties authProperties;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * 过滤器执行顺序，数值越小优先级越高
     * 认证过滤器需要在路由过滤器之前执行
     */
    @Override
    public int getOrder() {
        return -100; // 高优先级，确保在路由前执行
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        String path = request.getURI().getPath();
        String method = request.getMethod().name();

        log.debug("网关认证过滤器处理请求: {} {}", method, path);

        try {
            // 🔍 步骤1: 检查是否为排除路径
            if (isExcludedPath(path)) {
                log.debug("路径 {} 在排除列表中，跳过认证", path);
                return chain.filter(exchange);
            }

            // 🔍 步骤2: 提取Token
            String token = extractToken(request);
            if (!StringUtils.hasText(token)) {
                log.warn("请求 {} 缺少Token", path);
                return writeErrorResponse(response, HttpStatus.UNAUTHORIZED, "请先登录");
            }

            // 🔍 步骤3: 验证Token并解析用户信息
            UserContext userContext = jwtUtil.getUserContext(token);
            log.debug("Token验证成功，用户: {}", userContext.getUsername());

            // 🔍 步骤4: 检查Token是否需要刷新
            String refreshedToken = jwtUtil.refreshTokenIfNeeded(token);

            // 🔍 步骤5: 构建新的请求，添加用户信息到请求头
            ServerHttpRequest modifiedRequest = buildAuthenticatedRequest(request, userContext);
            ServerWebExchange modifiedExchange = exchange.mutate().request(modifiedRequest).build();

            // 🔍 步骤6: 如果Token被刷新，添加响应头
            // todo: 不用重构exchange?
            if (!token.equals(refreshedToken)) {
                log.info("Token已刷新，用户: {}", userContext.getUsername());
                response.getHeaders().add("New-Token", refreshedToken);
            }

            // 🔍 步骤7: 继续处理请求
            return chain.filter(modifiedExchange);

        } catch (JwtException.TokenExpiredException e) {
            log.warn("Token已过期: {}", e.getMessage());
            return writeErrorResponse(response, HttpStatus.UNAUTHORIZED, "登录已过期，请重新登录");
        } catch (JwtException e) {
            log.warn("Token验证失败: {}", e.getMessage());
            return writeErrorResponse(response, HttpStatus.UNAUTHORIZED, "认证失败，请重新登录");
        } catch (Exception e) {
            log.error("网关认证过滤器处理异常", e);
            return writeErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, "服务器内部错误");
        }
    }

    /**
     * 检查当前请求路径是否在认证排除列表中
     *
     * @param path 请求路径
     * @return 是否为排除路径
     */
    private boolean isExcludedPath(String path) {
        List<String> excludePaths = authProperties.getExcludePaths();
        if (excludePaths == null || excludePaths.isEmpty()) {
            return false;
        }

        for (String excludePath : excludePaths) {
            if (pathMatcher.match(excludePath, path)) {
                log.debug("路径 {} 匹配排除规则 {}", path, excludePath);
                return true;
            }
        }
        return false;
    }

    /**
     * 从请求头中提取JWT Token
     *
     * @param request HTTP请求
     * @return JWT Token字符串
     */
    private String extractToken(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (!StringUtils.hasText(authHeader)) {
            return null;
        }

        if (authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7); // 移除"Bearer "前缀
        }

        return null;
    }

    /**
     * 将用户信息添加到请求头中，传递给下游服务
     *
     * @param request 原始请求
     * @param userContext 用户上下文信息
     * @return 包含用户信息的新请求
     */
    private ServerHttpRequest buildAuthenticatedRequest(ServerHttpRequest request, UserContext userContext) {
        return request.mutate()
            .header("X-User-Id", userContext.getUserId().toString())
            .header("X-Username", userContext.getUsername())
            .build();
    }

    /**
     * 向客户端返回认证错误信息
     *
     * @param response HTTP响应
     * @param status HTTP状态码
     * @param message 错误消息
     * @return 响应处理结果
     */
    private Mono<Void> writeErrorResponse(ServerHttpResponse response, HttpStatus status, String message) {
        response.setStatusCode(status);
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");

        try {
            Result<Object> errorResult = Result.error(message);
            String jsonResponse = objectMapper.writeValueAsString(errorResult);
            DataBuffer buffer = response.bufferFactory().wrap(jsonResponse.getBytes(StandardCharsets.UTF_8));
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("序列化错误响应失败", e);
            return response.setComplete();
        }
    }
}