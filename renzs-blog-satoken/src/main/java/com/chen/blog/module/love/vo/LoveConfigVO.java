package com.chen.blog.module.love.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 纪念页后台配置请求对象。
 *
 * 用于后台保存纪念页的基础信息。
 *
 * @author Chen
 * @date 2026/05/17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "纪念页后台配置")
public class LoveConfigVO {

    /**
     * 页面标题。
     */
    @NotNull(message = "title 不能为空")
    @ApiModelProperty(name = "title", value = "页面标题", required = true, dataType = "String")
    private String title;

    /**
     * 页面副标题。
     */
    @ApiModelProperty(name = "subtitle", value = "页面副标题", dataType = "String")
    private String subtitle;

    /**
     * 背景图地址。
     */
    @ApiModelProperty(name = "background", value = "背景图地址", dataType = "String")
    private String background;

    /**
     * 开始时间。
     */
    @NotNull(message = "startTime 不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(name = "startTime", value = "开始时间", required = true, dataType = "LocalDateTime")
    private LocalDateTime startTime;

    /**
     * 纪念日时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(name = "anniversaryTime", value = "纪念日时间", dataType = "LocalDateTime")
    private LocalDateTime anniversaryTime;

    /**
     * 纪念日标题。
     */
    @ApiModelProperty(name = "anniversaryTitle", value = "纪念日标题", dataType = "String")
    private String anniversaryTitle;

    /**
     * 是否启用。
     */
    @ApiModelProperty(name = "isEnabled", value = "是否启用", dataType = "Integer")
    private Integer isEnabled;

}
