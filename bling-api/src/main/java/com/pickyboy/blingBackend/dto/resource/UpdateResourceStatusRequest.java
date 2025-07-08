package com.pickyboy.blingBackend.dto.resource;

import lombok.Data;

/**
 * 更新资源状态请求
 *
 * @author pickyboy
 */
@Data
public class UpdateResourceStatusRequest {

    /**
     * 资源状态
     * 0: 下架
     * 1: 上架
     * 2: 强制下架(管理员)
     */
    private Integer status;
}