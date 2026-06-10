package com.chen.blog.module.love.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 纪念页前台配置响应对象。
 *
 * 用于返回纪念页的基础信息，供前台页面直接渲染。
 *
 * @author Chen
 * @date 2026/05/17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "纪念页前台配置")
public class LoveConfigDTO {

    /**
     * 页面标题。
     */
    @ApiModelProperty(name = "title", value = "页面标题", dataType = "String")
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(name = "startTime", value = "开始时间", dataType = "LocalDateTime")
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
