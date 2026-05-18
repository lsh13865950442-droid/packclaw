package com.packclaw.mapper;

import com.packclaw.model.po.SessionChat;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 会话聊天 Mapper 接口
 *
 * @author PackClaw
 * @version 1.0
 */
@Mapper
public interface SessionChatMapper {

    /**
     * 插入或更新单个状态
     *
     * @param entity 会话聊天实体
     * @return 影响的行数
     */
    int upsertState(SessionChat entity);

    /**
     * 批量插入状态项
     *
     * @param entities 会话聊天实体列表
     * @return 影响的行数
     */
    int batchInsertStates(@Param("entities") List<SessionChat> entities);

    /**
     * 根据会话ID和状态键查询单个状态
     *
     * @param sessionId 会话ID
     * @param stateKey 状态键
     * @param itemIndex 项索引
     * @return 会话聊天实体
     */
    SessionChat selectByKey(@Param("sessionId") String sessionId,
                            @Param("stateKey") String stateKey,
                            @Param("itemIndex") Integer itemIndex);

    /**
     * 根据会话ID和状态键查询列表状态
     *
     * @param sessionId 会话ID
     * @param stateKey 状态键
     * @return 会话聊天实体列表
     */
    List<SessionChat> selectListByKey(@Param("sessionId") String sessionId,
                                      @Param("stateKey") String stateKey);

    /**
     * 获取列表状态的最大索引
     *
     * @param sessionId 会话ID
     * @param stateKey 状态键
     * @return 最大索引值，如果不存在返回null
     */
    Integer selectMaxIndex(@Param("sessionId") String sessionId,
                           @Param("stateKey") String stateKey);

    /**
     * 检查会话是否存在
     *
     * @param sessionId 会话ID
     * @return 存在返回1，否则返回0
     */
    Integer existsSession(@Param("sessionId") String sessionId);

    /**
     * 删除会话的所有状态
     *
     * @param sessionId 会话ID
     * @return 影响的行数
     */
    int deleteBySessionId(@Param("sessionId") String sessionId);

    /**
     * 删除指定键的所有状态项
     *
     * @param sessionId 会话ID
     * @param stateKey 状态键
     * @return 影响的行数
     */
    int deleteByStateKey(@Param("sessionId") String sessionId,
                         @Param("stateKey") String stateKey);

    /**
     * 查询所有不同的会话ID
     *
     * @return 会话ID列表
     */
    List<String> selectAllSessionIds();

    /**
     * 清除所有会话数据
     *
     * @return 影响的行数
     */
    int clearAllSessions();
}
