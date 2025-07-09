package com.pickyboy.blingBackend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.pickyboy.blingBackend.common.response.Result;
import com.pickyboy.blingBackend.dto.knowledgebase.InsertKnowledgeBaseRequest;
import com.pickyboy.blingBackend.entity.KnowledgeBases;
import com.pickyboy.blingBackend.entity.Resources;
import com.pickyboy.blingBackend.vo.knowledgebase.KbsWithRecentResourceVo;
import com.pickyboy.blingBackend.vo.knowledgebase.TrashVO;
import com.pickyboy.blingBackend.vo.resource.ResourceTreeVo;
import com.pickyboy.blingBackend.service.IKnowledgeBaseService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 知识库控制器
 * 处理知识库相关的API请求
 *
 * @author pickyboy
 */
@Slf4j
@RestController
@RequestMapping()
@RequiredArgsConstructor
public class KnowledgeBaseController {

    private final IKnowledgeBaseService knowledgeBaseService;

    /**
     * 获取当前用户的知识库列表
     * GET /knowledge-bases?withRecentResources=true/false
     *
     * @param withRecentResources 是否包含最近资源
     * @return 知识库列表
     */
    @GetMapping("/knowledge-bases")
    public Result<List<KbsWithRecentResourceVo>> getUserKnowledgeBases(
            @RequestParam(value = "withRecentResources", defaultValue = "false") boolean withRecentResources) {
        log.info("获取当前用户的知识库列表: withRecentResources={}", withRecentResources);
        List<KbsWithRecentResourceVo> knowledgeBases = knowledgeBaseService.getUserKnowledgeBases(withRecentResources);
        return Result.success(knowledgeBases);
    }

    /**
     * 创建新的知识库
     * POST /knowledge-bases
     *
     * @param createRequest 创建请求
     * @return 操作结果
     */
    @PostMapping("/knowledge-bases")
    public Result<Void> createKnowledgeBase(@Valid @RequestBody InsertKnowledgeBaseRequest createRequest) {
        log.info("创建新的知识库: name={}", createRequest.getName());
        knowledgeBaseService.createKnowledgeBase(createRequest);
        return Result.success();
    }

    /**
     * 获取指定知识库的详细信息(用于知识库编辑页面展示和查看他人知识库详细信息)
     * GET /knowledge-bases/{kbId}
     *
     * @param kbId 知识库ID
     * @return 知识库详细信息
     */
    @GetMapping("/knowledge-bases/{kbId}")
    public Result<KnowledgeBases> getKnowledgeBase(@PathVariable Long kbId) {
        log.info("获取知识库详细信息: kbId={}", kbId);
        KnowledgeBases knowledgeBase = knowledgeBaseService.getKnowledgeBase(kbId);
        return Result.success(knowledgeBase);
    }



    /**
     * 更新知识库信息
     * PUT /knowledge-bases/{kbId}
     *
     * @param kbId 知识库ID
     * @param updateRequest 更新请求
     * @return 操作结果
     */
    @PutMapping("/knowledge-bases/{kbId}")
    public Result<Void> updateKnowledgeBase(@PathVariable Long kbId,
                                           @Valid @RequestBody InsertKnowledgeBaseRequest updateRequest) {
        log.info("更新知识库信息: kbId={}", kbId);
        knowledgeBaseService.updateKnowledgeBase(kbId, updateRequest);
        return Result.success();
    }

    /**
     * 删除知识库 (逻辑删除)
     * DELETE /knowledge-bases/{kbId}
     *
     * @param kbId 知识库ID
     * @return 操作结果
     */
    @DeleteMapping("/knowledge-bases/{kbId}")
    public Result<Void> deleteKnowledgeBase(@PathVariable Long kbId) {
        log.info("删除知识库: kbId={}", kbId);
        knowledgeBaseService.deleteKnowledgeBase(kbId);
        return Result.success();
    }

    /**
     * 更改知识库的可见性
     * PATCH /knowledge-bases/{kbId}/visibility
     *
     * @param kbId 知识库ID
     * @param visibilityRequest 可见性设置请求
     * @return 操作结果
     */
    @PatchMapping("/knowledge-bases/{kbId}/visibility")
    public Result<Void> updateKnowledgeBaseVisibility(@PathVariable Long kbId,
                                                      @Valid @RequestBody VisibilityRequest visibilityRequest) {
        log.info("更新知识库可见性: kbId={}, visibility={}", kbId, visibilityRequest.getVisibility());
        knowledgeBaseService.updateKnowledgeBaseVisibility(kbId, visibilityRequest.getVisibility());
        return Result.success();
    }

    /**
     * 从回收站恢复知识库
     * POST /recycle-bin/knowledge-bases/{kbId}
     *
     * @param kbId 知识库ID
     * @return 操作结果
     */
    @PostMapping("/recycle-bin/knowledge-bases/{kbId}")
    public Result<Void> restoreKnowledgeBase(@PathVariable Long kbId) {
        log.info("从回收站恢复知识库: kbId={}", kbId);
        knowledgeBaseService.restoreKnowledgeBase(kbId);
        return Result.success();
    }

    /**
     * 获取回收站内容列表
     * GET /trash
     *
     * @return 回收站内容
     */
    @GetMapping("/recycle-bin/items")
    public Result<TrashVO> getTrashContent() {
        log.info("获取回收站内容列表");
        TrashVO trash = knowledgeBaseService.getTrashContent();
        return Result.success(trash);
    }

    /**
     * 获取知识库下最近编辑的文档
     * GET /knowledge-bases/{kbId}/recent-resources
     *
     * @param kbId 知识库ID
     * @return 最近编辑的文档列表
     */
    @GetMapping("/knowledge-bases/{kbId}/recent-resources")
    public Result<List<Resources>> getRecentResources(@PathVariable Long kbId) {
        log.info("获取知识库下最近编辑的文档: kbId={}", kbId);
        List<Resources> documents = knowledgeBaseService.getRecentResources(kbId);
        return Result.success(documents);
    }

    /**
     * 获取指定知识库下文档树(要么是点进知识库,要么是点到知识库的文档)
     * 会触发知识库访问量增加
     * GET /knowledge-bases/{kbId}/resources/tree
     *
     * @param kbId 知识库ID
     * @return 知识库下文档树
     */
    @GetMapping("/knowledge-bases/{kbId}/resources/tree")
    public Result<List<ResourceTreeVo>> getKnowledgeBaseWithDocuments(@PathVariable Long kbId) {
        log.info("获取知识库下文档树: kbId={}", kbId);
        List<ResourceTreeVo> knowledgeBaseWithDocuments = knowledgeBaseService.getKnowledgeBaseWithDocuments(kbId);
        return Result.success(knowledgeBaseWithDocuments);
    }

    /**
     * 可见性设置请求VO
     */
    @lombok.Data
    public static class VisibilityRequest {
        /**
         * 可见性设置：0-私有，1-公开
         */
        @NotNull(message = "可见性设置不能为空")
        @Min(value = 0, message = "可见性值必须为0或1")
        @Max(value = 1, message = "可见性值必须为0或1")
        private Integer visibility;
    }
}