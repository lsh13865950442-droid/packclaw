package com.packclaw.model.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Schema
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Session implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "id")
    private String id;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "ip地址")
    private String ip;

    @Schema(description = "是否删除")
    private Boolean status;

    @Schema(description = "原始问题")
    private String query;

    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date createTime;

    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date updateTime;
}
