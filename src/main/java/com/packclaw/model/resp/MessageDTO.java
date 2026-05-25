package com.packclaw.model.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 消息 DTO，用于序列化/反序列化 JSONL 中的消息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String type;
    private String id;
    private double timestamp;
    private String role;
    
    /**
     * 兼容两种格式：
     * 1. 字符串（JSONL 原始格式）
     * 2. 数组（AgentScope Msg 格式）
     */
    private Object content;

}
