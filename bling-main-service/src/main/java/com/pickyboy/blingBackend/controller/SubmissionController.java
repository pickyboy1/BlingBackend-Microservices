package com.pickyboy.blingBackend.controller;

import com.pickyboy.blingBackend.common.exception.BusinessException;
import com.pickyboy.blingBackend.common.exception.ErrorCode;
import com.pickyboy.blingBackend.common.response.Result;
import com.pickyboy.blingBackend.common.utils.CurrentHolder;
import com.pickyboy.blingBackend.dto.submission.SubmissionRequestDTO;
import com.pickyboy.blingBackend.dto.submission.SubmissionUpdateDTO;
import com.pickyboy.blingBackend.service.ISubmissionsService;
import com.pickyboy.blingBackend.vo.submission.SubmissionVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 投稿相关接口
 * 专家级重构，风格统一，异常处理、日志、参数校验齐全
 */
@Slf4j
@RestController
@RequestMapping("/submissions")
@RequiredArgsConstructor
public class SubmissionController {

    private final ISubmissionsService submissionService;

    /**
     * 提交一篇新的稿件
     * POST /submissions
     */
    @PostMapping
    public Result<Long> createSubmission(@Valid @RequestBody SubmissionRequestDTO requestDTO) {
        Long userId = CurrentHolder.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        log.info("用户 {} 提交新稿件: {}", userId, requestDTO);
        Long submissionId = submissionService.createSubmission(requestDTO, userId);
        return Result.success(submissionId);
    }

    /**
     * 获取当前用户的所有投稿记录
     * GET /submissions
     */
    @GetMapping
    public Result<List<SubmissionVO>> getUserSubmissions() {
        Long userId = CurrentHolder.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        List<SubmissionVO> submissions = submissionService.getSubmissionsForUser(userId);
        return Result.success(submissions);
    }

    /**
     * 获取单个投稿的详细信息
     * GET /submissions/{id}
     */
    @GetMapping("/{id}")
    public Result<SubmissionVO> getSubmissionDetails(@PathVariable Long id) {
        Long userId = CurrentHolder.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        SubmissionVO submission = submissionService.getSubmissionById(id, userId);
        if (submission == null) {
            throw new BusinessException(ErrorCode.SUBMISSION_NOT_FOUND);
        }
        return Result.success(submission);
    }

    /**
     * 更新投稿原因
     * PUT /submissions/{id}/reason
     */
    @PutMapping("/{id}/reason")
    public Result<Void> updateSubmissionReason(@PathVariable Long id, @Valid @RequestBody SubmissionUpdateDTO updateDTO) {
        Long userId = CurrentHolder.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        submissionService.updateSubmissionReason(id, updateDTO, userId);
        return Result.success();
    }

    /**
     * 取消投稿
     * DELETE /submissions/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> cancelSubmission(@PathVariable Long id) {
        Long userId = CurrentHolder.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        submissionService.cancelSubmission(id, userId);
        return Result.success();
    }
}