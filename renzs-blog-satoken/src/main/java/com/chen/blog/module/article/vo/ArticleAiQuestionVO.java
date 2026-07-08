package com.chen.blog.module.article.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 文章AI问答请求
 *
 * @author chenfuyun
 * @date 2026/07/07
 */
@Data
@ApiModel(description = "文章AI问答请求")
public class ArticleAiQuestionVO {

    /**
     * 读者围绕当前文章提出的问题
     */
    @NotBlank(message = "问题不能为空")
    @ApiModelProperty(name = "question", value = "问题", required = true, dataType = "String")
    private String question;
}
