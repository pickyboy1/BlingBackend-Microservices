package com.pickyboy.blingBackend.vo.resource;

import lombok.Data;

import java.util.List;

@Data
public class ResourceTreeVo {
    private Long id;
    private String title;
    private String type;
    private Long preId;
    private List<ResourceTreeVo> children;
}
