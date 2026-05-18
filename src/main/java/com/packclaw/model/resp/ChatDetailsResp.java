package com.packclaw.model.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.agentscope.core.message.Msg;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Chat details response
 */
@Schema(description = "Chat details response")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatDetailsResp implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Chat ID")
    private String chatId;

    @Schema(description = "Session title")
    private String title;

    @Schema(description = "Created at")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date createTime;

    @Schema(description = "Updated at")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date updateTime;

    @Schema(description = "Chat messages")
    private List<Msg> messages;
}
