<script setup lang="ts">
import { useTheme } from '@/composables/useTheme'

const { theme, toggleTheme } = useTheme()
</script>

<template>
  <button class="theme-toggle" @click="toggleTheme" :title="theme === 'light' ? '切换暗色模式' : '切换亮色模式'">
    <span class="icon" v-if="theme === 'light'">🌙</span>
    <span class="icon" v-else>☀️</span>
  </button>
</template>

<style scoped>
.theme-toggle {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: var(--radius-full);
  background: var(--color-bg-subtle);
  transition: all var(--transition-fast);
}

.theme-toggle:hover {
  background: var(--color-border);
  transform: scale(1.05);
}

.icon {
  font-size: 18px;
  line-height: 1;
}
</style>
