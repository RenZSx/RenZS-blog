-- 功能说明: 将友链头像字段改为友链封面字段。
-- 作者: OpenAI Codex
-- 创建时间: 2026-07-05
-- 用途概述: 友链卡片和申请表使用网站封面图，不再使用头像字段。

ALTER TABLE `tb_friend_link`
  CHANGE COLUMN `link_avatar` `link_cover` varchar(255) NOT NULL COMMENT '网站封面';
