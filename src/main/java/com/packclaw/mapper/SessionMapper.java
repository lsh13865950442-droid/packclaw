package com.packclaw.mapper;

import com.packclaw.model.po.Session;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 会话 Mapper 接口
 *
 * @author PackClaw
 * @version 1.0
 */
@Mapper
public interface SessionMapper {

    /**
     * 插入会话
     *
     * @param entity 会话实体
     * @return 影响的行数
     */
    int insert(Session entity);

    /**
     * 根据ID更新会话
     *
     * @param entity 会话实体
     * @return 影响的行数
     */
    int updateById(Session entity);

    /**
     * 根据ID查询会话
     *
     * @param id 会话ID
     * @return 会话实体
     */
    Session selectById(@Param("id") String id);

    /**
     * 查询会话列表
     *
     * @return 会话列表
     */
    List<Session> selectByUserId();

    /**
     * 根据名称查询会话列表（支持模糊查询）
     *
     * @param name   会话名称（模糊查询）
     * @return 会话列表
     */
    List<Session> selectByUserIdAndName(@Param("name") String name);

    /**
     * 根据ID删除会话（逻辑删除，更新status字段）
     *
     * @param id 会话ID
     * @return 影响的行数
     */
    int deleteById(@Param("id") String id);

    /**
     * 删除所有会话（逻辑删除）
     *
     * @return 影响的行数
     */
    int deleteByUserId();

    /**
     * 根据ID更新会话名称
     *
     * @param sessionId 会话ID
     * @param title 会话名称
     * @return 影响的行数
     */
    int updateNameById(@Param("sessionId") String sessionId, @Param("title") String title);
}
