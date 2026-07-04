package com.chen.blog.module.friendLink.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * 友链VO
 *
 * @author chenfuyun
 * @date 2021/07/28
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "友链")
public class FriendLinkVO {
    /**
     * id
     */
    @ApiModelProperty(name = "categoryId", value = "友链id", dataType = "Integer")
    private Integer id;

    /**
     * 链接名
     */
    @NotBlank(message = "链接名不能为空")
    @ApiModelProperty(name = "linkName", value = "友链名", dataType = "String", required = true)
    private String linkName;

    /**
     * 链接封面
     */
    @NotBlank(message = "链接封面不能为空")
    @ApiModelProperty(name = "linkCover", value = "友链封面", dataType = "String", required = true)
    private String linkCover;

    /**
     * 链接地址
     */
    @NotBlank(message = "链接地址不能为空")
    @ApiModelProperty(name = "linkAddress", value = "友链地址", dataType = "String", required = true)
    private String linkAddress;

    /**
     * 介绍
     */
    @NotBlank(message = "链接介绍不能为空")
    @ApiModelProperty(name = "linkIntro", value = "友链介绍", dataType = "String", required = true)
    private String linkIntro;

    /**
     * 审核状态 0=待审核 1=已通过 2=已拒绝
     */
    @ApiModelProperty(name = "linkStatus", value = "审核状态", dataType = "Integer")
    private Integer linkStatus;

}
