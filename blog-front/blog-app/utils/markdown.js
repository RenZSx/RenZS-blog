/**
 * markdown → HTML 转换(适配 uniapp <rich-text>)
 *
 * 当前完全用纯 JS 正则实现,无 npm 依赖,无网络请求,适合 App 离线场景。
 * 如果未来要更完整的 markdown(表格 / 任务列表 / GFM),建议从 HBuilderX 插件市场
 * 装 mp-html,然后:
 *   1. 把本文件删掉
 *   2. article.vue 模板用 <mp-html :content="article.articleContent" />
 *
 * 当前支持:
 *   - 标题 H1-H3
 *   - 段落
 *   - 粗体 / 斜体 / 行内代码 / 删除线
 *   - 代码块(支持语言标记)
 *   - 链接 / 图片
 *   - 无序/有序列表(单层)
 *   - 引用
 *   - 水平分割线 ---
 */

export function markdownToHtml(md) {
  if (!md || typeof md !== 'string') return ''

  let html = md.trim()

  // 1. 代码块(优先处理,避免内部内容被其他规则误转)
  html = html.replace(/```(\w+)?\n?([\s\S]*?)```/g, (_, lang, code) => {
    const langTag = lang ? `<span class="md-code-lang">${escapeHtml(lang)}</span>` : ''
    return `<pre class="md-pre">${langTag}<code class="md-code">${escapeHtml(code.trim())}</code></pre>`
  })

  // 2. 图片(优先于链接)
  html = html.replace(/!\[(.*?)\]\((.*?)\)/g, '<img class="md-img" src="$2" alt="$1" />')

  // 3. 链接
  html = html.replace(/\[(.*?)\]\((.*?)\)/g, '<a class="md-link" href="$2">$1</a>')

  // 4. 水平分割线
  html = html.replace(/^---+$/gm, '<hr class="md-hr" />')

  // 5. 标题(从大到小,避免误匹配)
  html = html.replace(/^###\s+(.+)$/gm, '<h3 class="md-h3">$1</h3>')
  html = html.replace(/^##\s+(.+)$/gm, '<h2 class="md-h2">$1</h2>')
  html = html.replace(/^#\s+(.+)$/gm, '<h1 class="md-h1">$1</h1>')

  // 6. 强调
  html = html.replace(/\*\*(.+?)\*\*/g, '<strong class="md-bold">$1</strong>')
  html = html.replace(/(?<![*])\*(?!\s)(.+?)(?<!\s)\*(?![*])/g, '<em class="md-em">$1</em>')
  html = html.replace(/~~(.+?)~~/g, '<del class="md-del">$1</del>')

  // 7. 行内代码
  html = html.replace(/`([^`\n]+)`/g, '<code class="md-inline-code">$1</code>')

  // 8. 引用
  html = html.replace(/^>\s+(.+)$/gm, '<blockquote class="md-quote">$1</blockquote>')

  // 9. 无序列表
  html = html.replace(/^((?:- .+(?:\n|$))+)/gm, (block) => {
    const items = block.trim().split('\n').map((line) => `<li class="md-li">${line.replace(/^- /, '')}</li>`).join('')
    return `<ul class="md-ul">${items}</ul>`
  })

  // 10. 有序列表
  html = html.replace(/^((?:\d+\. .+(?:\n|$))+)/gm, (block) => {
    const items = block.trim().split('\n').map((line) => `<li class="md-li">${line.replace(/^\d+\. /, '')}</li>`).join('')
    return `<ol class="md-ol">${items}</ol>`
  })

  // 11. 段落(空行分段)
  html = html.split(/\n{2,}/).map((block) => {
    block = block.trim()
    if (!block) return ''
    // 已是 HTML 块的不再包 <p>
    if (/^<(h\d|pre|blockquote|ul|ol|img|div|hr|table)/i.test(block)) return block
    return `<p class="md-p">${block.replace(/\n/g, '<br />')}</p>`
  }).join('')

  return html
}

function escapeHtml(text) {
  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
}
