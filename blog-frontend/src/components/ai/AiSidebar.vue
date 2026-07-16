<script setup lang="ts">
import { computed, onMounted, ref, watch, nextTick } from 'vue'
import { useAiStore } from '@/store/ai'
import type { AiTaskType, ArticleCreateParams, AiGenerationResult } from '@/types'
import { ElMessage, ElTag } from 'element-plus'
import { renderMarkdown } from '@/utils/markdown'

interface Props {
  form?: ArticleCreateParams
  articleId?: number
  selectedText?: string
}

const props = withDefaults(defineProps<Props>(), {
  selectedText: ''
})
const emit = defineEmits<{
  (e: 'apply', payload: { taskType: AiTaskType; result: AiGenerationResult; fullText: string; action: 'replace' | 'append' | 'merge' }): void
}>()

const aiStore = useAiStore()
const testLastResult = ref<{ ok: boolean; text?: string; latencyMs?: number; error?: string } | null>(null)
const testLastRunAt = ref<number | null>(null)

const showIframeFallback = ref(false)
const genTextRef = ref<HTMLDivElement | null>(null)

const visible = computed(() => aiStore.sidebarVisible)
const width = computed(() => aiStore.sidebarWidth)
const activeTab = computed({
  get: () => aiStore.activeTab,
  set: (v) => aiStore.setActiveTab(v)
})

const isResizing = ref(false)
const startX = ref(0)
const startW = ref(380)

const startResize = (e: MouseEvent) => {
  isResizing.value = true
  startX.value = e.clientX
  startW.value = width.value
  document.body.style.cursor = 'col-resize'
  document.body.style.userSelect = 'none'
  window.addEventListener('mousemove', onResizeMove)
  window.addEventListener('mouseup', stopResize)
}

const onResizeMove = (e: MouseEvent) => {
  if (!isResizing.value) return
  const delta = startX.value - e.clientX
  aiStore.setSidebarWidth(startW.value + delta)
}

const stopResize = () => {
  isResizing.value = false
  document.body.style.cursor = ''
  document.body.style.userSelect = ''
  window.removeEventListener('mousemove', onResizeMove)
  window.removeEventListener('mouseup', stopResize)
}

const handleTestConnection = async () => {
  testLastResult.value = null
  try {
    const result = await aiStore.runTestConnection()
    if (result) {
      testLastResult.value = result as any
      testLastRunAt.value = Date.now()
      if (result.ok) {
        ElMessage.success(`连通性测试成功（${result.latencyMs ?? 0}ms）`)
      } else {
        ElMessage.error(`连通性测试失败：${result.error ?? '未知错误'}`)
      }
    }
  } catch (e: any) {
    testLastResult.value = { ok: false, error: e?.message ?? '请求异常' }
    testLastRunAt.value = Date.now()
    ElMessage.error(`连通性测试请求失败：${e?.message ?? '请检查后端是否启动'}`)
  }
}

const runTask = async (taskType: AiTaskType) => {
  if (!props.form) {
    ElMessage.warning('请在写文章页面使用 AI 任务')
    return
  }
  await aiStore.runTask(taskType, {
    articleId: props.articleId,
    form: props.form,
    selectedText: props.selectedText
  })
}

const applyToForm = (gen: AiGenerationResult, action: 'replace' | 'append' | 'merge' = 'replace') => {
  if (gen.status !== 'done' || !gen.fullText) {
    ElMessage.warning('请等生成完成后再应用结果')
    return
  }
  emit('apply', { taskType: gen.taskType, result: gen, fullText: gen.fullText, action })
}

const applyTextToTitle = () => {
  const latest = latestByTask('TITLE')
  if (latest) applyToForm(latest, 'replace')
}
const applyTextToSummary = () => {
  const latest = latestByTask('SUMMARY')
  if (latest) applyToForm(latest, 'replace')
}
const applyTextToTags = () => {
  const latest = latestByTask('TAG')
  if (latest) applyToForm(latest, 'merge')
}

function latestByTask(taskType: AiTaskType) {
  return aiStore.generations.find(g => g.taskType === taskType && g.status === 'done')
}

