package com.pickyboy.blingBackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 布灵客户端演示服务
 * 用于演示如何使用Feign客户端调用其他微服务
 *
 * @author pickyboy
 */
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.pickyboy.blingBackend.client")
public class BlingClientDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlingClientDemoApplication.class, args);
    }
}