// 在 com.pickyboy.blingBackend.mapper.ResourceStateMapper.java
package com.pickyboy.blingBackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pickyboy.blingBackend.entity.Resources;
import com.pickyboy.blingBackend.vo.cache.HotArticleVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ResourceStateMapper extends BaseMapper<Resources> {
    // BaseMapper 自带了 selectById 方法，我们暂时不需要添加自定义方法。
    // 未来如果需要更复杂的查询，可以在这里添加。

    /**
     * 查询近7天的热门文章，联查作者信息，按score排序，限制返回数量
     * @param limit 限制返回的文章数量
     * @return 热门文章列表，包含作者昵称和头像
     */
    List<HotArticleVO> selectHotArticlesWithAuthor(@Param("limit") int limit);
}