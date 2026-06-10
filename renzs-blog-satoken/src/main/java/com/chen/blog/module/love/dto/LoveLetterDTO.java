package com.chen.blog.module.love.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 飞书传信前台信件响应对象。
 *
 * 用于向前台页面返回信件标题和正文。
 *
 * @author Chen
 * @date 2026/05/17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "Love letter response")
public class LoveLetterDTO {

    /**
     * 前台展示的信件标题。
     */
    @ApiModelProperty(name = "letterTitle", value = "信件标题", dataType = "String")
    private String letterTitle;

    /**
     * 前台展示的信件正文。
     */
    @ApiModelProperty(name = "letterContent", value = "信件正文", dataType = "String")
    private String letterContent;

}
