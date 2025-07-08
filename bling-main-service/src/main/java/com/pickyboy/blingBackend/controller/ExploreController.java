package com.pickyboy.blingBackend.controller;

import com.pickyboy.blingBackend.common.response.PageResult;
import com.pickyboy.blingBackend.common.response.Result;
import com.pickyboy.blingBackend.service.IResourceService;
import com.pickyboy.blingBackend.vo.resource.PublicResourceVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * 发现/探索文章接口
 * 专家级实现：所有接口均只返回公开、未删除、上架、已发表的文章
 */
@Slf4j
@RestController
@RequestMapping("/article")
@RequiredArgsConstructor
@Validated
public class ExploreController {

    private final IResourceService resourceService;

    /**
     * 按标题模糊搜索文章（分页）
     * @param keyword 关键词
     * @param page 页码
     * @param limit 每页数量
     * @return 文章分页结果
     */
    @GetMapping("/search")
    public Result<PageResult<PublicResourceVO>> searchArticles(
            @RequestParam @NotBlank(message = "关键词不能为空") String keyword,
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(defaultValue = "10") @Min(1) Integer limit) {
        log.info("按标题搜索文章: keyword={}, page={}, limit={}", keyword, page, limit);
        PageResult<PublicResourceVO> result = resourceService.searchArticlesByTitle(keyword, page, limit);
        return Result.success(result);
    }

    /**
     * 分页获取最新投稿文章
     * @param page 页码
     * @param limit 每页数量
     * @return 文章分页结果
     */
    @GetMapping("/latest")
    public Result<PageResult<PublicResourceVO>> getLatestArticles(
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(defaultValue = "10") @Min(1) Integer limit) {
        log.info("分页获取最新投稿文章: page={}, limit={}", page, limit);
        PageResult<PublicResourceVO> result = resourceService.listLatestArticles(page, limit);
        return Result.success(result);
    }

    /**
     * 分页获取热门文章（预留，暂未实现，未来用Redis）
     */
    @GetMapping("/hot")
    public Result<PageResult<PublicResourceVO>> getHotArticles(
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(defaultValue = "10") @Min(1) Integer limit) {
        log.info("分页获取热门文章（预留）: page={}, limit={}", page, limit);
        throw new UnsupportedOperationException("热门文章接口暂未实现，未来将基于Redis实现");
    }

    /**
     * 分页获取历史最热文章
     * @param page 页码
     * @param limit 每页数量
     * @return 文章分页结果
     */
    @GetMapping("/history-hot")
    public Result<PageResult<PublicResourceVO>> getHistoryHotArticles(
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(defaultValue = "10") @Min(1) Integer limit) {
        log.info("分页获取历史最热文章: page={}, limit={}", page, limit);
        PageResult<PublicResourceVO> result = resourceService.listHistoryHotArticles(page, limit);
        return Result.success(result);
    }
}
