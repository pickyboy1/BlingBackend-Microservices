package com.pickyboy.blingBackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pickyboy.blingBackend.dto.submission.SubmissionRequestDTO;
import com.pickyboy.blingBackend.dto.submission.SubmissionUpdateDTO;
import com.pickyboy.blingBackend.entity.Submissions;
import com.pickyboy.blingBackend.vo.submission.SubmissionVO;

import java.util.List;

/**
 * 投稿服务接口
 * 专家级重构，风格统一，注释齐全
 */
public interface ISubmissionsService extends IService<Submissions> {
    /**
     * 创建投稿
     * @param requestDTO 投稿请求
     * @param userId 用户ID
     * @return 投稿ID
     */
    Long createSubmission(SubmissionRequestDTO requestDTO, Long userId);

    /**
     * 获取单个投稿详情
     * @param id 投稿ID
     * @param userId 用户ID
     * @return 投稿VO
     */
    SubmissionVO getSubmissionById(Long id, Long userId);

    /**
     * 获取当前用户所有投稿
     * @param userId 用户ID
     * @return 投稿VO列表
     */
    List<SubmissionVO> getSubmissionsForUser(Long userId);

    /**
     * 更新投稿原因
     * @param id 投稿ID
     * @param updateDTO 更新内容
     * @param userId 用户ID
     */
    void updateSubmissionReason(Long id, SubmissionUpdateDTO updateDTO, Long userId);

    /**
     * 取消投稿
     * @param id 投稿ID
     * @param userId 用户ID
     */
    void cancelSubmission(Long id, Long userId);
}
