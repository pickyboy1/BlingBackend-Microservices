package com.pickyboy.blingBackend.vo.resource;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 公开分享文档VO
 *
 * @author pickyboy
 */
@Data
public class PublicResourceVO {

    private Long id;

    /**
     * 文档标题
     */
    private String title;

    /**
     * 文档内容
     */
    private String content;

    /**
     * 更新时间
     */
    private LocalDateTime publishedAt;

    private Integer viewCount;

    private Integer likeCount;

    private Integer commentCount;

    private Integer favoriteCount;

    private Long kbId;

   private  Long userId;

   private String coverUrl;

        /**
         * 作者昵称
         */
        private String nickname;

        /**
         * 作者头像URL
         */
        private String avatarUrl;

}