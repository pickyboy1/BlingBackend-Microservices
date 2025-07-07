package com.pickyboy.blingBackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@MapperScan("com.pickyboy.blingBackend.mapper")
@EnableAsync
@EnableFeignClients(basePackages = "com.pickyboy.blingBackend.client")
public class BlingBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlingBackendApplication.class, args);
    }

}
