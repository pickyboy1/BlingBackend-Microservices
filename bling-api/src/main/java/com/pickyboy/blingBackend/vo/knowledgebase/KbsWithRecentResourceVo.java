package com.pickyboy.blingBackend.vo.knowledgebase;

import java.util.List;

import com.pickyboy.blingBackend.entity.Resources;

import lombok.Data;

@Data
public class KbsWithRecentResourceVo {

    private Long id;
    private String name;
    private String iconIndex;
    private Integer visibility;
    private String coverUrl;

    private List<Resources> recentResources;
}
