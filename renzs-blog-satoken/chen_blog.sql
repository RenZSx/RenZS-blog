/*
 Navicat Premium Dump SQL

 Source Server         : .
 Source Server Type    : MySQL
 Source Server Version : 80043 (8.0.43)
 Source Host           : localhost:3306
 Source Schema         : chen-blog

 Target Server Type    : MySQL
 Target Server Version : 80043 (8.0.43)
 File Encoding         : 65001

 Date: 13/08/2026 14:55:41
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for tb_article
-- ----------------------------
DROP TABLE IF EXISTS `tb_article`;
CREATE TABLE `tb_article`  (
                               `id` int NOT NULL AUTO_INCREMENT,
                               `user_id` int NOT NULL COMMENT '作者',
                               `category_id` int NULL DEFAULT NULL COMMENT '文章分类',
                               `article_cover` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文章缩略图',
                               `article_title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标题',
                               `article_content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '内容',
                               `type` tinyint(1) NOT NULL DEFAULT 0 COMMENT '文章类型 1原创 2转载 3翻译',
                               `original_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '原文链接',
                               `is_top` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否置顶 0否 1是',
                               `is_delete` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除  0否 1是',
                               `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '状态值 1公开 2私密 3评论可见 4推荐',
                               `create_time` datetime NOT NULL COMMENT '发表时间',
                               `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
                               `ai_summary` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT 'AI文章总结',
                               `ai_summary_status` tinyint NULL DEFAULT 0 COMMENT 'AI总结状态 0未生成 1已生成 2已审核',
                               `ai_summary_time` datetime NULL DEFAULT NULL COMMENT 'AI总结生成时间',
                               `seo_title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'SEO标题',
                               `seo_description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'SEO描述',
                               `seo_keywords` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'SEO关键词',
                               `seo_og_description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Open Graph分享描述',
                               PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 92 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tb_article
-- ----------------------------

-- ----------------------------
-- Table structure for tb_article_tag
-- ----------------------------
DROP TABLE IF EXISTS `tb_article_tag`;
CREATE TABLE `tb_article_tag`  (
                                   `id` int NOT NULL AUTO_INCREMENT,
                                   `article_id` int NOT NULL COMMENT '文章id',
                                   `tag_id` int NOT NULL COMMENT '标签id',
                                   PRIMARY KEY (`id`) USING BTREE,
                                   INDEX `fk_article_tag_1`(`article_id` ASC) USING BTREE,
                                   INDEX `fk_article_tag_2`(`tag_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1221 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tb_article_tag
-- ----------------------------

-- ----------------------------
-- Table structure for tb_category
-- ----------------------------
DROP TABLE IF EXISTS `tb_category`;
CREATE TABLE `tb_category`  (
                                `id` int NOT NULL AUTO_INCREMENT,
                                `category_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分类名',
                                `create_time` datetime NOT NULL COMMENT '创建时间',
                                `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
                                PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 197 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tb_category
-- ----------------------------

-- ----------------------------
-- Table structure for tb_chat_record
-- ----------------------------
DROP TABLE IF EXISTS `tb_chat_record`;
CREATE TABLE `tb_chat_record`  (
                                   `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
                                   `user_id` int NULL DEFAULT NULL COMMENT '用户id',
                                   `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '昵称',
                                   `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '头像',
                                   `content` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '聊天内容',
                                   `ip_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'ip地址',
                                   `ip_source` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'ip来源',
                                   `type` tinyint NOT NULL COMMENT '类型',
                                   `create_time` datetime NOT NULL COMMENT '创建时间',
                                   `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
                                   PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3007 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tb_chat_record
-- ----------------------------

-- ----------------------------
-- Table structure for tb_chat_room_mute
-- ----------------------------
DROP TABLE IF EXISTS `tb_chat_room_mute`;
CREATE TABLE `tb_chat_room_mute`  (
                                      `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
                                      `user_id` bigint NOT NULL COMMENT '用户ID',
                                      `mute_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '禁言原因',
                                      `start_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
                                      `end_time` datetime NULL DEFAULT NULL COMMENT '结束时间',
                                      `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态 0失效 1生效',
                                      `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                      PRIMARY KEY (`id`) USING BTREE,
                                      INDEX `idx_user_id_status`(`user_id` ASC, `status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '聊天室禁言表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_chat_room_mute
-- ----------------------------

-- ----------------------------
-- Table structure for tb_comment
-- ----------------------------
DROP TABLE IF EXISTS `tb_comment`;
CREATE TABLE `tb_comment`  (
                               `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
                               `user_id` int NOT NULL COMMENT '评论用户Id',
                               `topic_id` int NULL DEFAULT NULL COMMENT '评论主题id',
                               `comment_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '评论内容',
                               `reply_user_id` int NULL DEFAULT NULL COMMENT '回复用户id',
                               `parent_id` int NULL DEFAULT NULL COMMENT '父评论id',
                               `type` tinyint NOT NULL COMMENT '评论类型 1.文章 2.友链 3.说说',
                               `is_delete` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除  0否 1是',
                               `is_review` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否审核',
                               `create_time` datetime NOT NULL COMMENT '评论时间',
                               `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
                               PRIMARY KEY (`id`) USING BTREE,
                               INDEX `fk_comment_user`(`user_id` ASC) USING BTREE,
                               INDEX `fk_comment_parent`(`parent_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 756 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tb_comment
-- ----------------------------

-- ----------------------------
-- Table structure for tb_friend_link
-- ----------------------------
DROP TABLE IF EXISTS `tb_friend_link`;
CREATE TABLE `tb_friend_link`  (
                                   `id` int NOT NULL AUTO_INCREMENT,
                                   `link_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '链接名',
                                   `link_cover` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '网站封面',
                                   `link_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '链接地址',
                                   `link_intro` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '链接介绍',
                                   `link_status` tinyint NOT NULL DEFAULT 1 COMMENT '审核状态 0=待审核 1=已通过 2=已拒绝',
                                   `create_time` datetime NOT NULL COMMENT '创建时间',
                                   `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
                                   PRIMARY KEY (`id`) USING BTREE,
                                   INDEX `fk_friend_link_user`(`link_name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 40 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tb_friend_link
-- ----------------------------

-- ----------------------------
-- Table structure for tb_love_config
-- ----------------------------
DROP TABLE IF EXISTS `tb_love_config`;
CREATE TABLE `tb_love_config`  (
                                   `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
                                   `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '页面标题',
                                   `subtitle` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '页面副标题',
                                   `background` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '背景图地址',
                                   `start_time` datetime NOT NULL COMMENT '开始时间',
                                   `anniversary_time` datetime NULL DEFAULT NULL COMMENT '纪念日时间',
                                   `anniversary_title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '纪念日标题',
                                   `is_enabled` tinyint NOT NULL DEFAULT 1 COMMENT '是否启用 0否 1是',
                                   `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
                                   `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
                                   PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '纪念页基础配置' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_love_config
-- ----------------------------

-- ----------------------------
-- Table structure for tb_love_letter
-- ----------------------------
DROP TABLE IF EXISTS `tb_love_letter`;
CREATE TABLE `tb_love_letter`  (
                                   `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
                                   `letter_title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '信件标题',
                                   `letter_content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '信件正文',
                                   `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
                                   `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
                                   PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '飞书传信正文' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_love_letter
-- ----------------------------

-- ----------------------------
-- Table structure for tb_mail_subscribe
-- ----------------------------
DROP TABLE IF EXISTS `tb_mail_subscribe`;
CREATE TABLE `tb_mail_subscribe`  (
                                      `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
                                      `email` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '邮箱',
                                      `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态 0退订 1订阅中',
                                      `subscribe_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'article' COMMENT '订阅类型',
                                      `token` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '确认或退订token',
                                      `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                      PRIMARY KEY (`id`) USING BTREE,
                                      UNIQUE INDEX `uk_email_type`(`email` ASC, `subscribe_type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '邮件订阅表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_mail_subscribe
-- ----------------------------

-- ----------------------------
-- Table structure for tb_menu
-- ----------------------------
DROP TABLE IF EXISTS `tb_menu`;
CREATE TABLE `tb_menu`  (
                            `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
                            `name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '菜单名',
                            `path` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '菜单路径',
                            `component` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '组件',
                            `icon` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '菜单icon',
                            `create_time` datetime NOT NULL COMMENT '创建时间',
                            `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
                            `order_num` tinyint(1) NOT NULL COMMENT '排序',
                            `parent_id` int NULL DEFAULT NULL COMMENT '父id',
                            `is_hidden` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否隐藏  0否1是',
                            PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 222 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tb_menu
-- ----------------------------
INSERT INTO `tb_menu` VALUES (1, '首页', '/', '/home/Home.vue', 'el-icon-myshouye', '2021-01-26 17:06:51', '2021-01-26 17:06:53', 1, NULL, 0);
INSERT INTO `tb_menu` VALUES (2, '文章管理', '/article-submenu', 'Layout', 'el-icon-mywenzhang-copy', '2021-01-25 20:43:07', '2021-01-25 20:43:09', 2, NULL, 0);
INSERT INTO `tb_menu` VALUES (3, '消息管理', '/message-submenu', 'Layout', 'el-icon-myxiaoxi', '2021-01-25 20:44:17', '2021-01-25 20:44:20', 3, NULL, 0);
INSERT INTO `tb_menu` VALUES (4, '系统管理', '/system-submenu', 'Layout', 'el-icon-myshezhi', '2021-01-25 20:45:57', '2021-01-25 20:45:59', 5, NULL, 0);
INSERT INTO `tb_menu` VALUES (5, '个人中心', '/setting', '/setting/Setting.vue', 'el-icon-myuser', '2021-01-26 17:22:38', '2021-01-26 17:22:41', 7, NULL, 0);
INSERT INTO `tb_menu` VALUES (6, '发布文章', '/articles', '/article/Article.vue', 'el-icon-myfabiaowenzhang', '2021-01-26 14:30:48', '2021-01-26 14:30:51', 1, 2, 0);
INSERT INTO `tb_menu` VALUES (7, '修改文章', '/articles/*', '/article/Article.vue', 'el-icon-myfabiaowenzhang', '2021-01-26 14:31:32', '2025-05-23 17:59:57', 2, 2, 1);
INSERT INTO `tb_menu` VALUES (8, '文章列表', '/article-list', '/article/ArticleList.vue', 'el-icon-mywenzhangliebiao', '2021-01-26 14:32:13', '2021-01-26 14:32:16', 3, 2, 0);
INSERT INTO `tb_menu` VALUES (9, '分类管理', '/categories', '/category/Category.vue', 'el-icon-myfenlei', '2021-01-26 14:33:42', '2021-01-26 14:33:43', 4, 2, 0);
INSERT INTO `tb_menu` VALUES (10, '标签管理', '/tags', '/tag/Tag.vue', 'el-icon-myicontag', '2021-01-26 14:34:33', '2021-01-26 14:34:36', 5, 2, 0);
INSERT INTO `tb_menu` VALUES (11, '评论管理', '/comments', '/comment/Comment.vue', 'el-icon-mypinglunzu', '2021-01-26 14:35:31', '2021-01-26 14:35:34', 1, 3, 0);
INSERT INTO `tb_menu` VALUES (12, '留言管理', '/messages', '/message/Message.vue', 'el-icon-myliuyan', '2021-01-26 14:36:09', '2021-01-26 14:36:13', 2, 3, 0);
INSERT INTO `tb_menu` VALUES (13, '用户列表', '/users', '/user/User.vue', 'el-icon-myyonghuliebiao', '2021-01-26 14:38:09', '2021-01-26 14:38:12', 1, 202, 0);
INSERT INTO `tb_menu` VALUES (14, '角色管理', '/roles', '/role/Role.vue', 'el-icon-myjiaoseliebiao', '2021-01-26 14:39:01', '2021-01-26 14:39:03', 2, 213, 0);
INSERT INTO `tb_menu` VALUES (15, '接口管理', '/resources', '/resource/Resource.vue', 'el-icon-myjiekouguanli', '2021-01-26 14:40:14', '2021-08-07 20:00:28', 2, 213, 0);
INSERT INTO `tb_menu` VALUES (16, '菜单管理', '/menus', '/menu/Menu.vue', 'el-icon-mycaidan', '2021-01-26 14:40:54', '2021-08-07 10:18:49', 2, 213, 0);
INSERT INTO `tb_menu` VALUES (17, '友链管理', '/links', '/friendLink/FriendLink.vue', 'el-icon-mydashujukeshihuaico-', '2021-01-26 14:41:35', '2021-01-26 14:41:37', 3, 4, 0);
INSERT INTO `tb_menu` VALUES (18, '关于我', '/about', '/about/About.vue', 'el-icon-myguanyuwo', '2021-01-26 14:42:05', '2021-01-26 14:42:10', 4, 4, 0);
INSERT INTO `tb_menu` VALUES (19, '日志管理', '/log-submenu', 'Layout', 'el-icon-myguanyuwo', '2021-01-31 21:33:56', '2021-01-31 21:33:59', 6, NULL, 0);
INSERT INTO `tb_menu` VALUES (20, '操作日志', '/operation/log', '/log/Operation.vue', 'el-icon-myguanyuwo', '2021-01-31 15:53:21', '2021-01-31 15:53:25', 1, 19, 0);
INSERT INTO `tb_menu` VALUES (201, '在线用户', '/online/users', '/user/Online.vue', 'el-icon-myyonghuliebiao', '2021-02-05 14:59:51', '2021-02-05 14:59:53', 7, 202, 0);
INSERT INTO `tb_menu` VALUES (202, '用户管理', '/users-submenu', 'Layout', 'el-icon-myyonghuliebiao', '2021-02-06 23:44:59', '2021-02-06 23:45:03', 4, NULL, 0);
INSERT INTO `tb_menu` VALUES (205, '相册管理', '/album-submenu', 'Layout', 'el-icon-myimage-fill', '2021-08-03 15:10:54', '2021-08-07 20:02:06', 5, NULL, 0);
INSERT INTO `tb_menu` VALUES (206, '相册列表', '/albums', '/album/Album.vue', 'el-icon-myzhaopian', '2021-08-03 20:29:19', '2021-08-04 11:45:47', 1, 205, 0);
INSERT INTO `tb_menu` VALUES (208, '照片管理', '/albums/:albumId', '/album/Photo.vue', 'el-icon-myzhaopian', '2021-08-03 21:37:47', '2024-07-23 21:48:00', 1, 205, 1);
INSERT INTO `tb_menu` VALUES (209, '页面管理', '/pages', '/page/Page.vue', 'el-icon-myyemianpeizhi', '2021-08-04 11:36:27', '2021-08-07 20:01:26', 2, 4, 0);
INSERT INTO `tb_menu` VALUES (210, '照片回收站', '/photos/delete', '/album/Delete.vue', 'el-icon-myhuishouzhan', '2021-08-05 13:55:19', NULL, 3, 205, 0);
INSERT INTO `tb_menu` VALUES (213, '权限管理', '/permission-submenu', 'Layout', 'el-icon-mydaohanglantubiao_quanxianguanli', '2021-08-07 19:56:55', '2021-08-07 19:59:40', 4, NULL, 0);
INSERT INTO `tb_menu` VALUES (214, '网站管理', '/website', '/website/Website.vue', 'el-icon-myxitong', '2021-08-07 20:06:41', NULL, 1, 4, 0);
INSERT INTO `tb_menu` VALUES (215, '说说管理', '/talk-submenu', 'Layout', 'el-icon-mypinglun', '2022-01-23 20:17:59', '2022-01-23 20:38:06', 5, NULL, 0);
INSERT INTO `tb_menu` VALUES (216, '发布说说', '/talks', '/talk/Talk.vue', 'el-icon-myfabusekuai', '2022-01-23 20:18:43', '2022-01-23 20:38:19', 1, 215, 0);
INSERT INTO `tb_menu` VALUES (217, '说说列表', '/talk-list', '/talk/TalkList.vue', 'el-icon-myiconfontdongtaidianji', '2022-01-23 23:28:24', '2022-01-24 00:02:48', 2, 215, 0);
INSERT INTO `tb_menu` VALUES (218, '修改说说', '/talks/:talkId', '/talk/Talk.vue', 'el-icon-myshouye', '2022-01-24 00:10:44', NULL, 3, 215, 1);
INSERT INTO `tb_menu` VALUES (219, '发布通知', '/system-notice', '/notice/SystemNotice.vue', 'el-icon-myliuyan', '2026-05-10 15:12:42', '2026-05-10 15:28:39', 3, 3, 0);
INSERT INTO `tb_menu` VALUES (221, '纪念页管理', '/love', '/love/Love.vue', 'el-icon-myxiaoxi', '2026-05-17 23:55:16', '2026-05-18 00:03:24', 5, 4, 0);

-- ----------------------------
-- Table structure for tb_message
-- ----------------------------
DROP TABLE IF EXISTS `tb_message`;
CREATE TABLE `tb_message`  (
                               `id` int NOT NULL AUTO_INCREMENT COMMENT '主键id',
                               `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '昵称',
                               `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '头像',
                               `message_content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '留言内容',
                               `ip_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户ip',
                               `ip_source` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户地址',
                               `time` tinyint(1) NULL DEFAULT NULL COMMENT '弹幕速度',
                               `is_review` tinyint NOT NULL DEFAULT 1 COMMENT '是否审核 0需要审核 1不需要审核',
                               `create_time` datetime NOT NULL COMMENT '发布时间',
                               `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
                               PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 359277 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tb_message
-- ----------------------------

-- ----------------------------
-- Table structure for tb_operation_log
-- ----------------------------
DROP TABLE IF EXISTS `tb_operation_log`;
CREATE TABLE `tb_operation_log`  (
                                     `id` int NOT NULL AUTO_INCREMENT COMMENT '主键id',
                                     `opt_module` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '操作模块',
                                     `opt_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '操作类型',
                                     `opt_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '操作url',
                                     `opt_method` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '操作方法',
                                     `opt_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '操作描述',
                                     `request_param` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '请求参数',
                                     `request_method` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '请求方式',
                                     `response_data` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '返回数据',
                                     `user_id` int NOT NULL COMMENT '用户id',
                                     `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户昵称',
                                     `ip_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '操作ip',
                                     `ip_source` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '操作地址',
                                     `create_time` datetime NOT NULL COMMENT '创建时间',
                                     `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
                                     PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1727 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tb_operation_log
-- ----------------------------

-- ----------------------------
-- Table structure for tb_page
-- ----------------------------
DROP TABLE IF EXISTS `tb_page`;
CREATE TABLE `tb_page`  (
                            `id` int NOT NULL AUTO_INCREMENT COMMENT '页面id',
                            `page_name` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '页面名',
                            `page_label` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '页面标签',
                            `page_cover` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '页面封面',
                            `create_time` datetime NOT NULL COMMENT '创建时间',
                            `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
                            PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 908 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '页面' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tb_page
-- ----------------------------
INSERT INTO `tb_page` VALUES (2, '归档', 'archive', 'https://static.renzs.top/config/20fbdb59aa1f67af7fcf0cf5e3c4b6b8.jpg', '2021-08-07 10:32:36', '2024-05-25 20:54:13');
INSERT INTO `tb_page` VALUES (3, '分类', 'category', 'https://static.renzs.top/config/7e39f5da9acc5258d768419bd6965e8a.jpg', '2021-08-07 10:32:36', '2024-05-25 20:53:58');
INSERT INTO `tb_page` VALUES (4, '标签', 'tag', 'https://static.renzs.top/config/20fbdb59aa1f67af7fcf0cf5e3c4b6b8.jpg', '2021-08-07 10:32:36', '2024-05-25 20:54:20');
INSERT INTO `tb_page` VALUES (5, '相册', 'album', 'https://static.renzs.top/config/48dc486b30f704613b58949918a76900.PNG', '2021-08-07 10:32:36', '2024-08-07 20:44:38');
INSERT INTO `tb_page` VALUES (6, '友链', 'link', 'https://static.renzs.top/config/34129cbddd863b6ee21456eecec0b2bc.jpeg', '2021-08-07 10:32:36', '2024-05-25 21:06:56');
INSERT INTO `tb_page` VALUES (7, '关于', 'about', 'https://static.renzs.top/config/9b6d39c0161b44058d43659f84f802f1.PNG', '2021-08-07 10:32:36', '2024-08-07 20:40:04');
INSERT INTO `tb_page` VALUES (9, '个人中心', 'user', 'https://static.renzs.top/config/bf19a0d18f0224b53a687c3e8b69aab4.jpeg', '2021-08-07 10:32:36', '2024-05-25 20:58:30');
INSERT INTO `tb_page` VALUES (10, '文章列表', 'articleList', 'https://static.renzs.top/config/0436350f03b89c7f6de82fac00c2265d.jpeg', '2021-08-10 15:36:19', '2024-06-01 22:44:01');
INSERT INTO `tb_page` VALUES (904, '说说', 'talk', 'https://static.renzs.top/config/7e39f5da9acc5258d768419bd6965e8a.jpg', '2022-01-23 00:51:24', '2024-05-25 21:06:29');
INSERT INTO `tb_page` VALUES (906, '留言', 'message', 'https://static.renzs.top/config/fc3173b30d5620ca8afc9170491b3948.jpeg', '2024-05-15 21:31:21', '2024-06-05 21:57:41');
INSERT INTO `tb_page` VALUES (907, '首页', 'home', 'https://static.renzs.top/config/9b6d39c0161b44058d43659f84f802f1.PNG', '2024-05-16 23:41:50', '2024-08-07 20:41:41');

-- ----------------------------
-- Table structure for tb_photo
-- ----------------------------
DROP TABLE IF EXISTS `tb_photo`;
CREATE TABLE `tb_photo`  (
                             `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
                             `album_id` int NOT NULL COMMENT '相册id',
                             `photo_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '照片名',
                             `photo_desc` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '照片描述',
                             `photo_src` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '照片地址',
                             `is_delete` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除',
                             `create_time` datetime NOT NULL COMMENT '创建时间',
                             `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
                             PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 82 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '照片' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tb_photo
-- ----------------------------

-- ----------------------------
-- Table structure for tb_photo_album
-- ----------------------------
DROP TABLE IF EXISTS `tb_photo_album`;
CREATE TABLE `tb_photo_album`  (
                                   `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
                                   `album_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '相册名',
                                   `album_desc` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '相册描述',
                                   `album_cover` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '相册封面',
                                   `is_delete` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除',
                                   `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '状态值 1公开 2私密',
                                   `create_time` datetime NOT NULL COMMENT '创建时间',
                                   `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
                                   PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '相册' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tb_photo_album
-- ----------------------------

-- ----------------------------
-- Table structure for tb_resource
-- ----------------------------
DROP TABLE IF EXISTS `tb_resource`;
CREATE TABLE `tb_resource`  (
                                `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
                                `resource_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '资源名',
                                `url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '权限路径',
                                `request_method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求方式',
                                `parent_id` int NULL DEFAULT NULL COMMENT '父权限id',
                                `is_anonymous` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否匿名访问 0否 1是',
                                `create_time` datetime NOT NULL COMMENT '创建时间',
                                `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
                                PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 297 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tb_resource
-- ----------------------------
INSERT INTO `tb_resource` VALUES (165, '分类模块', NULL, NULL, NULL, 0, '2021-08-11 21:04:21', NULL);
INSERT INTO `tb_resource` VALUES (166, '博客信息模块', NULL, NULL, NULL, 0, '2021-08-11 21:04:21', NULL);
INSERT INTO `tb_resource` VALUES (167, '友链模块', NULL, NULL, NULL, 0, '2021-08-11 21:04:21', NULL);
INSERT INTO `tb_resource` VALUES (168, '文章模块', NULL, NULL, NULL, 0, '2021-08-11 21:04:21', NULL);
INSERT INTO `tb_resource` VALUES (169, '日志模块', NULL, NULL, NULL, 0, '2021-08-11 21:04:21', NULL);
INSERT INTO `tb_resource` VALUES (170, '标签模块', NULL, NULL, NULL, 0, '2021-08-11 21:04:21', NULL);
INSERT INTO `tb_resource` VALUES (171, '照片模块', NULL, NULL, NULL, 0, '2021-08-11 21:04:21', NULL);
INSERT INTO `tb_resource` VALUES (172, '用户信息模块', NULL, NULL, NULL, 0, '2021-08-11 21:04:21', NULL);
INSERT INTO `tb_resource` VALUES (173, '用户账号模块', NULL, NULL, NULL, 0, '2021-08-11 21:04:21', NULL);
INSERT INTO `tb_resource` VALUES (174, '留言模块', NULL, NULL, NULL, 0, '2021-08-11 21:04:21', NULL);
INSERT INTO `tb_resource` VALUES (175, '相册模块', NULL, NULL, NULL, 0, '2021-08-11 21:04:21', NULL);
INSERT INTO `tb_resource` VALUES (176, '菜单模块', NULL, NULL, NULL, 0, '2021-08-11 21:04:21', NULL);
INSERT INTO `tb_resource` VALUES (177, '角色模块', NULL, NULL, NULL, 0, '2021-08-11 21:04:21', NULL);
INSERT INTO `tb_resource` VALUES (178, '评论模块', NULL, NULL, NULL, 0, '2021-08-11 21:04:21', NULL);
INSERT INTO `tb_resource` VALUES (179, '资源模块', NULL, NULL, NULL, 0, '2021-08-11 21:04:21', NULL);
INSERT INTO `tb_resource` VALUES (180, '页面模块', NULL, NULL, NULL, 0, '2021-08-11 21:04:21', NULL);
INSERT INTO `tb_resource` VALUES (181, '查看博客信息', '/', 'GET', 166, 1, '2021-08-11 21:04:22', '2021-08-11 21:05:29');
INSERT INTO `tb_resource` VALUES (182, '查看关于我信息', '/about', 'GET', 166, 1, '2021-08-11 21:04:22', '2021-08-11 21:05:29');
INSERT INTO `tb_resource` VALUES (183, '查看后台信息', '/admin', 'GET', 166, 0, '2021-08-11 21:04:22', NULL);
INSERT INTO `tb_resource` VALUES (184, '修改关于我信息', '/admin/about', 'PUT', 166, 0, '2021-08-11 21:04:22', NULL);
INSERT INTO `tb_resource` VALUES (185, '查看后台文章', '/admin/articles', 'GET', 168, 0, '2021-08-11 21:04:22', NULL);
INSERT INTO `tb_resource` VALUES (186, '添加或修改文章', '/admin/articles', 'POST', 168, 0, '2021-08-11 21:04:22', NULL);
INSERT INTO `tb_resource` VALUES (187, '恢复或删除文章', '/admin/articles', 'PUT', 168, 0, '2021-08-11 21:04:22', NULL);
INSERT INTO `tb_resource` VALUES (188, '物理删除文章', '/admin/articles', 'DELETE', 168, 0, '2021-08-11 21:04:22', NULL);
INSERT INTO `tb_resource` VALUES (189, '上传文章图片', '/admin/articles/images', 'POST', 168, 0, '2021-08-11 21:04:22', NULL);
INSERT INTO `tb_resource` VALUES (190, '修改文章置顶', '/admin/articles/top', 'PUT', 168, 0, '2021-08-11 21:04:22', NULL);
INSERT INTO `tb_resource` VALUES (191, '根据id查看后台文章', '/admin/articles/*', 'GET', 168, 1, '2021-08-11 21:04:22', '2024-05-25 17:17:33');
INSERT INTO `tb_resource` VALUES (192, '查看后台分类列表', '/admin/categories', 'GET', 165, 0, '2021-08-11 21:04:22', NULL);
INSERT INTO `tb_resource` VALUES (193, '添加或修改分类', '/admin/categories', 'POST', 165, 0, '2021-08-11 21:04:22', NULL);
INSERT INTO `tb_resource` VALUES (194, '删除分类', '/admin/categories', 'DELETE', 165, 0, '2021-08-11 21:04:22', NULL);
INSERT INTO `tb_resource` VALUES (195, '搜索文章分类', '/admin/categories/search', 'GET', 165, 0, '2021-08-11 21:04:22', NULL);
INSERT INTO `tb_resource` VALUES (196, '查询后台评论', '/admin/comments', 'GET', 178, 0, '2021-08-11 21:04:22', NULL);
INSERT INTO `tb_resource` VALUES (197, '删除评论', '/admin/comments', 'DELETE', 178, 0, '2021-08-11 21:04:22', NULL);
INSERT INTO `tb_resource` VALUES (198, '审核评论', '/admin/comments/review', 'PUT', 178, 0, '2021-08-11 21:04:22', NULL);
INSERT INTO `tb_resource` VALUES (199, '查看后台友链列表', '/admin/links', 'GET', 167, 0, '2021-08-11 21:04:22', NULL);
INSERT INTO `tb_resource` VALUES (200, '保存或修改友链', '/admin/links', 'POST', 167, 0, '2021-08-11 21:04:22', NULL);
INSERT INTO `tb_resource` VALUES (201, '删除友链', '/admin/links', 'DELETE', 167, 0, '2021-08-11 21:04:22', NULL);
INSERT INTO `tb_resource` VALUES (202, '查看菜单列表', '/admin/menus', 'GET', 176, 0, '2021-08-11 21:04:22', NULL);
INSERT INTO `tb_resource` VALUES (203, '新增或修改菜单', '/admin/menus', 'POST', 176, 0, '2021-08-11 21:04:22', NULL);
INSERT INTO `tb_resource` VALUES (204, '删除菜单', '/admin/menus/*', 'DELETE', 176, 0, '2021-08-11 21:04:22', NULL);
INSERT INTO `tb_resource` VALUES (205, '查看后台留言列表', '/admin/messages', 'GET', 174, 0, '2021-08-11 21:04:22', NULL);
INSERT INTO `tb_resource` VALUES (206, '删除留言', '/admin/messages', 'DELETE', 174, 0, '2021-08-11 21:04:22', NULL);
INSERT INTO `tb_resource` VALUES (207, '审核留言', '/admin/messages/review', 'PUT', 174, 0, '2021-08-11 21:04:22', NULL);
INSERT INTO `tb_resource` VALUES (208, '查看操作日志', '/admin/operation/logs', 'GET', 169, 0, '2021-08-11 21:04:22', NULL);
INSERT INTO `tb_resource` VALUES (209, '删除操作日志', '/admin/operation/logs', 'DELETE', 169, 0, '2021-08-11 21:04:22', NULL);
INSERT INTO `tb_resource` VALUES (210, '获取页面列表', '/admin/pages', 'GET', 180, 0, '2021-08-11 21:04:22', NULL);
INSERT INTO `tb_resource` VALUES (211, '保存或更新页面', '/admin/pages', 'POST', 180, 0, '2021-08-11 21:04:22', NULL);
INSERT INTO `tb_resource` VALUES (212, '删除页面', '/admin/pages/*', 'DELETE', 180, 0, '2021-08-11 21:04:22', NULL);
INSERT INTO `tb_resource` VALUES (213, '根据相册id获取照片列表', '/admin/photos', 'GET', 171, 0, '2021-08-11 21:04:22', NULL);
INSERT INTO `tb_resource` VALUES (214, '保存照片', '/admin/photos', 'POST', 171, 0, '2021-08-11 21:04:22', NULL);
INSERT INTO `tb_resource` VALUES (215, '更新照片信息', '/admin/photos', 'PUT', 171, 0, '2021-08-11 21:04:22', NULL);
INSERT INTO `tb_resource` VALUES (216, '删除照片', '/admin/photos', 'DELETE', 171, 0, '2021-08-11 21:04:22', NULL);
INSERT INTO `tb_resource` VALUES (217, '移动照片相册', '/admin/photos/album', 'PUT', 171, 0, '2021-08-11 21:04:22', NULL);
INSERT INTO `tb_resource` VALUES (218, '查看后台相册列表', '/admin/photos/albums', 'GET', 175, 0, '2021-08-11 21:04:22', NULL);
INSERT INTO `tb_resource` VALUES (219, '保存或更新相册', '/admin/photos/albums', 'POST', 175, 0, '2021-08-11 21:04:22', NULL);
INSERT INTO `tb_resource` VALUES (220, '上传相册封面', '/admin/photos/albums/cover', 'POST', 175, 0, '2021-08-11 21:04:22', NULL);
INSERT INTO `tb_resource` VALUES (221, '获取后台相册列表信息', '/admin/photos/albums/info', 'GET', 175, 0, '2021-08-11 21:04:22', NULL);
INSERT INTO `tb_resource` VALUES (222, '根据id删除相册', '/admin/photos/albums/*', 'DELETE', 175, 0, '2021-08-11 21:04:22', NULL);
INSERT INTO `tb_resource` VALUES (223, '根据id获取后台相册信息', '/admin/photos/albums/*/info', 'GET', 175, 0, '2021-08-11 21:04:22', NULL);
INSERT INTO `tb_resource` VALUES (224, '更新照片删除状态', '/admin/photos/delete', 'PUT', 171, 0, '2021-08-11 21:04:22', NULL);
INSERT INTO `tb_resource` VALUES (225, '查看资源列表', '/admin/resources', 'GET', 179, 0, '2021-08-11 21:04:22', NULL);
INSERT INTO `tb_resource` VALUES (226, '新增或修改资源', '/admin/resources', 'POST', 179, 0, '2021-08-11 21:04:22', NULL);
INSERT INTO `tb_resource` VALUES (227, '导入swagger接口', '/admin/resources/import/swagger', 'GET', 179, 0, '2021-08-11 21:04:22', NULL);
INSERT INTO `tb_resource` VALUES (228, '删除资源', '/admin/resources/*', 'DELETE', 179, 0, '2021-08-11 21:04:22', NULL);
INSERT INTO `tb_resource` VALUES (229, '保存或更新角色', '/admin/role', 'POST', 177, 0, '2021-08-11 21:04:22', NULL);
INSERT INTO `tb_resource` VALUES (230, '查看角色菜单选项', '/admin/role/menus', 'GET', 176, 0, '2021-08-11 21:04:22', NULL);
INSERT INTO `tb_resource` VALUES (231, '查看角色资源选项', '/admin/role/resources', 'GET', 179, 0, '2021-08-11 21:04:22', NULL);
INSERT INTO `tb_resource` VALUES (232, '查询角色列表', '/admin/roles', 'GET', 177, 0, '2021-08-11 21:04:22', NULL);
INSERT INTO `tb_resource` VALUES (233, '删除角色', '/admin/roles', 'DELETE', 177, 0, '2021-08-11 21:04:22', NULL);
INSERT INTO `tb_resource` VALUES (234, '查询后台标签列表', '/admin/tags', 'GET', 170, 0, '2021-08-11 21:04:22', NULL);
INSERT INTO `tb_resource` VALUES (235, '添加或修改标签', '/admin/tags', 'POST', 170, 0, '2021-08-11 21:04:22', NULL);
INSERT INTO `tb_resource` VALUES (236, '删除标签', '/admin/tags', 'DELETE', 170, 0, '2021-08-11 21:04:22', NULL);
INSERT INTO `tb_resource` VALUES (237, '搜索文章标签', '/admin/tags/search', 'GET', 170, 0, '2021-08-11 21:04:22', NULL);
INSERT INTO `tb_resource` VALUES (238, '查看当前用户菜单', '/admin/user/menus', 'GET', 176, 0, '2021-08-11 21:04:22', NULL);
INSERT INTO `tb_resource` VALUES (239, '查询后台用户列表', '/admin/users', 'GET', 173, 0, '2021-08-11 21:04:22', NULL);
INSERT INTO `tb_resource` VALUES (240, '修改用户禁用状态', '/admin/users/disable', 'PUT', 172, 0, '2021-08-11 21:04:22', NULL);
INSERT INTO `tb_resource` VALUES (241, '查看在线用户', '/admin/users/online', 'GET', 172, 0, '2021-08-11 21:04:22', NULL);
INSERT INTO `tb_resource` VALUES (242, '修改管理员密码', '/admin/users/password', 'PUT', 173, 0, '2021-08-11 21:04:22', NULL);
INSERT INTO `tb_resource` VALUES (243, '查询用户角色选项', '/admin/users/role', 'GET', 177, 0, '2021-08-11 21:04:22', NULL);
INSERT INTO `tb_resource` VALUES (244, '修改用户角色', '/admin/users/role', 'PUT', 172, 0, '2021-08-11 21:04:22', NULL);
INSERT INTO `tb_resource` VALUES (245, '下线用户', '/admin/users/*/online', 'DELETE', 172, 0, '2021-08-11 21:04:22', NULL);
INSERT INTO `tb_resource` VALUES (246, '获取网站配置', '/admin/website/config', 'GET', 166, 0, '2021-08-11 21:04:22', NULL);
INSERT INTO `tb_resource` VALUES (247, '更新网站配置', '/admin/website/config', 'PUT', 166, 0, '2021-08-11 21:04:22', NULL);
INSERT INTO `tb_resource` VALUES (248, '根据相册id查看照片列表', '/albums/*/photos', 'GET', 171, 1, '2021-08-11 21:04:22', '2021-08-11 21:06:35');
INSERT INTO `tb_resource` VALUES (249, '查看首页文章', '/articles', 'GET', 168, 1, '2021-08-11 21:04:22', '2024-05-25 17:17:15');
INSERT INTO `tb_resource` VALUES (250, '查看文章归档', '/articles/archives', 'GET', 168, 1, '2021-08-11 21:04:22', '2021-08-11 21:05:47');
INSERT INTO `tb_resource` VALUES (251, '根据条件查询文章', '/articles/condition', 'GET', 168, 1, '2021-08-11 21:04:22', '2021-08-11 21:05:47');
INSERT INTO `tb_resource` VALUES (252, '搜索文章', '/articles/search', 'GET', 168, 1, '2021-08-11 21:04:22', '2021-08-11 21:05:48');
INSERT INTO `tb_resource` VALUES (253, '根据id查看文章', '/articles/*', 'GET', 168, 1, '2021-08-11 21:04:22', '2024-05-25 17:18:58');
INSERT INTO `tb_resource` VALUES (254, '点赞文章', '/articles/*/like', 'POST', 168, 0, '2021-08-11 21:04:22', NULL);
INSERT INTO `tb_resource` VALUES (255, '查看分类列表', '/categories', 'GET', 165, 1, '2021-08-11 21:04:22', '2021-08-11 21:05:26');
INSERT INTO `tb_resource` VALUES (256, '查询评论', '/comments', 'GET', 178, 1, '2021-08-11 21:04:22', '2021-08-11 21:07:33');
INSERT INTO `tb_resource` VALUES (257, '添加评论', '/comments', 'POST', 178, 0, '2021-08-11 21:04:22', '2021-08-11 21:10:05');
INSERT INTO `tb_resource` VALUES (258, '评论点赞', '/comments/*/like', 'POST', 178, 0, '2021-08-11 21:04:22', NULL);
INSERT INTO `tb_resource` VALUES (259, '查询评论下的回复', '/comments/*/replies', 'GET', 178, 1, '2021-08-11 21:04:22', '2021-08-11 21:07:30');
INSERT INTO `tb_resource` VALUES (260, '查看友链列表', '/links', 'GET', 167, 1, '2021-08-11 21:04:22', '2021-08-11 21:05:41');
INSERT INTO `tb_resource` VALUES (261, '查看留言列表', '/messages', 'GET', 174, 1, '2021-08-11 21:04:22', '2025-08-12 16:23:35');
INSERT INTO `tb_resource` VALUES (262, '添加留言', '/messages', 'POST', 174, 1, '2021-08-11 21:04:22', '2025-08-21 16:04:03');
INSERT INTO `tb_resource` VALUES (263, '获取相册列表', '/photos/albums', 'GET', 175, 1, '2021-08-11 21:04:22', '2021-08-11 21:07:20');
INSERT INTO `tb_resource` VALUES (264, '用户注册', '/register', 'POST', 173, 1, '2021-08-11 21:04:22', '2021-08-11 21:07:01');
INSERT INTO `tb_resource` VALUES (265, '查询标签列表', '/tags', 'GET', 170, 1, '2021-08-11 21:04:22', '2021-08-11 21:06:30');
INSERT INTO `tb_resource` VALUES (267, '更新用户头像', '/users/avatar', 'POST', 172, 0, '2021-08-11 21:04:22', NULL);
INSERT INTO `tb_resource` VALUES (268, '发送邮箱验证码', '/users/code', 'GET', 173, 1, '2021-08-11 21:04:22', '2021-08-11 21:07:02');
INSERT INTO `tb_resource` VALUES (269, '绑定用户邮箱', '/users/email', 'POST', 172, 0, '2021-08-11 21:04:22', NULL);
INSERT INTO `tb_resource` VALUES (270, '更新用户信息', '/users/info', 'PUT', 172, 0, '2021-08-11 21:04:22', NULL);
INSERT INTO `tb_resource` VALUES (271, 'qq登录', '/users/oauth/qq', 'POST', 173, 1, '2021-08-11 21:04:22', '2021-08-11 21:07:06');
INSERT INTO `tb_resource` VALUES (272, '微博登录', '/users/oauth/weibo', 'POST', 173, 1, '2021-08-11 21:04:22', '2021-08-11 21:07:06');
INSERT INTO `tb_resource` VALUES (273, '修改密码', '/users/password', 'PUT', 173, 1, '2021-08-11 21:04:22', '2021-08-11 21:07:09');
INSERT INTO `tb_resource` VALUES (274, '上传语音', '/voice', 'POST', 166, 1, '2021-08-11 21:04:22', '2021-08-11 21:05:33');
INSERT INTO `tb_resource` VALUES (275, '上传访客信息', '/report', 'POST', 166, 1, '2021-08-24 00:32:05', '2021-08-24 00:32:07');
INSERT INTO `tb_resource` VALUES (276, '获取用户区域分布', '/admin/users/area', 'GET', 173, 0, '2021-08-24 00:32:35', '2021-09-24 16:25:34');
INSERT INTO `tb_resource` VALUES (278, '说说模块', NULL, NULL, NULL, 0, '2022-01-24 01:29:13', NULL);
INSERT INTO `tb_resource` VALUES (279, '查看首页说说', '/home/talks', 'GET', 278, 1, '2022-01-24 01:29:29', '2022-01-24 01:31:56');
INSERT INTO `tb_resource` VALUES (280, '查看说说列表', '/talks', 'GET', 278, 1, '2022-01-24 01:29:52', '2022-01-24 01:31:56');
INSERT INTO `tb_resource` VALUES (281, '根据id查看说说', '/talks/*', 'GET', 278, 1, '2022-01-24 01:30:10', '2022-01-24 01:31:57');
INSERT INTO `tb_resource` VALUES (282, '点赞说说', '/talks/*/like', 'POST', 278, 0, '2022-01-24 01:30:30', NULL);
INSERT INTO `tb_resource` VALUES (283, '上传说说图片', '/admin/talks/images', 'POST', 278, 0, '2022-01-24 01:30:46', NULL);
INSERT INTO `tb_resource` VALUES (284, '保存或修改说说', '/admin/talks', 'POST', 278, 0, '2022-01-24 01:31:04', NULL);
INSERT INTO `tb_resource` VALUES (285, '删除说说', '/admin/talks', 'DELETE', 278, 0, '2022-01-24 01:31:22', NULL);
INSERT INTO `tb_resource` VALUES (286, '查看后台说说', '/admin/talks', 'GET', 278, 0, '2022-01-24 01:31:38', NULL);
INSERT INTO `tb_resource` VALUES (287, '根据id查看后台说说', '/admin/talks/*', 'GET', 278, 0, '2022-01-24 01:31:53', '2022-01-24 01:33:14');
INSERT INTO `tb_resource` VALUES (288, '系统通知', NULL, NULL, NULL, 0, '2026-05-10 15:29:20', NULL);
INSERT INTO `tb_resource` VALUES (289, '发布系统通知', '/admin/notices/system', 'POST', 288, 0, '2026-05-10 15:29:45', '2026-05-10 15:34:28');
INSERT INTO `tb_resource` VALUES (290, '纪念页模块', NULL, NULL, NULL, 0, '2026-05-17 23:39:23', NULL);
INSERT INTO `tb_resource` VALUES (291, '前台获取纪念页配置', '/love/config', 'GET', 290, 1, '2026-05-17 23:39:35', '2026-05-17 23:40:13');
INSERT INTO `tb_resource` VALUES (292, '后台查询纪念页配置', '/admin/love/config', 'GET', 290, 0, '2026-05-17 23:39:56', NULL);
INSERT INTO `tb_resource` VALUES (293, '后台保存/修改纪念页配置', '/admin/love/config', 'PUT', 290, 0, '2026-05-17 23:40:11', NULL);
INSERT INTO `tb_resource` VALUES (294, '前台获取信件正文', '/love/letter', 'GET', 290, 1, '2026-05-17 23:40:25', '2026-05-17 23:40:27');
INSERT INTO `tb_resource` VALUES (295, '后台查询信件正文', '/admin/love/letter', 'GET', 290, 0, '2026-05-17 23:40:41', NULL);
INSERT INTO `tb_resource` VALUES (296, '后台保存/修改信件正文', '/admin/love/letter', 'PUT', 290, 0, '2026-05-17 23:40:53', NULL);

