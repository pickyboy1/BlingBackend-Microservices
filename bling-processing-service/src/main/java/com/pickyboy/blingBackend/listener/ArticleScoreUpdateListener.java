package com.pickyboy.blingBackend.listener;

import com.pickyboy.blingBackend.common.constants.KafkaTopicConstants;
import com.pickyboy.blingBackend.common.constants.RedisKeyConstants;
import com.pickyboy.blingBackend.common.utils.RedisUtil;
import com.pickyboy.blingBackend.entity.Resources;
import com.pickyboy.blingBackend.mapper.ResourceStateMapper; // 【修正】引入新的 Mapper
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ArticleScoreUpdateListener {

    private final RedisUtil redisUtil;
    private final ResourceStateMapper resourceMapper; // 【修正】注入新的 Mapper

    @KafkaListener(
        topics = KafkaTopicConstants.TOPIC_UPDATE_ARTICLE_SCORE,
        groupId = "bling-processing-group",
        properties = {
            "value.deserializer=org.apache.kafka.common.serialization.IntegerDeserializer"
        }
    )
    public void handleScoreUpdate(ConsumerRecord<String, Integer> record) {
        String articleIdStr = record.key();
        Integer scoreIncrement = record.value();

        try {
            Long articleId = Long.parseLong(articleIdStr);
            log.info("消费到文章分数增量: Article ID={}, Score Increment={}", articleId, scoreIncrement);

            // 1. 查询文章是否存在且有效
            Resources resource = resourceMapper.selectById(articleId);
            if (resource == null || resource.getIsDeleted() || resource.getStatus() != 1) {
                log.warn("文章 {} 已下架或被删除，从热榜中移除。", articleId);
                redisUtil.zRemove(RedisKeyConstants.HOT_ARTICLE_ZSET_KEY, articleIdStr);
                return;
            }

            // 2. 获取 ZSet 当前大小
            Long zsetSize = redisUtil.zSize(RedisKeyConstants.HOT_ARTICLE_ZSET_KEY);
            log.debug("当前热榜大小: {}", zsetSize);

            // 3. 检查文章是否已在热榜中
            Double currentRedisScore = redisUtil.zScore(RedisKeyConstants.HOT_ARTICLE_ZSET_KEY, articleIdStr);
            boolean isInZSet = (currentRedisScore != null);

            if (isInZSet) {
                // 3.1 已在热榜中，直接增加分数
                Double newRedisScore = redisUtil.zIncrementScore(RedisKeyConstants.HOT_ARTICLE_ZSET_KEY, articleIdStr, scoreIncrement.doubleValue());
                log.info("更新热榜中文章分数: ArticleID={}, OldScore={}, NewScore={}", articleId, currentRedisScore, newRedisScore);

                // 更新数据库中的分数
                updateDatabaseScore(articleId, newRedisScore.intValue());

            } else if (zsetSize < 60) {
                // 3.2 不在热榜中且热榜未满，直接加入
                // 计算文章当前总分数（基于实际互动数据计算 + 本次增量）
                Integer currentCalculatedScore = calculateScoreFromResource(resource);
                Integer newTotalScore = currentCalculatedScore + scoreIncrement;

                redisUtil.zAdd(RedisKeyConstants.HOT_ARTICLE_ZSET_KEY, articleIdStr, newTotalScore.doubleValue());
                log.info("文章加入热榜: ArticleID={}, Score={}, 当前热榜大小: {}", articleId, newTotalScore, zsetSize + 1);

                // 更新数据库中的分数
                updateDatabaseScore(articleId, newTotalScore);

            } else {
                // 3.3 不在热榜中且热榜已满，需要与最低分数比较
                // 获取分数最低的文章（ZSet 中的第一个）
                var lowestScoreEntry = redisUtil.zRangeWithScores(RedisKeyConstants.HOT_ARTICLE_ZSET_KEY, 0, 0);

                if (!lowestScoreEntry.isEmpty()) {
                    var lowestEntry = lowestScoreEntry.iterator().next();
                    Double lowestScore = lowestEntry.getScore();
                    String lowestArticleId = (String) lowestEntry.getValue();

                    // 计算当前文章的总分数（基于实际互动数据计算）
                    Integer currentCalculatedScore = calculateScoreFromResource(resource);
                    Integer newTotalScore = currentCalculatedScore + scoreIncrement;

                    if (newTotalScore > lowestScore) {
                        // 新文章分数更高，替换最低分的文章
                        redisUtil.zRemove(RedisKeyConstants.HOT_ARTICLE_ZSET_KEY, lowestArticleId);
                        redisUtil.zAdd(RedisKeyConstants.HOT_ARTICLE_ZSET_KEY, articleIdStr, newTotalScore.doubleValue());

                        log.info("替换热榜最低分文章: 移除ArticleID={} (Score={}), 加入ArticleID={} (Score={})",
                                lowestArticleId, lowestScore, articleId, newTotalScore);

                        // 更新数据库中的分数
                        updateDatabaseScore(articleId, newTotalScore);
                    } else {
                        log.debug("文章分数不足以进入热榜: ArticleID={}, Score={}, 热榜最低分={}",
                                articleId, newTotalScore, lowestScore);

                        // 虽然不进入热榜，但仍需更新数据库分数
                        updateDatabaseScore(articleId, currentCalculatedScore + scoreIncrement);
                    }
                }
            }

        } catch (Exception e) {
            log.error("处理文章分数更新消息失败: Key={}, Value={}", articleIdStr, scoreIncrement, e);
        }
    }

    /**
     * 获取文章当前分数（优先使用数据库中的score，为null时才重新计算）
     * @param resource 文章资源对象
     * @return 当前分数
     */
    private Integer calculateScoreFromResource(Resources resource) {
        // 如果数据库中已有分数，直接使用
        if (resource.getScore() != null) {
            log.debug("使用数据库中的分数: ArticleID={}, Score={}", resource.getId(), resource.getScore());
            return resource.getScore();
        }

        // 只有当score为null时才重新计算
        int viewCount = resource.getViewCount() != null ? resource.getViewCount() : 0;
        int likeCount = resource.getLikeCount() != null ? resource.getLikeCount() : 0;
        int commentCount = resource.getCommentCount() != null ? resource.getCommentCount() : 0;
        int favoriteCount = resource.getFavoriteCount() != null ? resource.getFavoriteCount() : 0;

        // 使用与 ArticleScoreEvent 相同的权重计算分数
        int calculatedScore = (viewCount * 3) +       // 浏览得 3 分
                             (likeCount * 10) +       // 点赞得 10 分
                             (commentCount * 20) +    // 评论得 20 分
                             (favoriteCount * 15);    // 收藏得 15 分

        log.debug("重新计算文章分数: ArticleID={}, View={}, Like={}, Comment={}, Favorite={}, TotalScore={}",
                 resource.getId(), viewCount, likeCount, commentCount, favoriteCount, calculatedScore);

        return calculatedScore;
    }

    /**
     * 更新数据库中文章的分数
     * @param articleId 文章ID
     * @param newScore 新分数
     */
    private void updateDatabaseScore(Long articleId, Integer newScore) {
        try {
            Resources updateResource = new Resources();
            updateResource.setId(articleId);
            updateResource.setScore(newScore);

            int updateResult = resourceMapper.updateById(updateResource);
            if (updateResult > 0) {
                log.debug("更新数据库文章分数成功: ArticleID={}, NewScore={}", articleId, newScore);
            } else {
                log.warn("更新数据库文章分数失败: ArticleID={}, NewScore={}", articleId, newScore);
            }
        } catch (Exception e) {
            log.error("更新数据库文章分数异常: ArticleID={}, NewScore={}", articleId, newScore, e);
        }
    }
}