const statusLabel = (s: AiGenerationResult['status']) =>
  ({ idle: '待生成', generating: '生成中', done: '已完成', error: '失败' }[s])

const statusTagType = (s: AiGenerationResult['status']) =>
  ({ idle: 'info', generating: 'warning', done: 'success', error: 'danger' } as const)[s]

const formatLastRun = (ts: number | null) => {
  if (!ts) return '未运行'
  const d = new Date(ts)
  return d.toLocaleTimeString()
}

const displayText = (gen: AiGenerationResult) =>
  gen.status === 'generating' ? gen.streamingText : gen.fullText

const textLength = computed(() => {
  if (!aiStore.currentGeneration) return 0
  const text = displayText(aiStore.currentGeneration)
  return text ? text.length : 0
})

const copyToClipboard = async () => {
  if (!aiStore.currentGeneration) return
  const text = displayText(aiStore.currentGeneration)
  if (!text) {
    ElMessage.warning('没有可复制的内容')
    return
  }
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success('已复制到剪贴板')
  } catch {
    ElMessage.error('复制失败')
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (genTextRef.value) {
      genTextRef.value.scrollTop = genTextRef.value.scrollHeight
    }
  })
}

watch(() => aiStore.currentGeneration?.status, (status) => {
  if (status === 'generating') {
    scrollToBottom()
  }
})

watch(() => aiStore.currentGeneration?.streamingText, () => {
  scrollToBottom()
})

const healthBadge = computed(() => {
  if (!aiStore.config?.apiKeySet) return { type: 'danger' as const, label: '未填 Key' }
  if (!aiStore.enabled) return { type: 'warning' as const, label: '未启用' }
  return { type: 'success' as const, label: '已就绪' }
})

onMounted(() => {
  aiStore.fetchConfig()
})
</script>

