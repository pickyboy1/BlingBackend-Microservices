package com.pickyboy.blingBackend.dto.resource;

import lombok.Data;

/**
 * 更新资源可见性请求
 *
 * @author pickyboy
 */
@Data
public class UpdateResourceVisibilityRequest {

    /**
     * 可见性
     * 0: 私有
     * 1: 公开
     */
    private Integer visibility;
}