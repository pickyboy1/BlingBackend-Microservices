package com.pickyboy.blingBackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;

@SpringBootApplication
@MapperScan("com.pickyboy.blingBackend.mapper") // 依然需要扫描 Mapper
@EnableKafka          // 启用 Spring for Kafka 功能 (如 @KafkaListener)
@EnableKafkaStreams   // 启用 Kafka Streams 功能
public class BlingProcessingApplication {
    public static void main(String[] args) {
        SpringApplication.run(BlingProcessingApplication.class, args);
    }
}