<template>
  <div class="ai-sidebar-wrap" :style="{ width: visible ? `${width}px` : '40px' }">
    <!-- 折叠态悬浮按钮 -->
    <button
      v-if="!visible"
      class="ai-sidebar-toggle collapsed"
      @click="aiStore.toggleSidebar()"
      title="展开 AI 侧边栏"
    >
      🤖
    </button>

    <div v-show="visible" class="ai-sidebar-panel">
      <div class="ai-sidebar-header">
        <div class="ai-sidebar-title">
          <span class="ai-robot">🤖</span>
          <span>Nova AI 写作助手</span>
          <el-tag size="small" :type="healthBadge.type" effect="light">
            {{ healthBadge.label }}
          </el-tag>
        </div>
        <button class="collapse-btn" @click="aiStore.toggleSidebar()" title="收起侧边栏">
          ⏩
        </button>
      </div>

      <el-tabs v-model="activeTab" class="ai-sidebar-tabs" stretch>
        <!-- =========== Tab 1：任务 =========== -->
        <el-tab-pane name="tasks">
          <template #label>
            <span>🧰 任务</span>
          </template>

          <section class="ai-section health-card">
            <div class="health-row">
              <div class="health-label">当前 Provider</div>
              <div class="health-value">
                {{ aiStore.config?.providerName ?? aiStore.config?.defaultProvider ?? '加载中…' }}
                <span class="sub">/ {{ aiStore.config?.model ?? '—' }}</span>
              </div>
            </div>
            <div class="health-row">
              <div class="health-label">API Key</div>
              <div class="health-value mono">
                {{ aiStore.config?.apiKeyMasked || (aiStore.config?.apiKeySet ? '已设置' : '未设置') }}
              </div>
            </div>
            <div class="health-row">
              <div class="health-label">最近一次连通性测试</div>
              <div class="health-value">
                {{ formatLastRun(testLastRunAt) }}
                <el-tag v-if="testLastResult" size="small" class="ml-8" :type="testLastResult.ok ? 'success' : 'danger'">
                  {{ testLastResult.ok ? `成功 ${testLastResult.latencyMs}ms` : '失败' }}
                </el-tag>
              </div>
            </div>
            <div class="health-actions">
              <el-button size="default" type="primary" :loading="aiStore.testLoading" @click="handleTestConnection">
                {{ aiStore.testLoading ? '测试中…' : '一键连通性测试' }}
              </el-button>
            </div>
            <div v-if="testLastResult?.error" class="health-error">
              ❌ {{ testLastResult.error }}
            </div>
            <div v-else-if="testLastResult?.ok" class="health-success">
              ✅ 模型返回：{{ testLastResult.text }}
            </div>
          </section>

          <section v-for="(items, group) in aiStore.taskGroups" :key="group" class="ai-section task-group">
            <h4 class="group-title">{{ group }}</h4>
            <div class="task-grid">
              <button
                v-for="item in items"
                :key="item.type"
                class="task-btn"
                :class="[item.type === aiStore.activeTask ? 'active' : '', { 'is-running': aiStore.generating && aiStore.currentGeneration?.taskType === item.type }]"
                :disabled="!aiStore.enabled || !aiStore.config?.apiKeySet || aiStore.generating"
                @click="runTask(item.type)"
              >
                <span class="task-icon">{{ item.meta.icon }}</span>
                <span class="task-label">
                  {{ item.meta.label }}
                  <span v-if="aiStore.generating && aiStore.currentGeneration?.taskType === item.type" class="running-dot">●</span>
                </span>
              </button>
            </div>
          </section>

          <section class="ai-section tip-card">
            <div class="tip-title">💡 使用提示</div>
            <ul>
              <li>先在文章里输入标题或正文，再点对应任务，生成结果更准确</li>
              <li>「润色 / 改写 / 续写」在正文选中一段文字后触发效果最佳</li>
              <li>结果页可查看流式输出 + 一键应用到对应字段（标题 / 摘要 / 标签）</li>
            </ul>
            <div v-if="selectedText" class="selected-text-hint">
              <div class="selected-text-label">📌 当前选中 {{ selectedText.length }} 字</div>
              <div class="selected-text-preview">{{ selectedText.slice(0, 50) }}{{ selectedText.length > 50 ? '...' : '' }}</div>
            </div>
          </section>
        </el-tab-pane>

        <!-- =========== Tab 2：结果 =========== -->
        <el-tab-pane name="result">
          <template #label>
            <span>📄 结果
              <span v-if="aiStore.currentGeneration?.status === 'generating'" class="dot-blink">•</span>
            </span>
          </template>

          <section v-if="!aiStore.generations.length" class="ai-section empty">
            <div class="empty-hero">🪄</div>
            <p>还没有 AI 生成记录</p>
            <p class="sub">切回「任务」Tab，点任意任务按钮开始生成</p>
          </section>

          <section v-else-if="aiStore.currentGeneration" class="ai-section current-gen">
            <header class="gen-header">
              <div class="gen-title">
                <span class="gen-icon">{{ aiStore.TASK_META[aiStore.currentGeneration.taskType]?.icon }}</span>
                <span>{{ aiStore.TASK_META[aiStore.currentGeneration.taskType]?.label }}</span>
              </div>
              <div class="gen-header-right">
                <el-tag size="small" :type="statusTagType(aiStore.currentGeneration.status)">
                  {{ statusLabel(aiStore.currentGeneration.status) }}
                </el-tag>
                <button class="copy-btn" @click="copyToClipboard" title="复制">
                  📋
                </button>
              </div>
            </header>

            <div
              ref="genTextRef"
              class="gen-text-box"
              :class="{ 'is-streaming': aiStore.currentGeneration.status === 'generating' }"
            >
              <template v-if="aiStore.currentGeneration.status === 'error'">
                <div class="error-box">
                  <strong>错误：{{ aiStore.currentGeneration.errorCode }}</strong>
                  <p>{{ aiStore.currentGeneration.errorMessage }}</p>
                </div>
              </template>
              <template v-else-if="displayText(aiStore.currentGeneration)">
                <div v-if="aiStore.currentGeneration.status === 'done'" class="gen-markdown-content" v-html="renderMarkdown(displayText(aiStore.currentGeneration) || '')"></div>
                <template v-else>
                  {{ displayText(aiStore.currentGeneration) }}
                  <span class="stream-caret">▊</span>
                </template>
              </template>
              <template v-else>
                <div class="gen-placeholder">正在生成…</div>
              </template>
            </div>

            <div class="gen-meta">
              <span v-if="aiStore.currentGeneration.tokens">
                prompt {{ aiStore.currentGeneration.tokens.promptTokens }} / completion {{ aiStore.currentGeneration.tokens.completionTokens }}
              </span>
              <span v-if="aiStore.currentGeneration.latencyMs">
                耗时 {{ aiStore.currentGeneration.latencyMs }}ms
              </span>
              <span v-if="textLength > 0">
                {{ textLength }} 字
              </span>
            </div>

            <div class="gen-actions">
              <template v-if="aiStore.currentGeneration.taskType === 'TITLE'">
                <el-button size="small" type="primary" @click="applyToForm(aiStore.currentGeneration, 'replace')">
                  覆盖标题
                </el-button>
                <el-button size="small" @click="applyToForm(aiStore.currentGeneration, 'append')">
                  追加到标题
                </el-button>
              </template>

              <template v-else-if="aiStore.currentGeneration.taskType === 'SUMMARY'">
                <el-button size="small" type="primary" @click="applyToForm(aiStore.currentGeneration, 'replace')">
                  覆盖摘要
                </el-button>
                <el-button size="small" @click="applyToForm(aiStore.currentGeneration, 'append')">
                  追加到摘要
                </el-button>
              </template>

              <template v-else-if="aiStore.currentGeneration.taskType === 'TAG'">
                <el-button size="small" @click="applyToForm(aiStore.currentGeneration, 'replace')">
                  替换标签
                </el-button>
                <el-button size="small" type="primary" @click="applyToForm(aiStore.currentGeneration, 'append')">
                  追加标签
                </el-button>
                <el-button size="small" @click="applyToForm(aiStore.currentGeneration, 'merge')">
                  智能合并
                </el-button>
              </template>

              <template v-else-if="aiStore.currentGeneration.taskType === 'POLISH'">
                <el-button size="small" @click="applyToForm(aiStore.currentGeneration, 'replace')">
                  替换选中内容
                </el-button>
                <el-button size="small" type="primary" @click="applyToForm(aiStore.currentGeneration, 'replace')">
                  覆盖整篇文章
                </el-button>
                <el-button size="small" @click="applyToForm(aiStore.currentGeneration, 'append')">
                  追加到末尾
                </el-button>
              </template>

              <template v-else-if="aiStore.currentGeneration.taskType === 'CONTINUE'">
                <el-button size="small" type="primary" @click="applyToForm(aiStore.currentGeneration, 'append')">
                  追加到末尾
                </el-button>
                <el-button size="small" @click="applyToForm(aiStore.currentGeneration, 'replace')">
                  插入到光标位置
                </el-button>
              </template>

              <template v-else-if="aiStore.currentGeneration.taskType === 'REPHRASE'">
                <el-button size="small" type="primary" @click="applyToForm(aiStore.currentGeneration, 'replace')">
                  替换选中内容
                </el-button>
              </template>

              <template v-else-if="aiStore.currentGeneration.taskType === 'PROOFREAD'">
                <el-button size="small" type="primary" @click="applyToForm(aiStore.currentGeneration, 'replace')">
                  应用修改
                </el-button>
              </template>

              <template v-else-if="aiStore.currentGeneration.taskType === 'QUALITY_SCORE'">
                <el-button size="small" type="primary" @click="aiStore.setActiveTab('quality')">
                  查看详情
                </el-button>
              </template>

              <template v-else>
                <el-button size="small" @click="applyToForm(aiStore.currentGeneration, 'replace')">
                  覆盖
                </el-button>
                <el-button size="small" @click="applyToForm(aiStore.currentGeneration, 'append')">
                  追加
                </el-button>
              </template>
            </div>
          </section>

          <section v-if="aiStore.generations.length" class="ai-section history">
            <div class="history-title">
              <span>📚 历史（最近 20 条）</span>
              <el-button link size="small" @click="aiStore.clearHistory()">清空</el-button>
            </div>
            <ul class="history-list">
              <li
                v-for="gen in aiStore.generations.slice(0, 20)"
                :key="gen.id"
                class="history-item"
                :class="{ highlight: aiStore.currentGeneration?.id === gen.id }"
                @click="aiStore.currentGeneration = gen"
              >
                <div class="history-left">
                  <span class="h-icon">{{ aiStore.TASK_META[gen.taskType]?.icon }}</span>
                  <span class="h-label">{{ aiStore.TASK_META[gen.taskType]?.label }}</span>
                  <el-tag size="small" :type="statusTagType(gen.status)">{{ statusLabel(gen.status) }}</el-tag>
                </div>
                <div class="h-snippet">
                  {{ gen.fullText?.slice(0, 40) || '—' }}{{ gen.fullText?.length && gen.fullText.length > 40 ? '…' : '' }}
                </div>
              </li>
            </ul>
          </section>
        </el-tab-pane>

        <!-- =========== Tab 3：质量 =========== -->
        <el-tab-pane name="quality">
          <template #label><span>📊 质量</span></template>
          <section class="ai-section quality-card">
            <div class="quality-title">整体质量得分</div>
            <div v-if="aiStore.qualityScore != null" class="quality-score">
              <el-progress
                type="dashboard"
                :percentage="aiStore.qualityScore"
                :color="aiStore.qualityScore >= 80 ? '#67c23a' : aiStore.qualityScore >= 60 ? '#e6a23c' : '#f56c6c'"
              />
              <div class="quality-hint">
                <span v-if="aiStore.qualityScore >= 90">🎉 非常好！几乎可以直接发布</span>
                <span v-else-if="aiStore.qualityScore >= 80">👍 不错，建议补充细节</span>
                <span v-else-if="aiStore.qualityScore >= 60">⚠️ 建议润色 / 加标签摘要</span>
                <span v-else>🚧 需要大幅优化（建议 AI 润色）</span>
              </div>
            </div>
            <div v-else class="quality-empty">
              <div class="quality-empty-hero">📝</div>
              <p>暂无质量评分</p>
              <p class="sub">切回「任务」Tab → 点「📊 质量评分」即可生成</p>
            </div>

            <div class="quality-rule-list">
              <h5>📋 合规扣分规则（本地实时统计）</h5>
              <ul>
                <li>正文字数 {{ props.form?.content?.length ?? 0 }} 字（推荐 ≥ 500 字）</li>
                <li>标题长度 {{ props.form?.title?.length ?? 0 }} 字（推荐 8-30 字）</li>
                <li>摘要长度 {{ props.form?.summary?.length ?? 0 }} 字（推荐 80-200 字）</li>
                <li>标签数量 {{ props.form?.tags?.length ?? 0 }} 个（推荐 3-6 个）</li>
              </ul>
            </div>
          </section>
        </el-tab-pane>

        <!-- =========== Tab 4：设置 =========== -->
        <el-tab-pane name="settings">
          <template #label><span>⚙️ 设置</span></template>
          <section class="ai-section settings-card">
            <div class="settings-row">
              <div class="settings-label">全局 AI 功能</div>
              <div class="settings-value">
                <el-tag :type="aiStore.enabled ? 'success' : 'danger'">
                  {{ aiStore.enabled ? '已开启（后端配置）' : '未开启' }}
                </el-tag>
              </div>
            </div>
            <div class="settings-row">
              <div class="settings-label">默认 Provider</div>
              <div class="settings-value mono">{{ aiStore.config?.defaultProvider ?? '—' }}</div>
            </div>
            <div class="settings-row">
              <div class="settings-label">模型</div>
              <div class="settings-value mono">{{ aiStore.config?.model ?? '—' }}</div>
            </div>
            <div class="settings-row">
              <div class="settings-label">超时</div>
              <div class="settings-value">{{ aiStore.config?.timeoutSeconds ?? 30 }} s</div>
            </div>
            <div class="settings-row">
              <div class="settings-label">侧边栏宽度</div>
              <div class="settings-value">
                <el-slider
                  :model-value="width"
                  :min="280"
                  :max="640"
                  :step="10"
                  :show-tooltip="true"
                  @update:model-value="(v:number) => aiStore.setSidebarWidth(v)"
                />
              </div>
            </div>
            <div class="settings-actions">
              <el-button type="primary" @click="aiStore.fetchConfig">重新拉取配置</el-button>
              <el-button @click="handleTestConnection">连通性测试</el-button>
            </div>
          </section>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 拖拽分隔条（左侧边） -->
    <div
      v-if="visible"
      class="ai-sidebar-resizer"
      @mousedown="startResize"
      :class="{ resizing: isResizing }"
    />
  </div>
