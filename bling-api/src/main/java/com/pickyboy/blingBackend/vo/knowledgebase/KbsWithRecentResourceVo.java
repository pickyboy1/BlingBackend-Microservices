package com.pickyboy.blingBackend.vo.knowledgebase;

import com.pickyboy.blingBackend.entity.Resources;
import lombok.Data;

import java.util.List;

@Data
public class KbsWithRecentResourceVo {

    private Long id;
    private String name;
    private String iconIndex;
    private Integer visibility;

    private List<Resources> recentResources;
}
