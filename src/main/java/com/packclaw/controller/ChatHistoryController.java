package com.packclaw.controller;

import com.packclaw.common.domain.Response;
import com.packclaw.common.domain.TableDataVO;
import com.packclaw.model.resp.ChatDetailsResp;
import com.packclaw.model.resp.ChatListResp;
import com.packclaw.service.ChatHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/history")
@Tag(name = "AI对话的历史会话相关", description = "AI助手对话的历史记录相关接口")
public class ChatHistoryController {

    @Resource
    private ChatHistoryService chatHistoryService;

    /**
     * 分页查询会话列表
     */
    @PostMapping("/list")
    @Operation(summary = "分页查询会话列表", description = "分页查询会话列表")
    public Response<TableDataVO<ChatListResp>> history(
            @RequestParam(value = "pageNum") @Parameter(description = "第几页") Integer pageNum,
            @RequestParam(value = "pageSize") @Parameter(description = "一页多大") Integer pageSize,
            @RequestParam(value = "name", required = false) @Parameter(description = "会话名字，模糊查询") String name) {
        log.info("分页查询会话列表, pageNum: {}, pageSize: {}", pageNum, pageSize);
        TableDataVO<ChatListResp> respList = chatHistoryService.getChatHistoryList(pageNum, pageSize, name);
        return Response.ok(respList);
    }

    @PostMapping(value = "/details")
    @Operation(summary = "会话详情", description = "会话详情")
    public Response<ChatDetailsResp> details(@RequestParam(value = "chatId") @Parameter(description = "会话ID")  String chatId){
        ChatDetailsResp chatDetails = chatHistoryService.getChatDetailsById(chatId);
        return Response.ok(chatDetails);
    }

    @PostMapping(value = "/delete")
    @Operation(summary = "会话历史记录删除", description = "会话历史记录删除")
    public Response<Integer> historyDelete(@RequestParam(value = "chatId") @Parameter(description = "会话ID")  String chatId){
        int result = chatHistoryService.logicDeleteChatHistory(chatId);
        return result > 0 ? Response.ok(result, "删除成功") : Response.fail("删除失败");
    }

    @PostMapping(value = "/rename")
    @Operation(summary = "会话历史记录重命名", description = "会话历史记录重命名")
    public Response<Integer> historyRename(@RequestParam(value = "chatId") @Parameter(description = "会话ID")  String chatId,
                                          @RequestParam(value = "title") @Parameter(description = "命名")  String title){
        int result = chatHistoryService.updateChatTitle(chatId, title);

        return result > 0 ? Response.ok(result, "更新成功") : Response.fail("更新失败");
    }

}
