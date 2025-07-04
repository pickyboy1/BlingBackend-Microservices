package com.pickyboy.blingBackend.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pickyboy.blingBackend.entity.KnowledgeBases;

/**
 * <p>
 * 知识库表 Mapper 接口
 * </p>
 *
 * @author pickyboy
 * @since 2025-06-25
 */
public interface KnowledgeBasesMapper extends BaseMapper<KnowledgeBases> {

    // ====== 【原子操作】计数器更新方法 ======

    /**
     * 原子增加知识库浏览量
     * @param kbId 知识库ID
     * @return 影响行数
     */
    int incrementViewCount(@Param("kbId") Long kbId);

    // ====== 【逻辑删除相关】回收站查询方法 ======

    /**
     * 查询用户的已删除知识库列表（回收站）
     * @param userId 用户ID
     * @return 已删除的知识库列表
     */
    @Select("SELECT id, name, icon_index, visibility, updated_at AS deleted_at, is_deleted FROM knowledge_bases WHERE user_id = #{userId} AND is_deleted = 1 ORDER BY updated_at DESC")
    List<com.pickyboy.blingBackend.vo.knowledgebase.DeletedKnowledgeBaseVO> selectDeletedByUserId(@Param("userId") Long userId);

    /**
     * 根据ID查询单个已删除的知识库（用于恢复，返回实体）
     * @param kbId 知识库ID
     * @return 已删除的知识库实体，如果不存在或未删除则返回null
     */
    @Select("SELECT * FROM knowledge_bases WHERE id = #{kbId} AND is_deleted = 1")
    com.pickyboy.blingBackend.entity.KnowledgeBases selectDeletedById(@Param("kbId") Long kbId);

    /**
     * 更新已删除知识库的状态（用于恢复操作）
     * @param kbId 知识库ID
     * @param isDeleted 删除状态
     * @return 影响行数
     */
    @Update("UPDATE knowledge_bases SET is_deleted = #{isDeleted}, updated_at = NOW() WHERE id = #{kbId} AND is_deleted = 1")
    int updateDeletedKnowledgeBase(@Param("kbId") Long kbId, @Param("isDeleted") Boolean isDeleted);

}
