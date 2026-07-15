import request from './request'
import type {
  AiConfigDTO,
  AiTestConnectionResult,
  Result,
  AiTaskType
} from '@/types'

export interface SseEventHandlers {
  onMeta?: (data: Record<string, any>) => void
  onProgress?: (data: { stage?: string; percent?: number }) => void
  onToken?: (data: { text: string; seq: number }) => void
  onDone?: (data: {
    text: string
    usage?: { promptTokens: number; completionTokens: number; totalTokens?: number }
    latencyMs: number
    logId: number
    requestId?: string
  }) => void
  onError?: (data: { code: string; message: string; retryable: boolean }) => void
  onFinally?: () => void
}

export const aiApi = {
  getAdminConfig() {
    return request.get('/api/admin/ai/config') as Promise<Result<AiConfigDTO>>
  },

  testConnection() {
    return request.post('/api/admin/ai/test') as Promise<Result<AiTestConnectionResult>>
  },

  buildGenerateStreamUrl(params: {
    taskType: AiTaskType
    articleId?: number
    title?: string
    content?: string
    summary?: string
    tags?: string[] | string
  }) {
    const base = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080').replace(/\/$/, '')
    const url = new URL(`${base}/api/admin/ai/generate`)
    const tagsStr = Array.isArray(params.tags)
      ? params.tags.join(',')
      : params.tags ?? ''
    const entries: [string, any][] = [
      ['taskType', params.taskType],
      ['articleId', params.articleId],
      ['title', params.title ?? ''],
      ['content', params.content ?? ''],
      ['summary', params.summary ?? ''],
      ['tags', tagsStr]
    ]
    entries.forEach(([k, v]) => {
      if (v === undefined || v === null) return
      url.searchParams.set(k, String(v))
    })
    return url.toString()
  },

  async runGenerateStream(
    params: {
      taskType: AiTaskType
      articleId?: number
      title?: string
      content?: string
      summary?: string
      tags?: string[] | string
    },
    handlers: SseEventHandlers = {}
  ): Promise<void> {
    const url = aiApi.buildGenerateStreamUrl(params)
    const token = localStorage.getItem('token')

    const resp = await fetch(url, {
      method: 'GET',
      headers: {
        Accept: 'text/event-stream',
        ...(token ? { Authorization: `Bearer ${token}` } : {})
      },
      credentials: 'same-origin'
    })

    if (!resp.ok) {
      let message = `HTTP ${resp.status}`
      try {
        const txt = await resp.text()
        if (txt) message += `: ${txt.slice(0, 200)}`
      } catch (_) {}
      handlers.onError?.({ code: 'HTTP_' + resp.status, message, retryable: true })
      handlers.onFinally?.()
      return
    }

    const reader = resp.body?.getReader?.()
    if (!reader) {
      handlers.onError?.({ code: 'NO_STREAM', message: '浏览器不支持流式读取', retryable: false })
      handlers.onFinally?.()
      return
    }

    const decoder = new TextDecoder('utf-8')
    let buffer = ''
    const flushEvent = (raw: string) => {
      if (!raw.trim()) return
      let eventName = 'message'
      let dataLines: string[] = []
      raw.split(/\r?\n/).forEach(line => {
        if (!line) return
        if (line.startsWith('event:')) {
          eventName = line.slice(6).trim()
        } else if (line.startsWith('data:')) {
          dataLines.push(line.slice(5).trim())
        }
      })
      const dataStr = dataLines.join('\n')
      if (!dataStr) return
      let parsed: any = null
      try {
        parsed = JSON.parse(dataStr)
      } catch (_) {
        parsed = dataStr
      }
      switch (eventName) {
        case 'meta':
          handlers.onMeta?.(parsed)
          break
        case 'progress':
          handlers.onProgress?.(parsed)
          break
        case 'token':
          handlers.onToken?.(parsed)
          break
        case 'done':
          handlers.onDone?.(parsed)
          break
        case 'error':
          handlers.onError?.(parsed)
          break
      }
    }

    try {
      while (true) {
        const { done, value } = await reader.read()
        if (done) break
        buffer += decoder.decode(value, { stream: true })
        let idx: number
        while ((idx = buffer.indexOf('\n\n')) !== -1) {
          const chunk = buffer.slice(0, idx)
          buffer = buffer.slice(idx + 2)
          flushEvent(chunk)
        }
      }
      if (buffer.trim()) flushEvent(buffer)
    } catch (e: any) {
      handlers.onError?.({ code: 'STREAM_ABORTED', message: e?.message ?? '流中断', retryable: true })
    } finally {
      try { reader.releaseLock() } catch (_) {}
      handlers.onFinally?.()
    }
  }
}
