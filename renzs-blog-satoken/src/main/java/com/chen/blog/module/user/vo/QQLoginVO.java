package com.chen.blog.module.user.vo;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * QQ登录请求参数。
 *
 * @author chenfuyun
 * @date 2021/06/14
 * @since 1.0.0
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel(description = "QQ登录信息")
public class QQLoginVO {

    /**
     * QQ openId；当QQ只在token校验接口返回openId时，该字段允许为空。
     */
    @JsonAlias("openid")
    @ApiModelProperty(name = "openId", value = "qq openId", dataType = "String")
    private String openId;

    /**
     * QQ accessToken。
     */
    @JsonAlias("access_token")
    @NotBlank(message = "accessToken不能为空")
    @ApiModelProperty(name = "accessToken", value = "qq accessToken", required = true, dataType = "String")
    private String accessToken;

}