</template>

<style scoped>
.ai-sidebar-wrap {
  position: sticky;
  top: 0;
  align-self: flex-start;
  flex-shrink: 0;
  z-index: 5;
  border-left: 1px solid var(--color-border);
  background: var(--color-bg-card);
  height: calc(100vh - var(--header-height, 64px));
  min-height: 600px;
  display: flex;
  overflow: visible;
}

.ai-sidebar-toggle.collapsed {
  width: 40px;
  height: 40px;
  margin: 12px auto;
  border-radius: 12px;
  border: 1px solid var(--color-border);
  background: var(--color-primary);
  color: #fff;
  font-size: 18px;
  cursor: pointer;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
}
.ai-sidebar-toggle.collapsed:hover { filter: brightness(1.1); }

.ai-sidebar-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.ai-sidebar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  border-bottom: 1px solid var(--color-border);
}
.ai-sidebar-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: var(--color-text);
}
.ai-robot { font-size: 18px; }

.collapse-btn {
  width: 28px; height: 28px;
  border-radius: 6px;
  border: 1px solid var(--color-border);
  background: transparent;
  color: var(--color-text-muted);
  cursor: pointer;
  display: flex; align-items: center; justify-content: center;
}
.collapse-btn:hover { background: var(--color-primary-bg); color: var(--color-primary); }

