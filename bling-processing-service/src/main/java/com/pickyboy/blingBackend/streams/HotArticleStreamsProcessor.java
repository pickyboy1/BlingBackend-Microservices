// 在 com.pickyboy.blingBackend.streams.HotArticleStreamsProcessor.java
package com.pickyboy.blingBackend.streams;

import com.pickyboy.blingBackend.common.constants.KafkaTopicConstants;
import com.pickyboy.blingBackend.dto.kafka.ArticleScoreEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonSerde;
import java.time.Duration;

@Configuration
@Slf4j
public class HotArticleStreamsProcessor {

    @Autowired
    public void process(StreamsBuilder builder) {
        // 使用 JsonSerde 来自动处理我们自定义的 DTO
        JsonSerde<ArticleScoreEvent> eventSerde = new JsonSerde<>(ArticleScoreEvent.class);

        // 1. 从原始事件 Topic (`modscore_topic`) 创建流
        KStream<String, ArticleScoreEvent> stream = builder.stream(
            KafkaTopicConstants.TOPIC_ARTICLE_SCORE_CHANGE,
            Consumed.with(Serdes.String(), eventSerde)
        );

        // 2. 定义处理拓扑
        stream
            .peek((key, value) -> log.info("收到事件: 文章ID={}, 事件类型={}, 分数变化={}",
                key, value.getEventType(),
                value.getScoreChange() != null ? value.getScoreChange() : value.getEventType().getScoreChange()))
            // 按文章ID (即消息的Key) 进行分组
            .groupByKey(Grouped.with(Serdes.String(), eventSerde))
                // 【修正】定义一个24小时的滚动窗口，并使用正确的API
                .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofMinutes(30)))
            // 聚合计算窗口内的总分
            .aggregate(
                () -> 0, // 初始化聚合器值为 0
                (key, value, aggregate) -> {
                    int scoreToAdd = value.getScoreChange() != null ? value.getScoreChange() : value.getEventType().getScoreChange();
                    log.debug("Adding score {} for article {} (current aggregate: {})", scoreToAdd, key, aggregate);
                    return aggregate + scoreToAdd;
                }, // 核心逻辑：累加分数，优先使用scoreChange字段，fallback到枚举值
                Materialized.with(Serdes.String(), Serdes.Integer()) // 物化状态，以便容错
            )
            // 将窗口化的流 (KTable) 转回普通的 KStream
            .toStream()
            .map((windowedKey, value) -> {
                // 我们只关心文章ID和最终的分数
                log.info("30分钟窗口 [{} - {}] 文章 {} 的聚合分数: {}",
                        windowedKey.window().start(), windowedKey.window().end(), windowedKey.key(), value);
                return new KeyValue<>(windowedKey.key(), value);
            })
            // 3. 将聚合结果发送到新的 Topic (`update_article_score_topic`)
            .to(KafkaTopicConstants.TOPIC_UPDATE_ARTICLE_SCORE, Produced.with(Serdes.String(), Serdes.Integer()));
    }
}