-- ----------------------------
-- Table structure for tb_role
-- ----------------------------
DROP TABLE IF EXISTS `tb_role`;
CREATE TABLE `tb_role`  (
                            `id` int NOT NULL AUTO_INCREMENT COMMENT '主键id',
                            `role_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色名',
                            `role_label` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色描述',
                            `is_disable` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否禁用  0否 1是',
                            `create_time` datetime NOT NULL COMMENT '创建时间',
                            `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
                            PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tb_role
-- ----------------------------
INSERT INTO `tb_role` VALUES (1, '管理员', 'admin', 0, '2021-03-22 14:10:21', '2026-05-17 23:57:18');
INSERT INTO `tb_role` VALUES (2, '用户', 'user', 0, '2021-03-22 14:25:25', '2025-05-23 18:03:46');
INSERT INTO `tb_role` VALUES (3, '测试', 'test', 0, '2021-03-22 14:42:23', '2025-05-23 17:54:52');

-- ----------------------------
-- Table structure for tb_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `tb_role_menu`;
CREATE TABLE `tb_role_menu`  (
                                 `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
                                 `role_id` int NULL DEFAULT NULL COMMENT '角色id',
                                 `menu_id` int NULL DEFAULT NULL COMMENT '菜单id',
                                 PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2867 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tb_role_menu
-- ----------------------------
INSERT INTO `tb_role_menu` VALUES (2528, 3, 1);
INSERT INTO `tb_role_menu` VALUES (2529, 3, 2);
INSERT INTO `tb_role_menu` VALUES (2530, 3, 6);
INSERT INTO `tb_role_menu` VALUES (2531, 3, 7);
INSERT INTO `tb_role_menu` VALUES (2532, 3, 8);
INSERT INTO `tb_role_menu` VALUES (2533, 3, 9);
INSERT INTO `tb_role_menu` VALUES (2534, 3, 10);
INSERT INTO `tb_role_menu` VALUES (2535, 3, 3);
INSERT INTO `tb_role_menu` VALUES (2536, 3, 11);
INSERT INTO `tb_role_menu` VALUES (2537, 3, 12);
INSERT INTO `tb_role_menu` VALUES (2538, 3, 202);
INSERT INTO `tb_role_menu` VALUES (2539, 3, 13);
INSERT INTO `tb_role_menu` VALUES (2540, 3, 201);
INSERT INTO `tb_role_menu` VALUES (2541, 3, 213);
INSERT INTO `tb_role_menu` VALUES (2542, 3, 14);
INSERT INTO `tb_role_menu` VALUES (2543, 3, 15);
INSERT INTO `tb_role_menu` VALUES (2544, 3, 16);
INSERT INTO `tb_role_menu` VALUES (2545, 3, 4);
INSERT INTO `tb_role_menu` VALUES (2546, 3, 214);
INSERT INTO `tb_role_menu` VALUES (2547, 3, 209);
INSERT INTO `tb_role_menu` VALUES (2548, 3, 17);
INSERT INTO `tb_role_menu` VALUES (2549, 3, 18);
INSERT INTO `tb_role_menu` VALUES (2550, 3, 205);
INSERT INTO `tb_role_menu` VALUES (2551, 3, 206);
INSERT INTO `tb_role_menu` VALUES (2552, 3, 208);
INSERT INTO `tb_role_menu` VALUES (2553, 3, 210);
INSERT INTO `tb_role_menu` VALUES (2554, 3, 215);
INSERT INTO `tb_role_menu` VALUES (2555, 3, 216);
INSERT INTO `tb_role_menu` VALUES (2556, 3, 217);
INSERT INTO `tb_role_menu` VALUES (2557, 3, 218);
INSERT INTO `tb_role_menu` VALUES (2558, 3, 19);
INSERT INTO `tb_role_menu` VALUES (2559, 3, 20);
INSERT INTO `tb_role_menu` VALUES (2560, 3, 5);
INSERT INTO `tb_role_menu` VALUES (2832, 1, 1);
INSERT INTO `tb_role_menu` VALUES (2833, 1, 2);
INSERT INTO `tb_role_menu` VALUES (2834, 1, 6);
INSERT INTO `tb_role_menu` VALUES (2835, 1, 7);
INSERT INTO `tb_role_menu` VALUES (2836, 1, 8);
INSERT INTO `tb_role_menu` VALUES (2837, 1, 9);
INSERT INTO `tb_role_menu` VALUES (2838, 1, 10);
INSERT INTO `tb_role_menu` VALUES (2839, 1, 3);
INSERT INTO `tb_role_menu` VALUES (2840, 1, 11);
INSERT INTO `tb_role_menu` VALUES (2841, 1, 12);
INSERT INTO `tb_role_menu` VALUES (2842, 1, 219);
INSERT INTO `tb_role_menu` VALUES (2843, 1, 202);
INSERT INTO `tb_role_menu` VALUES (2844, 1, 13);
INSERT INTO `tb_role_menu` VALUES (2845, 1, 201);
INSERT INTO `tb_role_menu` VALUES (2846, 1, 213);
INSERT INTO `tb_role_menu` VALUES (2847, 1, 14);
INSERT INTO `tb_role_menu` VALUES (2848, 1, 15);
INSERT INTO `tb_role_menu` VALUES (2849, 1, 16);
INSERT INTO `tb_role_menu` VALUES (2850, 1, 4);
INSERT INTO `tb_role_menu` VALUES (2851, 1, 214);
INSERT INTO `tb_role_menu` VALUES (2852, 1, 209);
INSERT INTO `tb_role_menu` VALUES (2853, 1, 17);
INSERT INTO `tb_role_menu` VALUES (2854, 1, 18);
INSERT INTO `tb_role_menu` VALUES (2855, 1, 221);
INSERT INTO `tb_role_menu` VALUES (2856, 1, 205);
INSERT INTO `tb_role_menu` VALUES (2857, 1, 206);
INSERT INTO `tb_role_menu` VALUES (2858, 1, 208);
INSERT INTO `tb_role_menu` VALUES (2859, 1, 210);
INSERT INTO `tb_role_menu` VALUES (2860, 1, 215);
INSERT INTO `tb_role_menu` VALUES (2861, 1, 216);
INSERT INTO `tb_role_menu` VALUES (2862, 1, 217);
INSERT INTO `tb_role_menu` VALUES (2863, 1, 218);
INSERT INTO `tb_role_menu` VALUES (2864, 1, 19);
INSERT INTO `tb_role_menu` VALUES (2865, 1, 20);
INSERT INTO `tb_role_menu` VALUES (2866, 1, 5);

-- ----------------------------
-- Table structure for tb_role_resource
-- ----------------------------
DROP TABLE IF EXISTS `tb_role_resource`;
CREATE TABLE `tb_role_resource`  (
                                     `id` int NOT NULL AUTO_INCREMENT,
                                     `role_id` int NULL DEFAULT NULL COMMENT '角色id',
                                     `resource_id` int NULL DEFAULT NULL COMMENT '权限id',
                                     PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5333 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tb_role_resource
-- ----------------------------
INSERT INTO `tb_role_resource` VALUES (5044, 3, 192);
INSERT INTO `tb_role_resource` VALUES (5045, 3, 195);
INSERT INTO `tb_role_resource` VALUES (5046, 3, 183);
INSERT INTO `tb_role_resource` VALUES (5047, 3, 246);
INSERT INTO `tb_role_resource` VALUES (5048, 3, 199);
INSERT INTO `tb_role_resource` VALUES (5049, 3, 185);
INSERT INTO `tb_role_resource` VALUES (5050, 3, 254);
INSERT INTO `tb_role_resource` VALUES (5051, 3, 208);
INSERT INTO `tb_role_resource` VALUES (5052, 3, 234);
INSERT INTO `tb_role_resource` VALUES (5053, 3, 237);
INSERT INTO `tb_role_resource` VALUES (5054, 3, 213);
INSERT INTO `tb_role_resource` VALUES (5055, 3, 241);
INSERT INTO `tb_role_resource` VALUES (5056, 3, 239);
INSERT INTO `tb_role_resource` VALUES (5057, 3, 276);
INSERT INTO `tb_role_resource` VALUES (5058, 3, 205);
INSERT INTO `tb_role_resource` VALUES (5059, 3, 218);
INSERT INTO `tb_role_resource` VALUES (5060, 3, 221);
INSERT INTO `tb_role_resource` VALUES (5061, 3, 223);
INSERT INTO `tb_role_resource` VALUES (5062, 3, 202);
INSERT INTO `tb_role_resource` VALUES (5063, 3, 230);
INSERT INTO `tb_role_resource` VALUES (5064, 3, 238);
INSERT INTO `tb_role_resource` VALUES (5065, 3, 232);
INSERT INTO `tb_role_resource` VALUES (5066, 3, 243);
INSERT INTO `tb_role_resource` VALUES (5067, 3, 196);
INSERT INTO `tb_role_resource` VALUES (5068, 3, 257);
INSERT INTO `tb_role_resource` VALUES (5069, 3, 258);
INSERT INTO `tb_role_resource` VALUES (5070, 3, 225);
INSERT INTO `tb_role_resource` VALUES (5071, 3, 231);
INSERT INTO `tb_role_resource` VALUES (5072, 3, 210);
INSERT INTO `tb_role_resource` VALUES (5073, 3, 282);
INSERT INTO `tb_role_resource` VALUES (5074, 3, 286);
INSERT INTO `tb_role_resource` VALUES (5075, 3, 287);
INSERT INTO `tb_role_resource` VALUES (5125, 2, 192);
INSERT INTO `tb_role_resource` VALUES (5126, 2, 183);
INSERT INTO `tb_role_resource` VALUES (5127, 2, 246);
INSERT INTO `tb_role_resource` VALUES (5128, 2, 254);
INSERT INTO `tb_role_resource` VALUES (5129, 2, 267);
INSERT INTO `tb_role_resource` VALUES (5130, 2, 269);
INSERT INTO `tb_role_resource` VALUES (5131, 2, 270);
INSERT INTO `tb_role_resource` VALUES (5132, 2, 202);
INSERT INTO `tb_role_resource` VALUES (5133, 2, 257);
INSERT INTO `tb_role_resource` VALUES (5134, 2, 258);
INSERT INTO `tb_role_resource` VALUES (5135, 2, 282);
INSERT INTO `tb_role_resource` VALUES (5232, 1, 165);
INSERT INTO `tb_role_resource` VALUES (5233, 1, 192);
INSERT INTO `tb_role_resource` VALUES (5234, 1, 193);
INSERT INTO `tb_role_resource` VALUES (5235, 1, 194);
INSERT INTO `tb_role_resource` VALUES (5236, 1, 195);
INSERT INTO `tb_role_resource` VALUES (5237, 1, 166);
INSERT INTO `tb_role_resource` VALUES (5238, 1, 183);
INSERT INTO `tb_role_resource` VALUES (5239, 1, 184);
INSERT INTO `tb_role_resource` VALUES (5240, 1, 246);
INSERT INTO `tb_role_resource` VALUES (5241, 1, 247);
INSERT INTO `tb_role_resource` VALUES (5242, 1, 167);
INSERT INTO `tb_role_resource` VALUES (5243, 1, 199);
INSERT INTO `tb_role_resource` VALUES (5244, 1, 200);
INSERT INTO `tb_role_resource` VALUES (5245, 1, 201);
INSERT INTO `tb_role_resource` VALUES (5246, 1, 168);
INSERT INTO `tb_role_resource` VALUES (5247, 1, 185);
INSERT INTO `tb_role_resource` VALUES (5248, 1, 186);
INSERT INTO `tb_role_resource` VALUES (5249, 1, 187);
INSERT INTO `tb_role_resource` VALUES (5250, 1, 188);
INSERT INTO `tb_role_resource` VALUES (5251, 1, 189);
INSERT INTO `tb_role_resource` VALUES (5252, 1, 190);
INSERT INTO `tb_role_resource` VALUES (5253, 1, 254);
INSERT INTO `tb_role_resource` VALUES (5254, 1, 169);
INSERT INTO `tb_role_resource` VALUES (5255, 1, 208);
INSERT INTO `tb_role_resource` VALUES (5256, 1, 209);
INSERT INTO `tb_role_resource` VALUES (5257, 1, 170);
INSERT INTO `tb_role_resource` VALUES (5258, 1, 234);
INSERT INTO `tb_role_resource` VALUES (5259, 1, 235);
INSERT INTO `tb_role_resource` VALUES (5260, 1, 236);
INSERT INTO `tb_role_resource` VALUES (5261, 1, 237);
INSERT INTO `tb_role_resource` VALUES (5262, 1, 171);
INSERT INTO `tb_role_resource` VALUES (5263, 1, 213);
INSERT INTO `tb_role_resource` VALUES (5264, 1, 214);
INSERT INTO `tb_role_resource` VALUES (5265, 1, 215);
INSERT INTO `tb_role_resource` VALUES (5266, 1, 216);
INSERT INTO `tb_role_resource` VALUES (5267, 1, 217);
INSERT INTO `tb_role_resource` VALUES (5268, 1, 224);
INSERT INTO `tb_role_resource` VALUES (5269, 1, 172);
INSERT INTO `tb_role_resource` VALUES (5270, 1, 240);
INSERT INTO `tb_role_resource` VALUES (5271, 1, 241);
INSERT INTO `tb_role_resource` VALUES (5272, 1, 244);
INSERT INTO `tb_role_resource` VALUES (5273, 1, 245);
INSERT INTO `tb_role_resource` VALUES (5274, 1, 267);
INSERT INTO `tb_role_resource` VALUES (5275, 1, 269);
INSERT INTO `tb_role_resource` VALUES (5276, 1, 270);
INSERT INTO `tb_role_resource` VALUES (5277, 1, 173);
INSERT INTO `tb_role_resource` VALUES (5278, 1, 239);
INSERT INTO `tb_role_resource` VALUES (5279, 1, 242);
INSERT INTO `tb_role_resource` VALUES (5280, 1, 276);
INSERT INTO `tb_role_resource` VALUES (5281, 1, 174);
INSERT INTO `tb_role_resource` VALUES (5282, 1, 205);
INSERT INTO `tb_role_resource` VALUES (5283, 1, 206);
INSERT INTO `tb_role_resource` VALUES (5284, 1, 207);
INSERT INTO `tb_role_resource` VALUES (5285, 1, 175);
INSERT INTO `tb_role_resource` VALUES (5286, 1, 218);
INSERT INTO `tb_role_resource` VALUES (5287, 1, 219);
INSERT INTO `tb_role_resource` VALUES (5288, 1, 220);
INSERT INTO `tb_role_resource` VALUES (5289, 1, 221);
INSERT INTO `tb_role_resource` VALUES (5290, 1, 222);
INSERT INTO `tb_role_resource` VALUES (5291, 1, 223);
INSERT INTO `tb_role_resource` VALUES (5292, 1, 176);
INSERT INTO `tb_role_resource` VALUES (5293, 1, 202);
INSERT INTO `tb_role_resource` VALUES (5294, 1, 203);
INSERT INTO `tb_role_resource` VALUES (5295, 1, 204);
INSERT INTO `tb_role_resource` VALUES (5296, 1, 230);
INSERT INTO `tb_role_resource` VALUES (5297, 1, 238);
INSERT INTO `tb_role_resource` VALUES (5298, 1, 177);
INSERT INTO `tb_role_resource` VALUES (5299, 1, 229);
INSERT INTO `tb_role_resource` VALUES (5300, 1, 232);
INSERT INTO `tb_role_resource` VALUES (5301, 1, 233);
INSERT INTO `tb_role_resource` VALUES (5302, 1, 243);
INSERT INTO `tb_role_resource` VALUES (5303, 1, 178);
INSERT INTO `tb_role_resource` VALUES (5304, 1, 196);
INSERT INTO `tb_role_resource` VALUES (5305, 1, 197);
INSERT INTO `tb_role_resource` VALUES (5306, 1, 198);
INSERT INTO `tb_role_resource` VALUES (5307, 1, 257);
INSERT INTO `tb_role_resource` VALUES (5308, 1, 258);
INSERT INTO `tb_role_resource` VALUES (5309, 1, 179);
INSERT INTO `tb_role_resource` VALUES (5310, 1, 225);
INSERT INTO `tb_role_resource` VALUES (5311, 1, 226);
INSERT INTO `tb_role_resource` VALUES (5312, 1, 227);
INSERT INTO `tb_role_resource` VALUES (5313, 1, 228);
INSERT INTO `tb_role_resource` VALUES (5314, 1, 231);
INSERT INTO `tb_role_resource` VALUES (5315, 1, 180);
INSERT INTO `tb_role_resource` VALUES (5316, 1, 210);
INSERT INTO `tb_role_resource` VALUES (5317, 1, 211);
INSERT INTO `tb_role_resource` VALUES (5318, 1, 212);
INSERT INTO `tb_role_resource` VALUES (5319, 1, 278);
INSERT INTO `tb_role_resource` VALUES (5320, 1, 282);
INSERT INTO `tb_role_resource` VALUES (5321, 1, 283);
INSERT INTO `tb_role_resource` VALUES (5322, 1, 284);
INSERT INTO `tb_role_resource` VALUES (5323, 1, 285);
INSERT INTO `tb_role_resource` VALUES (5324, 1, 286);
INSERT INTO `tb_role_resource` VALUES (5325, 1, 287);
INSERT INTO `tb_role_resource` VALUES (5326, 1, 288);
INSERT INTO `tb_role_resource` VALUES (5327, 1, 289);
INSERT INTO `tb_role_resource` VALUES (5328, 1, 290);
INSERT INTO `tb_role_resource` VALUES (5329, 1, 292);
INSERT INTO `tb_role_resource` VALUES (5330, 1, 293);
INSERT INTO `tb_role_resource` VALUES (5331, 1, 295);
INSERT INTO `tb_role_resource` VALUES (5332, 1, 296);

-- ----------------------------
-- Table structure for tb_sensitive_word
-- ----------------------------
DROP TABLE IF EXISTS `tb_sensitive_word`;
CREATE TABLE `tb_sensitive_word`  (
                                      `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
                                      `word` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '敏感词',
                                      `word_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'default' COMMENT '敏感词类型',
                                      `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态 0禁用 1启用',
                                      `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                      PRIMARY KEY (`id`) USING BTREE,
                                      UNIQUE INDEX `uk_word`(`word` ASC) USING BTREE,
                                      INDEX `idx_word_type_status`(`word_type` ASC, `status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '敏感词表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_sensitive_word
-- ----------------------------
INSERT INTO `tb_sensitive_word` VALUES (1, '测试敏感词', 'default', 1, '2026-05-09 18:24:20');
INSERT INTO `tb_sensitive_word` VALUES (2, '广告', 'spam', 1, '2026-05-09 18:24:20');
INSERT INTO `tb_sensitive_word` VALUES (3, '刷单', 'spam', 1, '2026-05-09 18:24:20');

-- ----------------------------
-- Table structure for tb_system_notice
-- ----------------------------
DROP TABLE IF EXISTS `tb_system_notice`;
CREATE TABLE `tb_system_notice`  (
                                     `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
                                     `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '通知内容',
                                     `jump_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '前端跳转路径',
                                     `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                     PRIMARY KEY (`id`) USING BTREE,
                                     INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 28 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统通知表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_system_notice
-- ----------------------------

-- ----------------------------
-- Table structure for tb_system_notice_read
-- ----------------------------
DROP TABLE IF EXISTS `tb_system_notice_read`;
CREATE TABLE `tb_system_notice_read`  (
                                          `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
                                          `notice_id` bigint NOT NULL COMMENT '系统通知ID',
                                          `user_id` bigint NOT NULL COMMENT '用户ID',
                                          `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                          PRIMARY KEY (`id`) USING BTREE,
                                          UNIQUE INDEX `uk_notice_user`(`notice_id` ASC, `user_id` ASC) USING BTREE,
                                          INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统通知已读表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_system_notice_read
-- ----------------------------

-- ----------------------------
-- Table structure for tb_tag
-- ----------------------------
DROP TABLE IF EXISTS `tb_tag`;
CREATE TABLE `tb_tag`  (
                           `id` int NOT NULL AUTO_INCREMENT,
                           `tag_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标签名',
                           `create_time` datetime NOT NULL COMMENT '创建时间',
                           `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
                           PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 56 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tb_tag
-- ----------------------------

-- ----------------------------
-- Table structure for tb_talk
-- ----------------------------
DROP TABLE IF EXISTS `tb_talk`;
CREATE TABLE `tb_talk`  (
                            `id` int NOT NULL AUTO_INCREMENT COMMENT '说说id',
                            `user_id` int NOT NULL COMMENT '用户id',
                            `content` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '说说内容',
                            `images` varchar(2500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图片',
                            `is_top` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否置顶',
                            `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '状态 1.公开 2.私密',
                            `create_time` datetime NOT NULL COMMENT '创建时间',
                            `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
                            PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 58 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tb_talk
-- ----------------------------

-- ----------------------------
-- Table structure for tb_unique_view
-- ----------------------------
DROP TABLE IF EXISTS `tb_unique_view`;
CREATE TABLE `tb_unique_view`  (
                                   `id` int NOT NULL AUTO_INCREMENT,
                                   `views_count` int NOT NULL COMMENT '访问量',
                                   `create_time` datetime NOT NULL COMMENT '创建时间',
                                   `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
                                   PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 997 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tb_unique_view
-- ----------------------------

-- ----------------------------
-- Table structure for tb_user_article_collect
-- ----------------------------
DROP TABLE IF EXISTS `tb_user_article_collect`;
CREATE TABLE `tb_user_article_collect`  (
                                            `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
                                            `user_id` bigint NOT NULL COMMENT '用户ID',
                                            `article_id` bigint NOT NULL COMMENT '文章ID',
                                            `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                            PRIMARY KEY (`id`) USING BTREE,
                                            UNIQUE INDEX `uk_user_article`(`user_id` ASC, `article_id` ASC) USING BTREE,
                                            INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
                                            INDEX `idx_article_id`(`article_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户文章收藏表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_user_article_collect
-- ----------------------------

-- ----------------------------
-- Table structure for tb_user_article_history
-- ----------------------------
DROP TABLE IF EXISTS `tb_user_article_history`;
CREATE TABLE `tb_user_article_history`  (
                                            `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
                                            `user_id` bigint NOT NULL COMMENT '用户ID',
                                            `article_id` bigint NOT NULL COMMENT '文章ID',
                                            `progress_percent` int NOT NULL DEFAULT 0 COMMENT '阅读进度百分比',
                                            `last_read_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最近阅读时间',
                                            `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                            PRIMARY KEY (`id`) USING BTREE,
                                            UNIQUE INDEX `uk_user_article`(`user_id` ASC, `article_id` ASC) USING BTREE,
                                            INDEX `idx_user_id_last_read_time`(`user_id` ASC, `last_read_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户阅读历史表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_user_article_history
-- ----------------------------

-- ----------------------------
-- Table structure for tb_user_auth
-- ----------------------------
DROP TABLE IF EXISTS `tb_user_auth`;
CREATE TABLE `tb_user_auth`  (
                                 `id` int NOT NULL AUTO_INCREMENT,
                                 `user_info_id` int NOT NULL COMMENT '用户信息id',
                                 `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
                                 `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
                                 `login_type` tinyint(1) NOT NULL COMMENT '登录类型',
                                 `ip_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户登录ip',
                                 `ip_source` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'ip来源',
                                 `create_time` datetime NOT NULL COMMENT '创建时间',
                                 `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
                                 `last_login_time` datetime NULL DEFAULT NULL COMMENT '上次登录时间',
                                 PRIMARY KEY (`id`) USING BTREE,
                                 UNIQUE INDEX `username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1011 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tb_user_auth
-- ----------------------------
INSERT INTO `tb_user_auth` VALUES (1, 1, 'admin', '$2a$10$Y4YzRAyFyHSLoRL8beCOpecmh3xxHYbvOjKvt2Sm.nepRutbeSvWa', 1, '127.0.0.1', '', '2021-08-12 15:43:18', '2026-08-13 14:26:59', '2026-08-13 14:26:59');

-- ----------------------------
-- Table structure for tb_user_info
-- ----------------------------
DROP TABLE IF EXISTS `tb_user_info`;
CREATE TABLE `tb_user_info`  (
                                 `id` int NOT NULL AUTO_INCREMENT COMMENT '用户ID',
                                 `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱号',
                                 `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户昵称',
                                 `avatar` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '用户头像',
                                 `intro` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户简介',
                                 `web_site` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '个人网站',
                                 `is_disable` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否禁用',
                                 `create_time` datetime NOT NULL COMMENT '创建时间',
                                 `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
                                 PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1021 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tb_user_info
-- ----------------------------
INSERT INTO `tb_user_info` VALUES (1, '2956867099@qq.com', '任子双', 'https://static.renzs.top/avatar/3ae611672da0d4dfff80f1438d87f22a.jpg', '枯木会逢春，人无再少年。', 'www.renzs.top', 0, '1601-01-01 15:43:17', '2026-05-10 00:53:46');

-- ----------------------------
-- Table structure for tb_user_notice
-- ----------------------------
DROP TABLE IF EXISTS `tb_user_notice`;
CREATE TABLE `tb_user_notice`  (
                                   `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
                                   `user_id` bigint NOT NULL COMMENT '用户ID',
                                   `notice_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '通知类型',
                                   `source_id` bigint NULL DEFAULT NULL COMMENT '来源记录ID',
                                   `source_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '来源模块',
                                   `target_id` bigint NULL DEFAULT NULL COMMENT '最终跳转目标ID',
                                   `target_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最终跳转目标类型',
                                   `jump_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '前端跳转路径',
                                   `anchor_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '前端锚点键',
                                   `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '通知内容',
                                   `reply_content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '回复内容',
                                   `is_read` tinyint NOT NULL DEFAULT 0 COMMENT '是否已读 0未读 1已读',
                                   `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   PRIMARY KEY (`id`) USING BTREE,
                                   INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
                                   INDEX `idx_user_id_is_read`(`user_id` ASC, `is_read` ASC) USING BTREE,
                                   INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户通知表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_user_notice
-- ----------------------------

-- ----------------------------
-- Table structure for tb_user_role
-- ----------------------------
DROP TABLE IF EXISTS `tb_user_role`;
CREATE TABLE `tb_user_role`  (
                                 `id` int NOT NULL AUTO_INCREMENT,
                                 `user_id` int NULL DEFAULT NULL COMMENT '用户id',
                                 `role_id` int NULL DEFAULT NULL COMMENT '角色id',
                                 PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1025 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tb_user_role
-- ----------------------------
INSERT INTO `tb_user_role` VALUES (1001, 1, 1);

-- ----------------------------
-- Table structure for tb_website_config
-- ----------------------------
DROP TABLE IF EXISTS `tb_website_config`;
CREATE TABLE `tb_website_config`  (
                                      `id` int NOT NULL AUTO_INCREMENT,
                                      `config` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置信息',
                                      `create_time` datetime NOT NULL COMMENT '创建时间',
                                      `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
                                      PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tb_website_config
-- ----------------------------
INSERT INTO `tb_website_config` VALUES (1, '{}', '2026-08-13 14:54:33', '2026-08-13 14:54:35');

SET FOREIGN_KEY_CHECKS = 1;
