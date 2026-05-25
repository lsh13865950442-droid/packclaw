package com.packclaw.model.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 模型配置实体类
 */
@Schema(description = "模型配置")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ModelConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID(固定为1)")
    private Integer id;

    @Schema(description = "厂商类型: DASHSCOPE/OPENAI/ANTHROPIC/CUSTOM")
    private String provider;

    @Schema(description = "API请求地址")
    private String apiUrl;

    @Schema(description = "模型ID")
    private String modelId;

    @Schema(description = "API密钥")
    private String apiKey;

    @Schema(description = "是否为当前激活的模型")
    private Boolean isActive;

    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    @Schema(description = "创建时间")
    private Date createdAt;

    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    @Schema(description = "更新时间")
    private Date updatedAt;
}
