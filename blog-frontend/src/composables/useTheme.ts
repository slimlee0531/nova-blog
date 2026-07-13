import { ref, watchEffect } from 'vue'

const THEME_KEY = 'nova-theme'

type Theme = 'light' | 'dark'

const theme = ref<Theme>((localStorage.getItem(THEME_KEY) as Theme) || 'light')

export function useTheme() {
  const toggleTheme = () => {
    theme.value = theme.value === 'light' ? 'dark' : 'light'
    localStorage.setItem(THEME_KEY, theme.value)
  }

  const setTheme = (t: Theme) => {
    theme.value = t
    localStorage.setItem(THEME_KEY, t)
  }

  // 同步到 DOM
  watchEffect(() => {
    document.documentElement.setAttribute('data-theme', theme.value)
  })

  return {
    theme,
    toggleTheme,
    setTheme
  }
}