.ai-sidebar-tabs {
  flex: 1;
  min-height: 0;
  padding: 8px 16px 16px;
  overflow-y: auto;
}

.ai-sidebar-tabs :deep(.el-tabs__item) {
  font-size: 13px;
  padding: 0 10px;
}

.dot-blink {
  color: var(--color-primary);
  margin-left: 4px;
  font-weight: bold;
  animation: blink 0.9s steps(2, start) infinite;
}
@keyframes blink {
  to { visibility: hidden; }
}

/* ===== section 通用 ===== */
.ai-section {
  background: var(--color-bg-subtle);
  border-radius: var(--radius-md, 10px);
  border: 1px solid var(--color-border);
  padding: 12px 14px;
  margin-bottom: 12px;
}

/* ===== 健康卡片 ===== */
.health-row {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  padding: 4px 0;
  gap: 10px;
}
.health-label { color: var(--color-text-muted); font-size: 12px; flex-shrink: 0; }
.health-value { color: var(--color-text); font-size: 13px; text-align: right; word-break: break-all; }
.health-value .sub { color: var(--color-text-muted); margin-left: 4px; }
.health-value.mono { font-family: var(--font-mono, Menlo, monospace); font-size: 12px; }
.ml-8 { margin-left: 8px; }

.health-actions { margin-top: 8px; }
.health-actions .el-button { width: 100%; }

