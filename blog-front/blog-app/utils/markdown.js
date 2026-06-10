/**
 * markdown → HTML 转换(适配 mp-html 渲染器)
 *
 * 当前完全用纯 JS 正则实现,无 npm 依赖,无网络请求,适合 App 离线场景。
 *
 * 当前支持:
 *   - 标题 H1-H3
 *   - 段落
 *   - 粗体 / 斜体 / 行内代码 / 删除线
 *   - 代码块(支持语言标记 + 轻量语法高亮)
 *   - 链接 / 图片
 *   - 无序/有序列表(单层)
 *   - 任务列表 [ ] [x]
 *   - 引用
 *   - 水平分割线 ---
 *   - GFM 表格
 */

// 占位符用 Unicode 私用区字符,代码源码里几乎不会出现
const PH_OPEN = String.fromCharCode(0xE000)   // U+E000
const PH_CLOSE = String.fromCharCode(0xE001)  // U+E001

export function markdownToHtml(md) {
  if (!md || typeof md !== 'string') return ''

  let html = md.trim()

  // 1. 代码块(优先处理,避免内部内容被其他规则误转)
  const codeBlockStash = []
  html = html.replace(/```(\w+)?\n?([\s\S]*?)```/g, (_, lang, code) => {
    const langKey = (lang || '').toLowerCase()
    const langTag = lang ? `<span class="md-code-lang">${escapeHtml(lang)}</span>` : ''
    // 用轻量语法高亮替代直接 escape:字符串/注释/关键字着色,默认字色由 pre 决定
    const highlighted = highlightCode(code.trim(), langKey)
    const block = `<pre class="md-pre">${langTag}<code class="md-code">${highlighted}</code></pre>`
    codeBlockStash.push(block)
    return ` CODEBLOCK${codeBlockStash.length - 1} `
  })

  // 2. 行内代码
  const inlineCodeStash = []
  html = html.replace(/`([^`\n]+)`/g, (_, code) => {
    inlineCodeStash.push(`<code class="md-inline-code">${escapeHtml(code)}</code>`)
    return ` INLINECODE${inlineCodeStash.length - 1} `
  })

  // 3. GFM 表格
  html = html.replace(
    /^(\|.+\|[ \t]*\n\|[ \t:|-]+\|[ \t]*\n(?:\|.+\|[ \t]*\n?)+)/gm,
    (block) => {
      const lines = block.trim().split('\n')
      if (lines.length < 2) return block
      const head = splitRow(lines[0])
      const align = splitRow(lines[1]).map(alignCell)
      const bodyRows = lines.slice(2).map(splitRow)

      const ths = head
        .map((c, i) => `<th class="md-th"${alignStyle(align[i])}>${c.trim()}</th>`)
        .join('')
      const trs = bodyRows
        .map((row) => {
          const tds = row
            .map((c, i) => `<td class="md-td"${alignStyle(align[i])}>${c.trim()}</td>`)
            .join('')
          return `<tr class="md-tr">${tds}</tr>`
        })
        .join('')

      return `<table class="md-table"><thead><tr class="md-tr">${ths}</tr></thead><tbody>${trs}</tbody></table>`
    }
  )

  // 4. 图片
  html = html.replace(/!\[(.*?)\]\((.*?)\)/g, '<img class="md-img" src="$2" alt="$1" />')

  // 5. 链接
  html = html.replace(/\[(.*?)\]\((.*?)\)/g, '<a class="md-link" href="$2">$1</a>')

  // 6. 水平分割线
  html = html.replace(/^---+$/gm, '<hr class="md-hr" />')

  // 7. 标题
  html = html.replace(/^###\s+(.+)$/gm, '<h3 class="md-h3">$1</h3>')
  html = html.replace(/^##\s+(.+)$/gm, '<h2 class="md-h2">$1</h2>')
  html = html.replace(/^#\s+(.+)$/gm, '<h1 class="md-h1">$1</h1>')

  // 8. 强调
  html = html.replace(/\*\*(.+?)\*\*/g, '<strong class="md-bold">$1</strong>')
  html = html.replace(/(?<![*])\*(?!\s)(.+?)(?<!\s)\*(?![*])/g, '<em class="md-em">$1</em>')
  html = html.replace(/~~(.+?)~~/g, '<del class="md-del">$1</del>')

  // 9. 引用
  html = html.replace(/^>\s+(.+)$/gm, '<blockquote class="md-quote">$1</blockquote>')

  // 10. 任务列表
  html = html.replace(/^- \[([ xX])\] (.+)$/gm, (_, checked, text) => {
    const isChecked = checked === 'x' || checked === 'X'
    const mark = isChecked ? '✅' : '⬜'
    return `<li class="md-task${isChecked ? ' done' : ''}">${mark} ${text}</li>`
  })

  // 11. 无序列表
  html = html.replace(/^((?:- .+(?:\n|$))+)/gm, (block) => {
    const items = block
      .trim()
      .split('\n')
      .map((line) => `<li class="md-li">${line.replace(/^- /, '')}</li>`)
      .join('')
    return `<ul class="md-ul">${items}</ul>`
  })

  // 12. 有序列表
  html = html.replace(/^((?:\d+\. .+(?:\n|$))+)/gm, (block) => {
    const items = block
      .trim()
      .split('\n')
      .map((line) => `<li class="md-li">${line.replace(/^\d+\. /, '')}</li>`)
      .join('')
    return `<ol class="md-ol">${items}</ol>`
  })

  // 13. 段落
  html = html
    .split(/\n{2,}/)
    .map((block) => {
      block = block.trim()
      if (!block) return ''
      if (/^<(h\d|pre|blockquote|ul|ol|img|div|hr|table)/i.test(block)) return block
      // CODEBLOCK/INLINECODE 占位符:trim 后两侧空格丢失,检测时不带空格
      if (/^CODEBLOCK\d+$/.test(block)) return block
      return `<p class="md-p">${block.replace(/\n/g, '<br />')}</p>`
    })
    .join('')

  // 14. 还原占位符(trim 后可能丢空格,所以两侧空格都可选)
  html = html.replace(/ ?CODEBLOCK(\d+) ?/g, (_, idx) => codeBlockStash[Number(idx)] || '')
  html = html.replace(/ ?INLINECODE(\d+) ?/g, (_, idx) => inlineCodeStash[Number(idx)] || '')

  return html
}

// ============ HTML 转义 ============

function escapeHtml(text) {
  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
}

// ============ 表格工具 ============

function splitRow(line) {
  const cells = line.split('|')
  if (cells.length && cells[0].trim() === '') cells.shift()
  if (cells.length && cells[cells.length - 1].trim() === '') cells.pop()
  return cells
}

function alignCell(cell) {
  const trimmed = cell.trim()
  const startsWithColon = trimmed.startsWith(':')
  const endsWithColon = trimmed.endsWith(':')
  if (startsWithColon && endsWithColon) return 'center'
  if (endsWithColon) return 'right'
  if (startsWithColon) return 'left'
  return ''
}

function alignStyle(a) {
  if (!a) return ''
  return ` style="text-align:${a};"`
}

// ============ 语法高亮(VS Code Dark+ 配色) ============
//
// 配色:
//   关键字 (function/const/if 等) → #569cd6 蓝
//   字符串 ("..." '...' `...`)      → #ce9178 橙
//   数字                            → #b5cea8 浅绿
//   注释 (// /* */ #)               → #6a9955 灰绿
//   函数名                          → #dcdcaa 浅黄
//   默认                            → 由 pre 标签 color 决定(本项目用近白)
//
// 实现策略:占位符(私用区字符 U+E000/U+E001)保护已着色片段,避免重复匹配。
// 匹配顺序:注释 → 字符串 → 数字 → 关键字 → 函数调用

const KEYWORDS_COMMON = ['true', 'false', 'null', 'undefined']

const KEYWORDS_BY_LANG = {
  js: ['function', 'const', 'let', 'var', 'if', 'else', 'for', 'while', 'do', 'switch', 'case', 'break', 'continue', 'return', 'import', 'export', 'from', 'default', 'class', 'extends', 'new', 'this', 'super', 'async', 'await', 'try', 'catch', 'finally', 'throw', 'typeof', 'instanceof', 'in', 'of', 'delete', 'void', 'yield', 'static'],
  ts: ['function', 'const', 'let', 'var', 'if', 'else', 'for', 'while', 'do', 'switch', 'case', 'break', 'continue', 'return', 'import', 'export', 'from', 'default', 'class', 'extends', 'new', 'this', 'super', 'async', 'await', 'try', 'catch', 'finally', 'throw', 'typeof', 'instanceof', 'in', 'of', 'delete', 'void', 'yield', 'static', 'interface', 'type', 'enum', 'public', 'private', 'protected', 'readonly', 'implements', 'as', 'is', 'keyof', 'declare', 'namespace'],
  py: ['def', 'class', 'if', 'elif', 'else', 'for', 'while', 'return', 'import', 'from', 'as', 'lambda', 'try', 'except', 'finally', 'raise', 'with', 'in', 'not', 'and', 'or', 'pass', 'continue', 'break', 'yield', 'global', 'nonlocal', 'is', 'None', 'True', 'False', 'self'],
  java: ['public', 'private', 'protected', 'class', 'interface', 'void', 'int', 'long', 'short', 'byte', 'char', 'float', 'double', 'boolean', 'String', 'if', 'else', 'for', 'while', 'do', 'switch', 'case', 'break', 'continue', 'return', 'import', 'package', 'new', 'this', 'super', 'try', 'catch', 'finally', 'throw', 'throws', 'static', 'final', 'abstract', 'extends', 'implements', 'instanceof', 'enum'],
  go: ['func', 'var', 'const', 'type', 'struct', 'interface', 'if', 'else', 'for', 'range', 'return', 'package', 'import', 'defer', 'go', 'select', 'switch', 'case', 'break', 'continue', 'chan', 'map', 'nil'],
  rust: ['fn', 'let', 'mut', 'const', 'static', 'struct', 'enum', 'impl', 'trait', 'pub', 'use', 'mod', 'if', 'else', 'match', 'for', 'while', 'loop', 'return', 'break', 'continue', 'self', 'Self', 'Some', 'None', 'Ok', 'Err', 'as', 'in', 'move', 'ref', 'where'],
  sh: ['if', 'then', 'else', 'elif', 'fi', 'for', 'while', 'do', 'done', 'case', 'esac', 'function', 'return', 'in', 'echo', 'export', 'source', 'local'],
  bash: ['if', 'then', 'else', 'elif', 'fi', 'for', 'while', 'do', 'done', 'case', 'esac', 'function', 'return', 'in', 'echo', 'export', 'source', 'local'],
  sql: ['SELECT', 'FROM', 'WHERE', 'INSERT', 'INTO', 'VALUES', 'UPDATE', 'SET', 'DELETE', 'CREATE', 'TABLE', 'DROP', 'ALTER', 'ADD', 'COLUMN', 'INDEX', 'JOIN', 'INNER', 'LEFT', 'RIGHT', 'OUTER', 'ON', 'AS', 'AND', 'OR', 'NOT', 'NULL', 'IS', 'IN', 'LIKE', 'BETWEEN', 'GROUP', 'BY', 'HAVING', 'ORDER', 'LIMIT', 'OFFSET', 'UNION', 'DISTINCT']
}

const LANG_ALIAS = {
  javascript: 'js',
  typescript: 'ts',
  python: 'py',
  golang: 'go',
  shell: 'sh',
  ruby: 'py',
  rb: 'py'
}

const HASH_COMMENT_LANGS = new Set(['py', 'sh', 'bash', 'ruby', 'rb', 'yaml', 'yml', 'r', 'perl'])

function highlightCode(rawCode, lang) {
  let html = escapeHtml(rawCode)

  // 占位符策略:用私用区控制字符 + 字母前缀 K + 索引 + 控制字符
  //   例如:U+E000 + 'K' + '12' + U+E001
  //   字母 K 紧贴数字,使 \b\d+\b 数字正则无法误吃中间的数字索引
  const stash = []
  function preserve(content, color) {
    stash.push(`<span style="color:${color}">${content}</span>`)
    return `${PH_OPEN}K${stash.length - 1}${PH_CLOSE}`
  }

  const langKey = LANG_ALIAS[lang] || lang

  // 1. 块注释 /* ... */
  html = html.replace(/\/\*[\s\S]*?\*\//g, (m) => preserve(m, '#6a9955'))

  // 2. 行注释 // ...(避免 http:// 误判)
  html = html.replace(/(^|[^:])(\/\/[^\n]*)/g, (_, prefix, comment) => {
    return prefix + preserve(comment, '#6a9955')
  })

  // 3. # 注释(仅特定语言)
  if (HASH_COMMENT_LANGS.has(langKey)) {
    html = html.replace(/#[^\n]*/g, (m) => preserve(m, '#6a9955'))
  }

  // 4. 字符串 "..."
  html = html.replace(/&quot;(?:[^&\n]|&(?!quot;))*?&quot;/g, (m) => preserve(m, '#ce9178'))
  // 5. 字符串 '...'
  html = html.replace(/&#39;(?:[^&\n]|&(?!#39;))*?&#39;/g, (m) => preserve(m, '#ce9178'))
  // 6. 模板字符串 `...`
  html = html.replace(/`[^`\n]*?`/g, (m) => preserve(m, '#ce9178'))

  // 7. 数字
  html = html.replace(/\b\d+(?:\.\d+)?\b/g, (m) => preserve(m, '#b5cea8'))

  // 8. 关键字
  const keywords = KEYWORDS_BY_LANG[langKey] || []
  const allKeywords = keywords.concat(KEYWORDS_COMMON)
  if (allKeywords.length) {
    const reg = new RegExp(`\\b(${allKeywords.join('|')})\\b`, 'g')
    html = html.replace(reg, (m) => preserve(m, '#569cd6'))
  }

  // 9. 函数调用
  html = html.replace(/\b([A-Za-z_$][\w$]*)(?=\s*\()/g, (m) => preserve(m, '#dcdcaa'))

  // 10. 还原占位符
  const stashReg = new RegExp(`${PH_OPEN}K(\\d+)${PH_CLOSE}`, 'g')
  html = html.replace(stashReg, (_, idx) => stash[Number(idx)] || '')

  return html
}
