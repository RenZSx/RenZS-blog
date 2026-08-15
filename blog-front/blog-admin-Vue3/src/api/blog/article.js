import request from '@/utils/request'

// 查询文章列表
export function listArticles(query) {
  return request({
    url: '/admin/articles',
    method: 'get',
    params: query
  })
}

// 查询文章详情
export function getArticle(articleId) {
  return request({
    url: '/admin/articles/' + articleId,
    method: 'get'
  })
}

// 新增文章
export function addArticle(data) {
  return request({
    url: '/admin/articles',
    method: 'post',
    data: data
  })
}

// 修改文章
export function updateArticle(data) {
  return request({
    url: '/admin/articles',
    method: 'post',
    data: data
  })
}

// 删除文章(逻辑删除)
export function updateArticleDelete(data) {
  return request({
    url: '/admin/articles',
    method: 'put',
    data: data
  })
}

// 删除文章(物理删除)
export function deleteArticle(articleIds) {
  return request({
    url: '/admin/articles',
    method: 'delete',
    data: articleIds
  })
}

// 修改文章置顶
export function updateArticleTop(data) {
  return request({
    url: '/admin/articles/top',
    method: 'put',
    data: data
  })
}

// 导出文章
export function exportArticles(articleIds) {
  return request({
    url: '/admin/articles/export',
    method: 'post',
    data: articleIds
  })
}

// 上传文章图片
export function uploadArticleImage(data) {
  return request({
    url: '/admin/articles/images',
    method: 'post',
    data: data
  })
}

// 生成AI总结
export function generateAiSummary(articleId) {
  return request({
    url: '/admin/articles/' + articleId + '/ai-summary',
    method: 'post'
  })
}

// AI推荐标签
export function generateAiTags(data) {
  return request({
    url: '/admin/articles/ai-tags',
    method: 'post',
    data: data
  })
}

// 生成SEO
export function generateAiSeo(data) {
  return request({
    url: '/admin/articles/ai-seo',
    method: 'post',
    data: data
  })
}
