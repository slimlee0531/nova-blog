import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { aiApi } from '@/api/ai'
import type {
  AiGenerationResult,
  AiTaskType,
  AiConfigDTO,
  AiTaskMeta,
  AiSidebarTab,
  ArticleCreateParams
} from '@/types'

const TASK_META: Record<AiTaskType, AiTaskMeta> = {
  SUMMARY: { icon: '📝', label: 'AI 摘要', group: '写作' },
  TITLE: { icon: '🏷️', label: '标题建议', group: '写作' },
  TAG: { icon: '🏷️', label: '智能打标', group: '写作' },
  POLISH: { icon: '✨', label: '智能润色', group: '优化' },
  CONTINUE: { icon: '➡️', label: '智能续写', group: '写作' },
  PROOFREAD: { icon: '🔍', label: '纠错检查', group: '优化' },
  REPHRASE: { icon: '🔁', label: '改写风格', group: '优化' },
  QUALITY_SCORE: { icon: '📊', label: '质量评分', group: '质量' },
  SEO_META: { icon: '🔎', label: 'SEO 优化', group: 'SEO' },
  SUGGEST_OUTLINE: { icon: '🧭', label: '大纲建议', group: '写作' },
  TRANSLATE: { icon: '🌐', label: '翻译', group: '优化' }
}

const ERROR_MESSAGE_MAP: Record<string, string> = {
  AI_DISABLED: 'AI 功能暂不可用，请先在后端配置启用并设置 ALIYUN_API_KEY',
  NO_PROVIDER_ENABLED: '请管理员先在 AI 设置中启用模型并填 Key',
  KEY_INVALID: '模型 Key 无效，请联系管理员检查 ALIYUN_API_KEY',
  QUOTA_EXHAUSTED: '模型额度已用尽，请联系管理员充值',
  PROVIDER_TIMEOUT: '生成超时，已中断（可重试一次）',
  PROVIDER_RATE_LIMIT: '模型忙，请稍后重试',
  BAD_PARAMS: '输入参数有误，请检查是否有内容或标题',
  HTTP_401: '登录已过期，请刷新页面重新登录'
}

