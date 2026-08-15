package com.chen.blog.module.user.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 管理端添加用户请求对象
 *
 * @author RenZS Blog
 * @date 2026/01/01
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "管理端添加用户")
public class AdminUserVO {

    /**
     * 用户名(登录账号)
     */
    @NotBlank(message = "用户名不能为空")
    @ApiModelProperty(name = "username", value = "用户名", required = true, dataType = "String")
    private String username;

    /**
     * 密码
     */
    @Size(min = 6, message = "密码不能少于6位")
    @NotBlank(message = "密码不能为空")
    @ApiModelProperty(name = "password", value = "密码", required = true, dataType = "String")
    private String password;

    /**
     * 昵称
     */
    @ApiModelProperty(name = "nickname", value = "昵称", dataType = "String")
    private String nickname;

    /**
     * 角色id列表
     */
    @ApiModelProperty(name = "roleIdList", value = "角色id列表", dataType = "List<Integer>")
    private List<Integer> roleIdList;

}