.health-error {
  margin-top: 8px;
  background: rgba(245, 108, 108, 0.1);
  border: 1px solid rgba(245, 108, 108, 0.3);
  color: #f56c6c;
  font-size: 12px;
  padding: 6px 10px;
  border-radius: 6px;
}
.health-success {
  margin-top: 8px;
  background: rgba(103, 194, 58, 0.1);
  border: 1px solid rgba(103, 194, 58, 0.3);
  color: #67c23a;
  font-size: 12px;
  padding: 6px 10px;
  border-radius: 6px;
}

/* ===== 任务分组 ===== */
.group-title {
  margin: 0 0 8px;
  color: var(--color-text-secondary);
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.5px;
}
.task-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
}
.task-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px;
  border-radius: 10px;
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  color: var(--color-text);
  cursor: pointer;
  transition: all 0.15s ease;
  font-size: 13px;
  text-align: left;
}
.task-btn:hover:not(:disabled) {
  border-color: var(--color-primary);
  background: var(--color-primary-bg);
  transform: translateY(-1px);
  box-shadow: 0 2px 6px rgba(64, 158, 255, 0.08);
}
.task-btn.active {
  border-color: var(--color-primary);
  background: var(--color-primary-bg);
  color: var(--color-primary-dark);
}
.task-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.task-icon { font-size: 16px; }
.task-label { font-weight: 500; position: relative; }
.running-dot {
  display: inline-block;
  margin-left: 4px;
  color: var(--color-primary);
  font-size: 10px;
  animation: blink 0.8s steps(2, start) infinite;
}
.task-btn.is-running {
  border-color: var(--color-primary);
  background: var(--color-primary-bg);
  color: var(--color-primary-dark);
}

