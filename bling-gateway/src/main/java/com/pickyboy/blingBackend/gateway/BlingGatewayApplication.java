package com.pickyboy.blingBackend.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 布灵网关服务启动类
 *
 * 作为整个微服务架构的统一入口，负责：
 * 1. 请求路由和转发
 * 2. 服务发现和负载均衡
 * 3. 统一的跨域处理
 * 4. 可扩展的过滤器链（认证、限流等）
 *
 * @author pickyboy
 */
@SpringBootApplication
@EnableDiscoveryClient
public class BlingGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlingGatewayApplication.class, args);
    }
}