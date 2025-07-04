package com.pickyboy.blingBackend.dto.collection;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 添加文章到收藏夹请求DTO
 *
 * @author pickyboy
 */
@Data
public class CollectionItemCreateRequest {

    /**
     * 要收藏的文章ID
     */
    @NotNull(message = "文章ID不能为空")
    private Long resourceId;
}