export const useAiStore = defineStore('ai', () => {
  const sidebarVisible = ref(true)
  const sidebarWidth = ref(380)
  const activeTab = ref<AiSidebarTab>('tasks')
  const activeTask = ref<AiTaskType>('SUMMARY')
  const enabled = ref(true)
  const config = ref<AiConfigDTO | null>(null)
  const testLoading = ref(false)
  const generating = ref(false)
  const generations = ref<AiGenerationResult[]>([])
  const currentGeneration = ref<AiGenerationResult | null>(null)
  const qualityScore = ref<number | null>(null)

  const taskList = computed(() =>
    (Object.keys(TASK_META) as AiTaskType[]).map(k => ({
      type: k,
      ...TASK_META[k]
    }))
  )

  const taskGroups = computed(() => {
    const groups: Record<string, { type: AiTaskType; meta: AiTaskMeta }[]> = {}
    ;(Object.keys(TASK_META) as AiTaskType[]).forEach(k => {
      const group = TASK_META[k].group
      if (!groups[group]) groups[group] = []
      groups[group].push({ type: k, meta: TASK_META[k] })
    })
    return groups
  })

  const toggleSidebar = () => {
    sidebarVisible.value = !sidebarVisible.value
  }

  const setSidebarWidth = (w: number) => {
    const clamped = Math.max(280, Math.min(640, w))
    sidebarWidth.value = clamped
  }

  const setActiveTab = (tab: AiSidebarTab) => {
    activeTab.value = tab
  }

  const fetchConfig = async () => {
    try {
      const res = await aiApi.getAdminConfig()
      if (res.code === 200) {
        config.value = res.data
        enabled.value = !!res.data?.enabled ?? false
      }
    } catch (e) {
      enabled.value = false
    }
  }

  const runTestConnection = async () => {
    testLoading.value = true
    try {
      const res = await aiApi.testConnection()
      if (res.code === 200) {
        return res.data
      }
      return null as any
    } finally {
      testLoading.value = false
    }
  }

  const startGeneration = (taskType: AiTaskType, inputSnapshot?: Record<string, any>) => {
    const gen: AiGenerationResult = {
      id: `${taskType}-${Date.now()}`,
      taskType,
      status: 'generating',
      inputSnapshot,
      fullText: '',
      streamingText: '',
      createdAt: Date.now()
    }
    currentGeneration.value = gen
    generations.value.unshift(gen)
    generations.value = generations.value.slice(0, 20)
    return gen
  }

  const appendStreamingToken = (id: string, text: string) => {
    const gen = findGen(id)
    if (!gen) return
    gen.streamingText += text
  }

  const finishGeneration = (
    id: string,
    payload: {
      fullText: string
      tokens?: { promptTokens: number; completionTokens: number }
      latencyMs: number
      logId: number
    }
  ) => {
    const gen = findGen(id)
    if (!gen) return
    gen.status = 'done'
    gen.fullText = payload.fullText
    gen.streamingText = payload.fullText
    gen.tokens = payload.tokens
    gen.latencyMs = payload.latencyMs
    gen.logId = payload.logId
    if (gen.taskType === 'QUALITY_SCORE') {
      const match = payload.fullText.match(/(\d{1,3})(\s*\/\s*100)?/)
      if (match) qualityScore.value = Math.min(100, Math.max(0, Number(match[1])))
    }
  }

  const failGeneration = (id: string, errorCode: string, errorMessage: string) => {
    const gen = findGen(id)
    if (!gen) return
    gen.status = 'error'
    gen.errorCode = errorCode
    gen.errorMessage = errorMessage
  }

  const clearHistory = () => {
    generations.value = []
    currentGeneration.value = null
    qualityScore.value = null
  }

  function findGen(id: string) {
    return generations.value.find(g => g.id === id)
  }

  async function runTask(
    taskType: AiTaskType,
    payload: {
      articleId?: number
      form: ArticleCreateParams
      selectedText?: string
    }
  ) {
    if (generating.value) {
      ElMessage.warning('已有任务正在生成，请等待完成')
      return
    }
    if (!enabled.value) {
      ElMessage.error('AI 功能未启用')
      return
    }
    const contentToCheck = payload.selectedText || payload.form.content
    if (
      (taskType === 'SUMMARY' ||
        taskType === 'TITLE' ||
        taskType === 'TAG' ||
        taskType === 'POLISH' ||
        taskType === 'CONTINUE' ||
        taskType === 'PROOFREAD' ||
        taskType === 'REPHRASE') &&
      (!contentToCheck || contentToCheck.length < 20)
    ) {
      ElMessage.warning('请先输入至少 20 字正文，再使用此功能')
      return
    }
    setActiveTab('result')
    const snapshot: Record<string, any> = {
      title: payload.form.title ?? '',
      summary: payload.form.summary ?? '',
      contentLen: payload.form.content?.length ?? 0,
      tags: [...(payload.form.tags ?? [])],
      selectedTextLen: payload.selectedText?.length ?? 0
    }
    const gen = startGeneration(taskType, snapshot)
    generating.value = true

    try {
      await aiApi.runGenerateStream(
        {
          taskType,
          articleId: payload.articleId,
          title: payload.form.title ?? '',
          content: (payload.selectedText || payload.form.content) ?? '',
          summary: payload.form.summary ?? '',
          tags: payload.form.tags ?? []
        },
        {
          onToken: data => {
            if (data?.text) appendStreamingToken(gen.id, data.text)
          },
          onDone: data => {
            finishGeneration(gen.id, {
              fullText: data.text,
              tokens: data.usage
                ? {
                    promptTokens: data.usage.promptTokens ?? 0,
                    completionTokens: data.usage.completionTokens ?? 0
                  }
                : undefined,
              latencyMs: data.latencyMs ?? 0,
              logId: data.logId ?? -1
            })
            const label = TASK_META[taskType]?.label ?? taskType
            const len = (data.text || '').length
            ElMessage.success(`${label}完成（${len} 字，${data.latencyMs ?? 0}ms）`)
          },
          onError: data => {
            const code = data?.code ?? 'UNKNOWN'
            const userMsg = ERROR_MESSAGE_MAP[code] ?? data?.message ?? '未知错误'
            failGeneration(gen.id, code, data?.message ?? userMsg)
            ElMessage.error(`${TASK_META[taskType]?.label ?? taskType}失败：${userMsg}`)
          },
          onFinally: () => {
            generating.value = false
          }
        }
      )
    } catch (e: any) {
      failGeneration(gen.id, 'UNKNOWN', e?.message ?? '调用异常')
      generating.value = false
    }
  }

  return {
    sidebarVisible,
    sidebarWidth,
    activeTab,
    activeTask,
    enabled,
    config,
    testLoading,
    generating,
    generations,
    currentGeneration,
    qualityScore,
    taskList,
    taskGroups,
    toggleSidebar,
    setSidebarWidth,
    setActiveTab,
    fetchConfig,
    runTestConnection,
    startGeneration,
    appendStreamingToken,
    finishGeneration,
    failGeneration,
    clearHistory,
    runTask,
    TASK_META
  }
})

