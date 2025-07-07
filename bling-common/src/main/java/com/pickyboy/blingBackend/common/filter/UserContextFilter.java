package com.pickyboy.blingBackend.common.filter;

import java.io.IOException;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.pickyboy.blingBackend.common.config.UserContextProperties;
import com.pickyboy.blingBackend.common.context.UserContext;
import com.pickyboy.blingBackend.common.utils.CurrentHolder;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * 用户上下文过滤器
 * 负责从网关传递的请求头中提取用户信息，并设置到当前线程上下文
 *
 * 🎯 这个过滤器替换了原来的JWT认证过滤器
 * 因为认证逻辑已经转移到网关层，主服务只需要提取用户信息即可
 *
 * 🎯 主要功能：
 * 1. 从请求头中提取网关传递的用户信息
 * 2. 设置到当前线程上下文，供业务逻辑使用
 * 3. 请求结束后清理线程上下文
 * 4. 支持配置控制debug日志输出
 *
 * 🎯 配置支持：
 * - common.config.filter.enabled: 控制过滤器是否启用
 * - common.config.filter.debug: 控制是否输出详细debug日志
 *
 * @author pickyboy
 */
@Slf4j
@Order(1) // 确保在其他过滤器之前执行
public class UserContextFilter implements Filter {

    // 网关传递用户信息的请求头名称
    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USERNAME = "X-Username";
    private static final String HEADER_USER_ROLE = "X-User-Role";

    /**
     * 配置属性
     */
    private final UserContextProperties properties;

    /**
     * 默认构造函数 - 用于Spring组件扫描
     */
    public UserContextFilter() {
        this.properties = new UserContextProperties(); // 使用默认配置
    }

    /**
     * 带配置的构造函数 - 用于自动配置
     */
    public UserContextFilter(UserContextProperties properties) {
        this.properties = properties != null ? properties : new UserContextProperties();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String requestURI = httpRequest.getRequestURI();
        String method = httpRequest.getMethod();

        // 根据debug配置决定是否输出详细日志
        if (properties.isDebug()) {
            log.debug("用户上下文过滤器处理请求: {} {}", method, requestURI);
        }

        try {
            // 从网关传递的请求头中提取用户信息
            boolean extracted = extractAndSetUserContext(httpRequest);
            if (extracted && properties.isDebug()) {
                UserContext userContext = CurrentHolder.getCurrentUser();
                if (log.isDebugEnabled()) {
                    log.debug("成功提取用户信息: 用户ID={}, 用户名={}",
                        userContext.getUserId(), userContext.getUsername());
                }
            } else if (properties.isDebug()) {
                if (log.isDebugEnabled()) {
                    log.debug("未提取到用户信息，可能是公开接口或内部调用");
                }
            }

            // 继续处理请求
            chain.doFilter(request, response);

        } catch (Exception e) {
            log.error("用户上下文过滤器处理异常", e);
            // 即使出现异常，也继续处理请求，避免影响正常业务
            chain.doFilter(request, response);
        } finally {
            // 清理线程上下文
            CurrentHolder.remove();
            if (properties.isDebug()) {
                log.debug("已清理用户上下文");
            }
        }
    }

    /**
     * 从网关传递的请求头中提取用户信息，并设置到当前线程上下文
     *
     * @param request HTTP请求对象
     * @return 是否成功提取用户信息
     */
    private boolean extractAndSetUserContext(HttpServletRequest request) {
        try {
            String userIdStr = request.getHeader(HEADER_USER_ID);
            String username = request.getHeader(HEADER_USERNAME);

            if (!StringUtils.hasText(userIdStr) || !StringUtils.hasText(username)) {
                if (properties.isDebug()) {
                    log.debug("请求头中缺少用户信息，可能是公开接口");
                }
                return false;
            }

            Long userId = Long.parseLong(userIdStr);
            UserContext userContext = new UserContext();
            userContext.setUserId(userId);
            userContext.setUsername(username);

            // 可选：提取用户角色
            String userRole = request.getHeader(HEADER_USER_ROLE);
            if (StringUtils.hasText(userRole)) {
                // 如果UserContext有角色字段，可以在这里设置
                // userContext.setRole(userRole);
                if (properties.isDebug()) {
                    log.debug("提取到用户角色: {}", userRole);
                }
            }

            CurrentHolder.setCurrentUser(userContext);
            if (properties.isDebug()) {
                log.debug("成功提取并设置用户信息: 用户ID={}, 用户名={}", userId, username);
            }
            return true;

        } catch (NumberFormatException e) {
            log.warn("用户ID格式错误: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("提取用户信息失败", e);
            return false;
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("用户上下文过滤器初始化 - Debug模式: {}", properties.isDebug());
        log.info("注意：此过滤器替代了JWT认证过滤器，认证逻辑已转移到网关层");
    }

    @Override
    public void destroy() {
        log.info("用户上下文过滤器销毁");
    }
}