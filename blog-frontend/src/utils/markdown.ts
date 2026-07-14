/**
 * Markdown 渲染工具
 */
import { marked } from 'marked'

// 配置 marked
marked.setOptions({
  breaks: true,       // 支持换行符
  gfm: true,          // 支持 GitHub Flavored Markdown
})

/**
 * 渲染 Markdown 为 HTML
 */
export function renderMarkdown(content: string): string {
  if (!content) return ''
  const html = marked.parse(content) as string
  return html
}
