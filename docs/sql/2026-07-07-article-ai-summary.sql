-- 文章AI总结字段；如果当前表已经包含这些字段，无需重复执行。
ALTER TABLE `tb_article`
    ADD COLUMN `ai_summary` TEXT NULL COMMENT 'AI文章总结' AFTER `article_content`,
    ADD COLUMN `ai_summary_status` TINYINT DEFAULT 0 NULL COMMENT 'AI总结状态 0未生成 1已生成 2已审核' AFTER `ai_summary`,
    ADD COLUMN `ai_summary_time` DATETIME NULL COMMENT 'AI总结生成时间' AFTER `ai_summary_status`;
