package com.chen.blog.module.friendLink.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * 功能说明: 友链审核请求对象。
 * 作者: OpenAI Codex
 * 创建时间: 2026-07-05
 * 用途概述: 后台审核友链申请时只允许提交记录 ID 和审核状态。
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "友链审核")
public class FriendLinkReviewVO {

    /**
     * 友链申请 ID。
     */
    @NotNull(message = "友链id不能为空")
    @ApiModelProperty(name = "id", value = "友链id", dataType = "Integer", required = true)
    private Integer id;

    /**
     * 审核状态: 1=通过 2=拒绝。
     */
    @NotNull(message = "审核状态不能为空")
    @ApiModelProperty(name = "linkStatus", value = "审核状态", dataType = "Integer", required = true)
    private Integer linkStatus;
}
