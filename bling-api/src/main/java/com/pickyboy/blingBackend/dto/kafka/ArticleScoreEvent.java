// 在 bling-api/src/main/java/com/pickyboy/blingBackend/dto/kafka/ArticleScoreEvent.java
package com.pickyboy.blingBackend.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleScoreEvent {
    private Long articleId;
    private Long userId;
    private EventType eventType;
    private Integer scoreChange; // 分数变化值，可正可负
    private java.time.LocalDateTime timestamp;

    // 便捷构造方法，自动使用枚举中定义的分数变化值
    public ArticleScoreEvent(Long articleId, Long userId, EventType eventType, java.time.LocalDateTime timestamp) {
        this.articleId = articleId;
        this.userId = userId;
        this.eventType = eventType;
        this.scoreChange = eventType.getScoreChange(); // 自动从枚举获取分数变化值
        this.timestamp = timestamp;
    }


    public enum EventType {
        // Define each event type and its associated score change
        VIEW(3),
        LIKE(10),
        UNLIKE(-10),
        FAVORITE(15),
        UNFAVORITE(-15),
        COMMENT(20),
        DELETE_COMMENT(-20);

        private final int scoreChange;

        // Constructor for the enum
        EventType(int scoreChange) {
            this.scoreChange = scoreChange;
        }

        // Public getter to access the score change value
        public int getScoreChange() {
            return this.scoreChange;
        }
    }
}