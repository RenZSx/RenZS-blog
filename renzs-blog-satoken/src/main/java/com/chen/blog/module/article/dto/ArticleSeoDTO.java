package com.chen.blog.module.article.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI文章SEO信息
 *
 * @author chenfuyun
 * @date 2026/07/07
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "AI文章SEO信息")
public class ArticleSeoDTO {

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
}
