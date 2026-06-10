package com.chen.blog.module.love.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * 飞书传信后台信件保存请求对象。
 *
 * 用于后台页面提交信件标题和正文。
 *
 * @author Chen
 * @date 2026/05/17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "Love letter admin request")
public class LoveLetterVO {

    /**
     * 可编辑的信件标题。
     */
    @NotNull(message = "letterTitle 不能为空")
    @ApiModelProperty(name = "letterTitle", value = "信件标题", required = true, dataType = "String")
    private String letterTitle;

    /**
     * 可编辑的信件正文。
     */
    @NotNull(message = "letterContent 不能为空")
    @ApiModelProperty(name = "letterContent", value = "信件正文", required = true, dataType = "String")
    private String letterContent;

}
