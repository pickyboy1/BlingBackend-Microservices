package com.pickyboy.blingBackend.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pickyboy.blingBackend.dto.comment.CommentCreateRequest;
import com.pickyboy.blingBackend.dto.resource.CopyResourceRequest;
import com.pickyboy.blingBackend.dto.resource.CreateResourceRequest;
import com.pickyboy.blingBackend.dto.resource.MoveResourceRequest;
import com.pickyboy.blingBackend.dto.resource.UpdateResourceContentRequest;
import com.pickyboy.blingBackend.dto.resource.UpdateResourceInfoRequest;
import com.pickyboy.blingBackend.dto.resource.UpdateResourceStatusRequest;
import com.pickyboy.blingBackend.dto.resource.UpdateResourceVisibilityRequest;
import com.pickyboy.blingBackend.entity.Resources;
import com.pickyboy.blingBackend.vo.comment.RootCommentVO;
import com.pickyboy.blingBackend.vo.comment.SubCommentVO;
import com.pickyboy.blingBackend.vo.resource.PublicResourceVO;
import com.pickyboy.blingBackend.vo.resource.ShareUrlVO;

/**
 * 文档/资源服务接口
 *
 * @author pickyboy
 */
public interface IResourceService extends IService<Resources> {

    /**
     * 在知识库中新建资源
     *
     * @param kbId 知识库ID
     * @param createRequest 创建请求
     * @return 资源信息
     */
    Resources createResource(Long kbId, CreateResourceRequest createRequest);

    /**
     * 查看单个资源的完整信息
     *
     * @param resId 资源ID
     * @return 资源内容
     */
    Resources getResourceById(Long resId);

    /**
     * 更新资源内容或标题
     *
     * @param resId 资源ID
     * @param updateRequest 更新请求
     */
    void updateResource(Long resId, UpdateResourceContentRequest updateRequest);

    /**
     * 删除资源 (逻辑删除)
     *
     * @param resId 资源ID
     */
    void deleteResource(Long resId);

    /**
     * 重命名资源
     *
     * @param resId 资源ID
     * @param infoRequest 信息更新请求
     */
    void renameResource(Long resId, UpdateResourceInfoRequest infoRequest);

    /**
     * 更新资源可见性
     *
     * @param resId 资源ID
     * @param visibilityRequest 可见性更新请求
     */
    void updateResourceVisibility(Long resId, UpdateResourceVisibilityRequest visibilityRequest);

    /**
     * 更新资源上架/下架状态
     *
     * @param resId 资源ID
     * @param statusRequest 状态更新请求
     */
    void updateResourceStatus(Long resId, UpdateResourceStatusRequest statusRequest);

    /**
     * 恢复资源
     *
     * @param resId 资源ID
     * @param request 恢复请求（可选），指定恢复位置
     */
    void restoreResource(Long resId, com.pickyboy.blingBackend.dto.resource.RestoreResourceRequest request);

    /**
     * 彻底删除资源
     *
     * @param resId 资源ID
     */
    void permanentlyDeleteResource(Long resId);

    /**
     * 移动资源或目录
     *
     * @param resId 资源ID
     * @param moveRequest 移动请求
     */
    void moveResource(Long resId, MoveResourceRequest moveRequest);

    /**
     * 复制资源
     *
     * @param resId 资源ID
     * @param copyRequest 复制请求
     */
    Resources copyResource(Long resId, CopyResourceRequest copyRequest);

    /**
     * 复制目录(及目录下所有子资源)
     *
     * @param resId 资源ID
     * @param copyRequest 复制请求
     */
    void copyResourceTree(Long resId, CopyResourceRequest copyRequest);

    /**
     * 生成资源分享链接
     *
     * @param resId 资源ID
     * @return 分享链接信息
     */
    ShareUrlVO generateResourceShareLink(Long resId);

    /**
     * 访问分享链接查看资源
     *
     * @param kbShareId 知识库分享ID
     * @param resShareId 资源分享ID
     * @return 公开资源内容
     */
    PublicResourceVO accessSharedResource(String kbShareId, String resShareId);

    /**
     * 点赞文章
     *
     * @param articleId 文章ID
     */
    void likeArticle(Long articleId);

    /**
     * 取消点赞文章
     *
     * @param articleId 文章ID
     */
    void unlikeArticle(Long articleId);

    /**
     * 获取文章的根评论列表
     *
     * @param articleId 文章ID
     * @param page 页码
     * @param limit 每页数量
     * @return 根评论列表
     */
    List<RootCommentVO> listArticleComments(Long articleId, Integer page, Integer limit);

    /**
     * 获取评论的子评论列表
     *
     * @param commentId 评论ID
     * @param page 页码
     * @param limit 每页数量
     * @return 子评论列表
     */
    List<SubCommentVO> listCommentReplies(Long commentId, Integer page, Integer limit);

    /**
     * 发表评论
     *
     * @param articleId 文章ID
     * @param commentRequest 评论请求
     * @return 评论信息
     */
    RootCommentVO createComment(Long articleId, CommentCreateRequest commentRequest);

    /**
     * 删除评论
     *
     * @param commentId 评论ID
     */
    void deleteComment(Long commentId);

    /**
     * 提交投稿（申请推荐）
     *
     * @param submissionRequest 投稿请求
     */
    void createSubmission(Object submissionRequest);

    /**
     * 获取推荐文章列表
     *
     * @param sortBy 排序方式：hot(热门) 或 new(最新)
     * @param page 页码
     * @param limit 每页数量
     * @return 推荐文章列表
     */
    List<PublicResourceVO> listExploreArticles(String sortBy, Integer page, Integer limit);

    /**
     * 查询在未删除知识库中、但自身已被删除的文档列表
     * @param userId 用户ID
     * @return 文档列表
     */
    List<Resources> listDeletedResourcesInActiveKbs(Long userId);

    /**
     * 恢复资源到指定版本
     *
     * @param resId 资源ID
     * @param versionId 版本ID
     */
    void restoreResourceToVersion(Long resId, Long versionId);

}