import request from '@/api/request'

export function fetchArticle(path: string) {
  return request.get('/api' + path)
}

export function sendArticleLike(articleId: number) {
  return request.post('/api/articles/' + articleId + '/like')
}

export function askArticleQuestion(articleId: number, question: string) {
  return request.post('/api/articles/' + articleId + '/ai-question', { question })
}
