package com.pickyboy.blingBackend.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pickyboy.blingBackend.entity.Favorites;
import com.pickyboy.blingBackend.vo.user.ActivityRecord;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 * 收藏条目表 Mapper 接口
 * </p>
 *
 * @author pickyboy
 * @since 2025-06-25
 */
public interface FavoritesMapper extends BaseMapper<Favorites> {

    /**
     * 获取收藏夹分组下的文章列表
     *
     * @param groupId 分组ID
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 收藏文章列表
     */
    List<ActivityRecord> getArticlesInGroup(@Param("groupId") Long groupId, @Param("offset") Integer offset, @Param("limit") Integer limit);

    @Select("SELECT EXISTS (SELECT 1 FROM favorites f join favorite_groups fg on f.group_id = fg.id WHERE resource_id = #{resourceId} AND fg.user_id = #{userId})")
    int isFavorited(Long userId, Long resourceId);
}
