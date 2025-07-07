package com.pickyboy.blingBackend.tasks;

import com.pickyboy.blingBackend.common.constants.RedisKeyConstants;
import com.pickyboy.blingBackend.common.utils.RedisUtil;
import com.pickyboy.blingBackend.mapper.ResourceStateMapper;
import com.pickyboy.blingBackend.vo.cache.HotArticleVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class HotArticleSyncTask {

    private final ResourceStateMapper resourceMapper;
    private final RedisUtil redisUtil;

    // 定期刷新redis中文章,只保存近七天的60篇高热度文章
    @Scheduled(cron = "0 0 * * * ?")
    public void syncHotArticles() {
        log.info("开始执行【定时任务】：同步热点文章...");

        // 使用新的 Mapper 方法拉取近7天的热门文章，按score排序，限制60篇
        List<HotArticleVO> hotArticles = resourceMapper.selectHotArticlesWithAuthor(60);

        if (hotArticles.isEmpty()) {
            log.info("【定时任务】：没有需要同步的热点文章。");
            return;
        }

        // 将热门文章ID和分值存储到 Redis ZSet 中
        String tempKey = RedisKeyConstants.HOT_ARTICLE_ZSET_KEY + "_temp";
        redisUtil.delete(tempKey);

        // 构建分数映射
        Map<String, Double> scoreMap = new HashMap<>();
        for (HotArticleVO article : hotArticles) {
            // 从数据库的 score 字段获取分数，如果为空则计算默认分数
            double score = article.getScore() != null ? article.getScore().doubleValue() :
                          calculateDefaultScore(article);
            scoreMap.put(article.getId().toString(), score);
        }

        if (!scoreMap.isEmpty()) {
            redisUtil.zAdd(tempKey, scoreMap);
        }
        redisUtil.rename(tempKey, RedisKeyConstants.HOT_ARTICLE_ZSET_KEY);

        log.info("【定时任务】：热点文章同步完成，共处理 {} 篇文章。", hotArticles.size());
    }

    /**
     * 计算默认分数（当数据库score字段为空时使用）
     * @param article 文章对象
     * @return 计算得出的分数
     */
    private double calculateDefaultScore(HotArticleVO article) {
        int viewCount = article.getViewCount() != null ? article.getViewCount() : 0;
        int likeCount = article.getLikeCount() != null ? article.getLikeCount() : 0;
        int commentCount = article.getCommentCount() != null ? article.getCommentCount() : 0;
        int favoriteCount = article.getFavoriteCount() != null ? article.getFavoriteCount() : 0;

        // 使用与 ArticleScoreEvent 相同的权重计算分数
        return (double) (viewCount * 3) +       // 浏览得 3 分
               (double) (likeCount * 10) +      // 点赞得 10 分
               (double) (commentCount * 20) +   // 评论得 20 分
               (double) (favoriteCount * 15);   // 收藏得 15 分
    }
}