package com.packclaw.service;

import com.packclaw.common.domain.TableDataVO;
import com.packclaw.model.resp.ChatDetailsResp;
import com.packclaw.model.resp.ChatListResp;

/**
 * 会话历史记录服务接口
 *
 * @author PackClaw
 * @version 1.0
 */
public interface ChatHistoryService {

    /**
     * 分页查询会话列表
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param name     会话名称（模糊查询）
     * @return 会话列表分页数据
     */
    TableDataVO<ChatListResp> getChatHistoryList(Integer pageNum, Integer pageSize, String name);

    /**
     * 根据会话ID获取会话详情
     *
     * @param chatId 会话ID
     * @return 会话详情
     */
    ChatDetailsResp getChatDetailsById(String chatId);

    /**
     * 逻辑删除会话历史记录
     *
     * @param chatId 会话ID
     * @return 影响的行数
     */
    int logicDeleteChatHistory(String chatId);

    /**
     * 更新会话标题
     *
     * @param chatId 会话ID
     * @param title  新标题
     * @return 影响的行数
     */
    int updateChatTitle(String chatId, String title);
}
