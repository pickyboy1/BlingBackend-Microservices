package com.pickyboy.blingBackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaProducerService {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    public void sendMessage(String topic, String key,Object message) {
        try {
            log.info("向 Topic [{}] 发送消息 (Key: {}): {}", topic, key, message);
            kafkaTemplate.send(topic, key, message);
        } catch (Exception e) {
            log.error("发送 Kafka 消息失败: Topic={}, Key={}", topic, key, e);
        }
    }
}
