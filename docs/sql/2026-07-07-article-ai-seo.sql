-- 功能说明: 为文章表增加 AI SEO 元信息字段。
-- 作者: chenfuyun
-- 创建时间: 2026-07-07
-- 用途概述: 支持后台 AI 生成 SEO 标题、描述、关键词和社交分享描述，前台文章详情页读取展示。
ALTER TABLE `tb_article`
    ADD COLUMN `seo_title` varchar(100) NULL COMMENT 'SEO标题' AFTER `ai_summary_time`,
    ADD COLUMN `seo_description` varchar(255) NULL COMMENT 'SEO描述' AFTER `seo_title`,
    ADD COLUMN `seo_keywords` varchar(255) NULL COMMENT 'SEO关键词' AFTER `seo_description`,
    ADD COLUMN `seo_og_description` varchar(255) NULL COMMENT 'Open Graph分享描述' AFTER `seo_keywords`;
