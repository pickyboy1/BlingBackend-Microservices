package com.pickyboy.blingBackend.dto.submission;

import lombok.Data;

@Data
public class SubmissionRequestDTO {
    private Long knowledgeBaseId;
    private Long resourceId;
    private String recommendReason;

}