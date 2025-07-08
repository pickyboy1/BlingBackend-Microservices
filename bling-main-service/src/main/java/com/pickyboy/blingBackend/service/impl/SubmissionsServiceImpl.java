package com.pickyboy.blingBackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pickyboy.blingBackend.common.exception.BusinessException;
import com.pickyboy.blingBackend.common.exception.ErrorCode;
import com.pickyboy.blingBackend.dto.submission.SubmissionRequestDTO;
import com.pickyboy.blingBackend.dto.submission.SubmissionUpdateDTO;
import com.pickyboy.blingBackend.entity.Submissions;
import com.pickyboy.blingBackend.mapper.SubmissionsMapper;
import com.pickyboy.blingBackend.service.ISubmissionsService;
import com.pickyboy.blingBackend.vo.submission.SubmissionVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * 投稿服务实现
 * 专家级重构，风格统一，异常、日志、注释齐全
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionsServiceImpl extends ServiceImpl<SubmissionsMapper, Submissions> implements ISubmissionsService {

    private static final int STATUS_PENDING = 0;
    private static final int STATUS_CANCELLED = 3;

    private final SubmissionsMapper submissionsMapper;

    @Override
    public Long createSubmission(SubmissionRequestDTO requestDTO, Long userId) {
        if (userId == null) throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        Submissions submission = new Submissions();
        submission.setUserId(userId);
        submission.setKnowledgeBaseId(requestDTO.getKnowledgeBaseId());
        submission.setResourceId(requestDTO.getResourceId());
        submission.setRecommendReason(requestDTO.getRecommendReason());
        submission.setStatus(STATUS_PENDING);
        submissionsMapper.insert(submission);
        log.info("用户{}创建投稿: {}", userId, submission);
        return submission.getId();
    }

    @Override
    public SubmissionVO getSubmissionById(Long id, Long userId) {
        if (userId == null) throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        SubmissionVO vo = submissionsMapper.findVOByIdAndUserId(id, userId);
        if (vo == null) throw new BusinessException(ErrorCode.SUBMISSION_NOT_FOUND);
        return vo;
    }

    @Override
    public List<SubmissionVO> getSubmissionsForUser(Long userId) {
        if (userId == null) throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        return submissionsMapper.findByUserId(userId);
    }

    @Override
    public void updateSubmissionReason(Long id, SubmissionUpdateDTO updateDTO, Long userId) {
        Submissions submission = verifyAndGetSubmission(id, userId);
        if (!Objects.equals(submission.getStatus(), STATUS_PENDING)) {
            throw new BusinessException(ErrorCode.SUBMISSION_ALREADY_PROCESSED);
        }
        UpdateWrapper<Submissions> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id).set("recommend_reason", updateDTO.getRecommendReason());
        submissionsMapper.update(null, updateWrapper);
        log.info("用户{}更新投稿{}原因: {}", userId, id, updateDTO.getRecommendReason());
    }

    @Override
    public void cancelSubmission(Long id, Long userId) {
        Submissions submission = verifyAndGetSubmission(id, userId);
        if (!Objects.equals(submission.getStatus(), STATUS_PENDING)) {
            throw new BusinessException(ErrorCode.SUBMISSION_ALREADY_PROCESSED);
        }
        UpdateWrapper<Submissions> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id).set("status", STATUS_CANCELLED);
        submissionsMapper.update(null, updateWrapper);
        log.info("用户{}取消投稿{}", userId, id);
    }

    /**
     * 校验投稿是否存在且属于当前用户
     */
    private Submissions verifyAndGetSubmission(Long submissionId, Long userId) {
        if (userId == null) throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        Submissions submission = submissionsMapper.selectById(submissionId);
        if (submission == null) throw new BusinessException(ErrorCode.SUBMISSION_NOT_FOUND);
        if (!Objects.equals(submission.getUserId(), userId)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        return submission;
    }
}
