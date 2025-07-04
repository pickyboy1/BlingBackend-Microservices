package com.pickyboy.blingBackend.vo.knowledgebase;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class DeletedKnowledgeBaseVO {
    private Long id;
    private String name;
    private String iconIndex;
    private Integer visibility;
    private LocalDateTime deletedAt;
    private Boolean isDeleted;
}