.tip-card { font-size: 12px; color: var(--color-text-muted); }
.tip-title { color: var(--color-text-secondary); font-weight: 600; margin-bottom: 6px; }
.tip-card ul { margin: 0; padding-left: 16px; }
.tip-card li { margin: 4px 0; line-height: 1.6; }

.selected-text-hint {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px dashed var(--color-border);
}
.selected-text-label {
  font-size: 12px;
  font-weight: 500;
  color: var(--color-primary);
  margin-bottom: 4px;
}
.selected-text-preview {
  font-size: 12px;
  color: var(--color-text-secondary);
  background: var(--color-bg-card);
  padding: 6px 8px;
  border-radius: 6px;
  border: 1px solid var(--color-border);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* ===== 结果 Tab ===== */
.empty {
  text-align: center;
  padding: 36px 14px;
  color: var(--color-text-muted);
}
.empty-hero { font-size: 48px; margin-bottom: 10px; opacity: 0.5; }
.empty .sub { font-size: 12px; margin-top: 4px; }

.gen-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}
.gen-header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}
.copy-btn {
  width: 28px;
  height: 28px;
  border-radius: 6px;
  border: 1px solid var(--color-border);
  background: var(--color-bg-card);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  transition: all 0.15s;
}
.copy-btn:hover {
  border-color: var(--color-primary);
  background: var(--color-primary-bg);
}
.gen-title {
  font-weight: 600;
  color: var(--color-text);
  display: flex; align-items: center; gap: 6px;
  font-size: 14px;
}
.gen-icon { font-size: 16px; }

