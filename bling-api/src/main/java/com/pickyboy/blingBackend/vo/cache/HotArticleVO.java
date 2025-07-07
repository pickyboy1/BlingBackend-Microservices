package com.pickyboy.blingBackend.vo.cache;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 热门文章缓存 VO
 * 用于 Redis 缓存，包含文章基本信息和作者信息
 * 排除: score, is_deleted, status, share_id
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotArticleVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 资源ID
     */
    private Long id;

    /**
     * 外键,关联知识库
     */
    private Long knowledgeBaseId;

    /**
     * 父文档id
     */
    private Long preId;

    /**
     * 外键,关联用户
     */
    private Long userId;

    /**
     * 文档名
     */
    private String title;

    /**
     * 文档类型
     */
    private String type;

    /**
     * 文档内容url地址
     */
    private String content;

    /**
     * 可见性: 0-私密, 1-公开
     */
    private Integer visibility;

    /**
     * 发布时间
     */
    private LocalDateTime publishedAt;

    /**
     * 浏览量
     */
    private Integer viewCount;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 评论数
     */
    private Integer commentCount;

    /**
     * 收藏数
     */
    private Integer favoriteCount;

    /**
     * 热度分
     */
    private Integer score;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 作者昵称
     */
    private String authorNickname;

    /**
     * 作者头像URL
     */
    private String authorAvatarUrl;
}