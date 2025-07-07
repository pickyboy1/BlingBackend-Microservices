package com.pickyboy.blingBackend.common.constants;

/**
 * Kafka 主题 (Topic) 常量类
 * 集中管理所有项目中使用的 Kafka Topic 名称
 */
public final class KafkaTopicConstants {

    /**
     * 私有构造函数，防止实例化
     */
    private KafkaTopicConstants() {}

    /**
     * 文章分数变更事件主题
     * 用于接收点赞、评论、收藏等原始事件
     */
    public static final String TOPIC_ARTICLE_SCORE_CHANGE = "modscore_topic";

    /**
     * 文章最终分数更新主题
     * 由 Kafka Streams 处理后，将最终的窗口分数写入此主题
     */
    public static final String TOPIC_UPDATE_ARTICLE_SCORE = "update_article_score_topic";

    // 未来可以添加更多的主题常量
    // public static final String TOPIC_USER_REGISTRATION = "user_registration_topic";
}