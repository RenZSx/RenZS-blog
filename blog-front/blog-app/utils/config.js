/**
 * 全局可调常量
 * 后续要做"运行时切换服务器"时,把 BASE_URL 从常量改为 store 字段即可
 */

// 后端基地址(不带 /api 前缀;blog-vue3 的 /api 仅是 Vite 代理用,uniapp 直连后端要剥)
export const BASE_URL = 'http://8.137.86.224:8088'

// 本地 storage 中 token 的 key,与 blog-vue3 保持一致便于概念统一
export const TOKEN_KEY = 'token'

// 业务码常量,与后端 StatusCodeEnum 对齐
export const SUCCESS_CODE = 20000
export const FAIL_CODE = 51000
export const NO_LOGIN_CODE = 40001
export const NO_PERMISSION_CODE = 40300
export const SYSTEM_ERROR_CODE = 50000

// 评论类型:与后端 CommentTypeEnum 对齐
//   1 = 文章评论, 2 = 友链评论, 3 = 说说评论, 4 = 留言, 5 = 关于评论
export const COMMENT_TYPE_ARTICLE = 1
