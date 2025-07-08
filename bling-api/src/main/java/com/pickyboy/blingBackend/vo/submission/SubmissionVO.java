package com.pickyboy.blingBackend.vo.submission;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubmissionVO {
    private Long id;
    private Long knowledgeBaseId;
    private Long resourceId;
    private Long userId;
    private Integer status;
    private String recommendReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 可以添加关联的 resourceTitle, knowledgeBaseName 等字段
    private String resourceTitle;
    private String knowledgeBaseName;

}