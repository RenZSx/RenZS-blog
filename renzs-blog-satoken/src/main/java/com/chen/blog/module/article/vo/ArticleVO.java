package com.chen.blog.module.article.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.NotBlank;
import java.util.List;


/**
 * 文章
 *
 * @author chenfuyun
 * @date 2021/08/03
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "文章")
public class ArticleVO {

    /**
     * 文章id
     */
    @ApiModelProperty(name = "id", value = "文章id", dataType = "Integer")
    private Integer id;

    /**
     * 标题
     */
    @NotBlank(message = "文章标题不能为空")
    @ApiModelProperty(name = "articleTitle", value = "文章标题", required = true, dataType = "String")
    private String articleTitle;

    /**
     * 内容
     */
    @NotBlank(message = "文章内容不能为空")
    @ApiModelProperty(name = "articleContent", value = "文章内容", required = true, dataType = "String")
    private String articleContent;

    /**
     * AI文章总结
     */
    @ApiModelProperty(name = "aiSummary", value = "AI文章总结", dataType = "String")
    private String aiSummary;

    /**
     * AI总结状态 0未生成 1已生成 2已审核
     */
    @ApiModelProperty(name = "aiSummaryStatus", value = "AI总结状态", dataType = "Integer")
    private Integer aiSummaryStatus;

    /**
     * AI总结生成时间
     */
    @ApiModelProperty(name = "aiSummaryTime", value = "AI总结生成时间", dataType = "LocalDateTime")
    private java.time.LocalDateTime aiSummaryTime;

    /**
     * SEO标题
     */
    @ApiModelProperty(name = "seoTitle", value = "SEO标题", dataType = "String")
    private String seoTitle;

    /**
     * SEO描述
     */
    @ApiModelProperty(name = "seoDescription", value = "SEO描述", dataType = "String")
    private String seoDescription;

    /**
     * SEO关键词
     */
    @ApiModelProperty(name = "seoKeywords", value = "SEO关键词", dataType = "String")
    private String seoKeywords;

    /**
     * Open Graph分享描述
     */
    @ApiModelProperty(name = "seoOgDescription", value = "Open Graph分享描述", dataType = "String")
    private String seoOgDescription;

    /**
     * 文章封面
     */
    @ApiModelProperty(name = "articleCover", value = "文章缩略图", dataType = "String")
    private String articleCover;

    /**
     * 文章分类
     */
    @ApiModelProperty(name = "category", value = "文章分类", dataType = "Integer")
    private String categoryName;

    /**
     * 文章标签
     */
    @ApiModelProperty(name = "tagNameList", value = "文章标签", dataType = "List<Integer>")
    private List<String> tagNameList;

    /**
     * 文章类型
     */
    @ApiModelProperty(name = "type", value = "文章类型", dataType = "Integer")
    private Integer type;

    /**
     * 原文链接
     */
    @ApiModelProperty(name = "originalUrl", value = "原文链接", dataType = "String")
    private String originalUrl;

    /**
     * 是否置顶
     */
    @ApiModelProperty(name = "isTop", value = "是否置顶", dataType = "Integer")
    private Integer isTop;

    /**
     * 文章状态 1.公开 2.私密 3.评论可见
     */
    @ApiModelProperty(name = "status", value = "文章状态", dataType = "String")
    private Integer status;

}
