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
 * 技能配置实体类
 */
@Schema(description = "技能配置")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SkillConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Integer id;

    @Schema(description = "技能名称")
    private String skillName;

    @Schema(description = "技能描述")
    private String description;

    @Schema(description = "技能路径(本地路径)")
    private String skillPath;

    @Schema(description = "是否启用")
    private Boolean isEnabled;

    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    @Schema(description = "创建时间")
    private Date createdAt;

    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    @Schema(description = "更新时间")
    private Date updatedAt;
}
