-- 功能说明: 为友链表增加审核状态字段。
-- 作者: OpenAI Codex
-- 创建时间: 2026-07-05
-- 用途概述: 支持前台提交友链申请、后台审核后再公开展示。

ALTER TABLE `tb_friend_link`
  ADD COLUMN `link_status` tinyint NOT NULL DEFAULT 1 COMMENT '审核状态 0=待审核 1=已通过 2=已拒绝' AFTER `link_intro`;
