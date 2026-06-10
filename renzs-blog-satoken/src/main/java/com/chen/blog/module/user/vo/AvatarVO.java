package com.chen.blog.module.user.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * 用户头像链接
 *
 * @author chen
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "用户头像链接")
public class AvatarVO {

    /**
     * 头像链接
     */
    @NotBlank(message = "头像链接不能为空")
    @ApiModelProperty(name = "avatar", value = "头像链接", required = true, dataType = "String")
    private String avatar;
}