.gen-text-box {
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: 8px;
  min-height: 180px;
  max-height: 320px;
  padding: 12px;
  line-height: 1.7;
  font-size: 13px;
  color: var(--color-text-secondary);
  overflow-y: auto;
}
.gen-markdown-content {
  white-space: pre-wrap;
}
.gen-markdown-content :deep(h1),
.gen-markdown-content :deep(h2),
.gen-markdown-content :deep(h3),
.gen-markdown-content :deep(h4),
.gen-markdown-content :deep(h5),
.gen-markdown-content :deep(h6) {
  font-weight: 600;
  margin: 12px 0 6px;
  color: var(--color-text);
}
.gen-markdown-content :deep(h1) { font-size: 16px; }
.gen-markdown-content :deep(h2) { font-size: 15px; }
.gen-markdown-content :deep(h3) { font-size: 14px; }
.gen-markdown-content :deep(p) { margin: 6px 0; }
.gen-markdown-content :deep(strong) { font-weight: 600; color: var(--color-text); }
.gen-markdown-content :deep(em) { font-style: italic; }
.gen-markdown-content :deep(code) {
  background: rgba(64, 158, 255, 0.1);
  padding: 2px 4px;
  border-radius: 4px;
  font-family: var(--font-mono, Menlo, monospace);
  font-size: 12px;
}
.gen-markdown-content :deep(pre) {
  background: var(--color-bg-subtle);
  padding: 10px;
  border-radius: 6px;
  overflow-x: auto;
  margin: 8px 0;
}
.gen-markdown-content :deep(pre code) {
  background: transparent;
  padding: 0;
}
.gen-markdown-content :deep(ul),
.gen-markdown-content :deep(ol) {
  padding-left: 20px;
  margin: 6px 0;
}
.gen-markdown-content :deep(li) { margin: 4px 0; }
.gen-markdown-content :deep(blockquote) {
  border-left: 3px solid var(--color-primary);
  padding-left: 10px;
  margin: 8px 0;
  color: var(--color-text-muted);
}
.gen-markdown-content :deep(a) {
  color: var(--color-primary);
  text-decoration: underline;
}
.gen-markdown-content :deep(hr) {
  border: none;
  border-top: 1px dashed var(--color-border);
  margin: 12px 0;
}
.gen-text-box.is-streaming {
  border-color: var(--color-primary);
  box-shadow: inset 0 0 0 1px rgba(64, 158, 255, 0.15);
}
.gen-placeholder { color: var(--color-text-muted); font-style: italic; }
.stream-caret {
  display: inline-block;
  margin-left: 2px;
  color: var(--color-primary);
  animation: blink 1s steps(2, start) infinite;
}
.error-box {
  background: rgba(245, 108, 108, 0.08);
  border: 1px solid rgba(245, 108, 108, 0.3);
  color: #f56c6c;
  padding: 8px 10px;
  border-radius: 6px;
}
.error-box strong { color: #f56c6c; }
.error-box p { margin: 4px 0 0; }

.gen-meta {
  margin-top: 8px;
  font-size: 12px;
  color: var(--color-text-muted);
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}
.gen-actions {
  margin-top: 10px;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

/* ===== 历史列表 ===== */
.history-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  color: var(--color-text-secondary);
  font-weight: 600;
  margin-bottom: 8px;
}
.history-list {
  list-style: none;
  margin: 0;
  padding: 0;
  max-height: 280px;
  overflow-y: auto;
}
.history-item {
  padding: 8px 10px;
  border-radius: 8px;
  border: 1px solid transparent;
  display: flex;
  flex-direction: column;
  gap: 6px;
  cursor: pointer;
  margin-bottom: 4px;
}
.history-item:hover { background: var(--color-bg-card); }
.history-item.highlight {
  background: var(--color-primary-bg);
  border-color: var(--color-primary);
}
.history-left {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--color-text-secondary);
}
.h-icon { font-size: 14px; }
.h-label { font-weight: 500; color: var(--color-text); }
.h-snippet {
  font-size: 12px;
  color: var(--color-text-muted);
  padding-left: 22px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* ===== 质量 ===== */
.quality-title {
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 10px;
  text-align: center;
}
.quality-score {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  margin: 12px 0 16px;
}
.quality-hint { font-size: 13px; color: var(--color-text-secondary); text-align: center; }
.quality-empty {
  text-align: center;
  padding: 20px 10px;
  color: var(--color-text-muted);
  margin-bottom: 16px;
}
.quality-empty-hero { font-size: 32px; margin-bottom: 6px; opacity: 0.5; }
.quality-empty .sub { font-size: 12px; }

.quality-rule-list {
  margin-top: 12px;
  border-top: 1px dashed var(--color-border);
  padding-top: 10px;
}
.quality-rule-list h5 {
  margin: 0 0 6px;
  color: var(--color-text-secondary);
  font-size: 12px;
  font-weight: 600;
}
.quality-rule-list ul {
  margin: 0;
  padding-left: 16px;
  font-size: 12px;
  color: var(--color-text-muted);
  line-height: 2;
}

/* ===== 设置 ===== */
.settings-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 0;
  gap: 10px;
}
.settings-label { color: var(--color-text-muted); font-size: 13px; flex-shrink: 0; }
.settings-value { color: var(--color-text); font-size: 13px; text-align: right; flex: 1; min-width: 0; }
.settings-value.mono { font-family: var(--font-mono, Menlo, monospace); font-size: 12px; }

.settings-actions {
  margin-top: 10px;
  display: flex;
  gap: 8px;
}
.settings-actions .el-button { flex: 1; }

/* ===== 拖拽条 ===== */
.ai-sidebar-resizer {
  position: absolute;
  top: 0; bottom: 0; left: -5px;
  width: 10px;
  cursor: col-resize;
  z-index: 20;
  background: transparent;
  transition: background 0.2s;
}
.ai-sidebar-resizer:hover,
.ai-sidebar-resizer.resizing {
  background: linear-gradient(90deg, transparent, rgba(64, 158, 255, 0.3), transparent);
}
</style>
