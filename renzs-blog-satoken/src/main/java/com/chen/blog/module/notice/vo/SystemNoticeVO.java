package com.chen.blog.module.notice.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 系统通知
 *
 * @author chen
 * @date 2026/05/10
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "系统通知")
public class SystemNoticeVO {

    /**
     * 通知内容
     */
    @NotBlank(message = "通知内容不能为空")
    @Size(max = 500, message = "通知内容不能超过500个字符")
    @ApiModelProperty(name = "content", value = "通知内容", required = true, dataType = "String")
    private String content;

    /**
     * 跳转路径
     */
    @Size(max = 255, message = "跳转路径不能超过255个字符")
    @ApiModelProperty(name = "jumpPath", value = "跳转路径", dataType = "String")
    private String jumpPath;

}
