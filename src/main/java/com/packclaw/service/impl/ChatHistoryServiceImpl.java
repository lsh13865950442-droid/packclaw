package com.packclaw.service.impl;

import com.alibaba.fastjson2.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.packclaw.common.domain.TableDataVO;
import com.packclaw.mapper.SessionChatMapper;
import com.packclaw.mapper.SessionMapper;
import com.packclaw.model.po.Session;
import com.packclaw.model.po.SessionChat;
import com.packclaw.model.resp.ChatDetailsResp;
import com.packclaw.model.resp.ChatListResp;
import com.packclaw.service.ChatHistoryService;
import io.agentscope.core.message.Msg;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 会话历史记录服务实现类
 *
 * @author PackClaw
 * @version 1.0
 */
@Slf4j
@Service
public class ChatHistoryServiceImpl implements ChatHistoryService {

    @Resource
    private SessionMapper sessionMapper;

    @Resource
    private SessionChatMapper sessionChatMapper;

    @Override
    public TableDataVO<ChatListResp> getChatHistoryList(Integer pageNum, Integer pageSize, String name) {
        // 使用PageHelper进行分页
        PageHelper.startPage(pageNum, pageSize);

        // 查询会话列表
        List<Session> sessions = sessionMapper.selectByUserIdAndName(name);
        PageInfo<Session> pageInfo = new PageInfo<>(sessions);

        // 转换为响应对象
        List<ChatListResp> respList = sessions.stream()
                .map(this::convertToChatListResp)
                .collect(Collectors.toList());

        return TableDataVO.<ChatListResp>builder()
                .rows(respList)
                .total(pageInfo.getTotal())
                .build();
    }

    @Override
    public ChatDetailsResp getChatDetailsById(String chatId) {
        // 查询会话基本信息
        Session session = sessionMapper.selectById(chatId);
        if (session == null) {
            log.warn("会话不存在, chatId: {}", chatId);
            return null;
        }

        // 查询会话的消息列表
        List<SessionChat> sessionChats = sessionChatMapper.selectListByKey(chatId, "autoContextMemory_originalMessages");

        // 转换消息列表，按 itemIndex 排序
        List<Msg> msgList = sessionChats.stream()
                .sorted(Comparator.comparing(SessionChat::getItemIndex, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(sessionChat -> JSON.parseObject(sessionChat.getStateData(), Msg.class))
                .toList();

        return ChatDetailsResp.builder()
                .chatId(session.getId())
                .title(session.getTitle())
                .createTime(session.getCreateTime())
                .updateTime(session.getUpdateTime())
                .messages(msgList)
                .build();
    }

    @Override
    public int logicDeleteChatHistory(String chatId) {
        log.info("逻辑删除会话, chatId: {}", chatId);
        return sessionMapper.deleteById(chatId);
    }

    @Override
    public int updateChatTitle(String chatId, String title) {
        log.info("更新会话标题, chatId: {}, title: {}", chatId, title);
        return sessionMapper.updateNameById(chatId, title);
    }

    /**
     * 将Session转换为ChatListResp
     */
    private ChatListResp convertToChatListResp(Session session) {
        return ChatListResp.builder()
                .chatId(session.getId())
                .title(session.getTitle())
                .createTime(session.getCreateTime())
                .updateTime(session.getUpdateTime())
                .build();
    }
}
