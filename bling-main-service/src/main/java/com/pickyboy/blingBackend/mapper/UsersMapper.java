package com.pickyboy.blingBackend.mapper;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pickyboy.blingBackend.entity.Users;

/**
 * <p>
 * 用户表 Mapper 接口
 * </p>
 *
 * @author pickyboy
 * @since 2025-06-25
 */
public interface UsersMapper extends BaseMapper<Users> {

    // ====== 【原子操作】计数器更新方法 ======

    /**
     * 原子增加用户粉丝数
     * @param userId 用户ID
     * @return 影响行数
     */
    int incrementFollowerCount(@Param("userId") Long userId);

    /**
     * 原子减少用户粉丝数
     * @param userId 用户ID
     * @return 影响行数
     */
    int decrementFollowerCount(@Param("userId") Long userId);

    /**
     * 原子增加用户关注数
     * @param userId 用户ID
     * @return 影响行数
     */
    int incrementFollowedCount(@Param("userId") Long userId);

    /**
     * 原子减少用户关注数
     * @param userId 用户ID
     * @return 影响行数
     */
    int decrementFollowedCount(@Param("userId") Long userId);

    /**
     * 根据用户ID查询用户的状态信息，用于权限校验。
     * 这是一个轻量级查询，只获取必要的字段。
     * @param userId 用户ID
     * @return 用户实体，仅包含状态相关字段
     */
    Users findUserStatusById(@Param("userId") Long userId);

}
