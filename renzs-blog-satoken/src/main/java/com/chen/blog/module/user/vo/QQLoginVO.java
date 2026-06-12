package com.chen.blog.module.user.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * QQ login payload.
 *
 * @author chenfuyun
 * @date 2021/06/14
 * @since 1.0.0
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel(description = "QQ login information")
public class QQLoginVO {

    /**
     * QQ openId, optional when QQ only returns it from token validation.
     */
    @ApiModelProperty(name = "openId", value = "qq openId", dataType = "String")
    private String openId;

    /**
     * QQ accessToken.
     */
    @NotBlank(message = "accessToken cannot be blank")
    @ApiModelProperty(name = "accessToken", value = "qq accessToken", required = true, dataType = "String")
    private String accessToken;

}
