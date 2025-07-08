package com.pickyboy.blingBackend.mapper;

import com.pickyboy.blingBackend.entity.Submissions;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pickyboy.blingBackend.vo.submission.SubmissionVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 投稿审核表 Mapper接口
 * 专家级重构，注释齐全
 */
public interface SubmissionsMapper extends BaseMapper<Submissions> {

    /**
     * 根据用户ID查询其所有投稿记录（含资源、知识库等信息）
     * @param userId 用户ID
     * @return 投稿VO列表
     */
    List<SubmissionVO> findByUserId(@Param("userId") Long userId);

    /**
     * 根据ID和用户ID查询单个投稿详情
     * @param id 投稿ID
     * @param userId 用户ID
     * @return 投稿VO
     */
    SubmissionVO findVOByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}
