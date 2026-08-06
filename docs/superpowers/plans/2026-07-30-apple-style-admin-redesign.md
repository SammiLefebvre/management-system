# 管理后台 Apple 风 redesign 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将现有 Vue 3 + Element Plus 管理后台改造为 Apple 风格，新增 Dashboard、深色模式、流畅动效，并新增后端统计接口。

**Architecture:** 前端在 `admin-frontend` 引入 Tailwind CSS，通过 CSS 变量统一覆盖 Element Plus 主题；新增通用组件 `AppCard`、`CountUp`、`MiniTrend`、`StatusTag`、`AppSkeleton`、`ThemeToggle`；新增 Pinia `theme` store 管理深色模式；后端新增 `StatisticsController` + `StatisticsService` 提供 Dashboard 真实数据。

**Tech Stack:** Vue 3 + Vite + TypeScript + Element Plus + Tailwind CSS + Pinia + Spring Boot + MyBatis-Plus

## Global Constraints

- 不改业务逻辑和现有数据库表结构。
- 所有 Dashboard 数据必须来自后端真实统计接口 `/api/statistics/dashboard`。
- 深色模式通过 `<html class="dark">` + Tailwind `darkMode: 'class'` + Element Plus 深色变量实现。
- 主强调色 `#0071e3`、成功 `#34c759`、警告 `#ff9500`、危险 `#ff3b30`。
- 大卡片圆角 `20px`、按钮圆角 `9999px`、输入框圆角 `10px`。
- 页面切换动画：`opacity 0→1`，`translateY 16px→0`，`300ms`。

---

## File Structure

### 新增文件

- `admin-frontend/tailwind.config.js`
- `admin-frontend/postcss.config.js`
- `admin-frontend/src/store/theme.ts`
- `admin-frontend/src/composables/useTheme.ts`
- `admin-frontend/src/assets/animations.css`
- `admin-frontend/src/components/AppCard.vue`
- `admin-frontend/src/components/AppSkeleton.vue`
- `admin-frontend/src/components/CountUp.vue`
- `admin-frontend/src/components/MiniTrend.vue`
- `admin-frontend/src/components/StatusTag.vue`
- `admin-frontend/src/components/ThemeToggle.vue`
- `admin-frontend/src/api/statistics.ts`
- `admin-frontend/src/views/dashboard/index.vue`
- `admin-frontend/src/components/icons/SystemLogo.vue`
- `backend/src/main/java/edu/cdut/aiback/controller/StatisticsController.java`
- `backend/src/main/java/edu/cdut/aiback/service/StatisticsService.java`
- `backend/src/main/java/edu/cdut/aiback/service/impl/StatisticsServiceImpl.java`
- `backend/src/main/java/edu/cdut/aiback/vo/DashboardStatisticsVO.java`

### 修改文件

- `admin-frontend/package.json`
- `admin-frontend/src/assets/base.css`
- `admin-frontend/src/assets/main.css`
- `admin-frontend/src/App.vue`
- `admin-frontend/src/main.ts`
- `admin-frontend/src/layout/index.vue`
- `admin-frontend/src/router/index.ts`
- `admin-frontend/src/views/login/index.vue`
- `admin-frontend/src/views/workorder/list.vue`
- `admin-frontend/src/views/workorder/create.vue`
- `admin-frontend/src/views/workorder/detail.vue`
- `admin-frontend/src/views/device/index.vue`
- `admin-frontend/src/views/personnel/index.vue`
- `admin-frontend/src/views/codetable/index.vue`
- `admin-frontend/src/views/sla/index.vue`
- `admin-frontend/src/views/team/index.vue`

---

## Task 1: 安装 Tailwind CSS 并配置

**Files:**
- Modify: `admin-frontend/package.json`
- Create: `admin-frontend/tailwind.config.js`
- Create: `admin-frontend/postcss.config.js`
- Modify: `admin-frontend/src/assets/main.css`

**Interfaces:**
- Consumes: 现有 Vite + Element Plus 构建流程。
- Produces: Tailwind 工具类可在 `.vue` 文件中使用；`@tailwind` 指令生效。

- [ ] **Step 1: 安装依赖**

```bash
cd admin-frontend
npm install -D tailwindcss postcss autoprefixer
```

- [ ] **Step 2: 创建 `tailwind.config.js`**

```javascript
/** @type {import('tailwindcss').Config} */
export default {
  darkMode: 'class',
  content: [
    './index.html',
    './src/**/*.{vue,js,ts,jsx,tsx}',
  ],
  theme: {
    extend: {
      fontFamily: {
        sans: ['-apple-system', 'BlinkMacSystemFont', '"SF Pro Text"', '"Segoe UI"', 'Roboto', 'Helvetica', 'Arial', 'sans-serif'],
      },
      borderRadius: {
        '2xl': '20px',
        'xl': '12px',
      },
      boxShadow: {
        'apple-sm': '0 2px 8px rgba(0,0,0,0.04)',
        'apple-md': '0 8px 24px rgba(0,0,0,0.06)',
        'apple-lg': '0 16px 40px rgba(0,0,0,0.08)',
      },
      transitionTimingFunction: {
        'apple': 'cubic-bezier(0.25, 0.1, 0.25, 1.0)',
      },
    },
  },
  plugins: [],
}
```

- [ ] **Step 3: 创建 `postcss.config.js`**

```javascript
export default {
  plugins: {
    tailwindcss: {},
    autoprefixer: {},
  },
}
```

- [ ] **Step 4: 修改 `src/assets/main.css`，加入 Tailwind 指令并移除旧样式**

```css
@tailwind base;
@tailwind components;
@tailwind utilities;

#app {
  width: 100%;
  min-height: 100vh;
}
```

- [ ] **Step 5: 启动 dev server 验证 Tailwind 生效**

```bash
cd admin-frontend
npm run dev
```

预期：页面不再出现 `#app max-width: 1280px` 等旧 Vue 默认样式，Tailwind 工具类可用。

---

## Task 2: 全局 CSS 变量、动画、主题切换

**Files:**
- Modify: `admin-frontend/src/assets/base.css`
- Create: `admin-frontend/src/assets/animations.css`
- Create: `admin-frontend/src/store/theme.ts`
- Create: `admin-frontend/src/composables/useTheme.ts`
- Modify: `admin-frontend/src/main.ts`
- Modify: `admin-frontend/src/App.vue`

**Interfaces:**
- Consumes: 无。
- Produces: CSS 变量 `:root` / `.dark` 定义完成；`useTheme()` 返回 `{ isDark, toggleTheme }`；`App.vue` 根据主题切换 `<html class="dark">`；`animations.css` 提供 `page-enter`、shimmer、渐变动画。

- [ ] **Step 1: 修改 `src/assets/base.css` 为 Design Tokens**

```css
:root {
  --bg-primary: #ffffff;
  --bg-secondary: #f5f5f7;
  --bg-tertiary: #ffffff;
  --text-primary: #1d1d1f;
  --text-secondary: #6e6e73;
  --text-tertiary: #86868b;
  --accent: #0071e3;
  --accent-hover: #0077ed;
  --accent-light: #e8f4fd;
  --success: #34c759;
  --warning: #ff9500;
  --danger: #ff3b30;
  --border: #d2d2d7;
  --shadow-sm: 0 2px 8px rgba(0,0,0,0.04);
  --shadow-md: 0 8px 24px rgba(0,0,0,0.06);
  --shadow-lg: 0 16px 40px rgba(0,0,0,0.08);
}

.dark {
  --bg-primary: #000000;
  --bg-secondary: #1d1d1f;
  --bg-tertiary: #2c2c2e;
  --text-primary: #f5f5f7;
  --text-secondary: #a1a1a6;
  --text-tertiary: #8e8e93;
  --accent: #0a84ff;
  --accent-hover: #2997ff;
  --accent-light: #1c2c3c;
  --success: #30d158;
  --warning: #ff9f0a;
  --danger: #ff453a;
  --border: #38383a;
  --shadow-sm: 0 2px 8px rgba(0,0,0,0.2);
  --shadow-md: 0 8px 24px rgba(0,0,0,0.32);
  --shadow-lg: 0 16px 40px rgba(0,0,0,0.48);
}

* {
  transition: background-color 0.3s ease, color 0.3s ease, border-color 0.3s ease;
}

body {
  margin: 0;
  padding: 0;
  background-color: var(--bg-primary);
  color: var(--text-primary);
  font-family: -apple-system, BlinkMacSystemFont, "SF Pro Text", "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
}

/* Element Plus 变量覆盖 */
.el-button--primary {
  --el-button-bg-color: var(--accent);
  --el-button-border-color: var(--accent);
  --el-button-hover-bg-color: var(--accent-hover);
  --el-button-hover-border-color: var(--accent-hover);
}

.el-input__wrapper.is-focus {
  box-shadow: 0 0 0 4px var(--accent-light) !important;
  border-color: var(--accent) !important;
}
```

- [ ] **Step 2: 创建 `src/assets/animations.css`**

```css
@keyframes shimmer {
  0% { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}

@keyframes gradient-flow {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

@keyframes fade-in-up {
  from { opacity: 0; transform: translateY(16px); }
  to { opacity: 1; transform: translateY(0); }
}

.animate-shimmer {
  background: linear-gradient(90deg, var(--bg-secondary) 25%, #e8e8ed 50%, var(--bg-secondary) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.dark .animate-shimmer {
  background: linear-gradient(90deg, var(--bg-secondary) 25%, #2c2c2e 50%, var(--bg-secondary) 75%);
  background-size: 200% 100%;
}

.animate-gradient-flow {
  background-size: 200% 200%;
  animation: gradient-flow 20s ease infinite;
}

.page-enter-active {
  transition: opacity 0.3s cubic-bezier(0.25, 0.1, 0.25, 1.0), transform 0.3s cubic-bezier(0.25, 0.1, 0.25, 1.0);
}

.page-enter-from {
  opacity: 0;
  transform: translateY(16px);
}

.page-leave-active {
  transition: opacity 0.2s ease;
}

.page-leave-to {
  opacity: 0;
}
```

- [ ] **Step 3: 创建 `src/store/theme.ts`**

```typescript
import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

export const useThemeStore = defineStore('theme', () => {
  const isDark = ref(localStorage.getItem('theme') === 'dark')

  function applyTheme() {
    if (isDark.value) {
      document.documentElement.classList.add('dark')
      localStorage.setItem('theme', 'dark')
    } else {
      document.documentElement.classList.remove('dark')
      localStorage.setItem('theme', 'light')
    }
  }

  function toggleTheme() {
    isDark.value = !isDark.value
    applyTheme()
  }

  // 初始化
  applyTheme()

  watch(isDark, applyTheme)

  return { isDark, toggleTheme, applyTheme }
})
```

- [ ] **Step 4: 创建 `src/composables/useTheme.ts`**

```typescript
import { useThemeStore } from '@/store/theme'
import { storeToRefs } from 'pinia'

export function useTheme() {
  const store = useThemeStore()
  const { isDark } = storeToRefs(store)
  return { isDark, toggleTheme: store.toggleTheme }
}
```

- [ ] **Step 5: 修改 `src/main.ts`，导入 dark CSS 和 animations**

```typescript
import { createApp } from 'vue'
import App from './App.vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import router from './router'
import { createPinia } from 'pinia'

import './assets/base.css'
import './assets/main.css'
import './assets/animations.css'

const app = createApp(App)

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(ElementPlus)
app.use(router)
app.use(createPinia())
app.mount('#app')
```

- [ ] **Step 6: 修改 `src/App.vue`，添加路由过渡**

```vue
<template>
  <router-view v-slot="{ Component }">
    <transition name="page" mode="out-in">
      <component :is="Component" />
    </transition>
  </router-view>
</template>
```

- [ ] **Step 7: 验证主题切换**

在浏览器控制台执行 `document.documentElement.classList.toggle('dark')`，背景应变黑、文字变白。后续会有 `ThemeToggle` 组件正式切换。

---

## Task 3: 通用组件

**Files:**
- Create: `admin-frontend/src/components/AppCard.vue`
- Create: `admin-frontend/src/components/AppSkeleton.vue`
- Create: `admin-frontend/src/components/CountUp.vue`
- Create: `admin-frontend/src/components/MiniTrend.vue`
- Create: `admin-frontend/src/components/StatusTag.vue`
- Create: `admin-frontend/src/components/ThemeToggle.vue`
- Create: `admin-frontend/src/components/icons/SystemLogo.vue`

**Interfaces:**
- Consumes: CSS 变量、Tailwind 工具类。
- Produces: 后续页面直接使用的 `<AppCard>`、`<CountUp>`、`<MiniTrend>`、`<StatusTag>`、`<ThemeToggle>`、`<SystemLogo>`。

- [ ] **Step 1: 创建 `AppCard.vue`**

```vue
<template>
  <div class="app-card" :class="{ 'app-card-hover': hoverable }">
    <div v-if="$slots.header" class="app-card-header">
      <slot name="header" />
    </div>
    <div class="app-card-body">
      <slot />
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{ hoverable?: boolean }>()
</script>

<script lang="ts">
export default { name: 'AppCard' }
</script>

<style scoped>
.app-card {
  background-color: var(--bg-secondary);
  border-radius: 20px;
  box-shadow: var(--shadow-md);
  transition: transform 0.3s cubic-bezier(0.25, 0.1, 0.25, 1.0), box-shadow 0.3s cubic-bezier(0.25, 0.1, 0.25, 1.0);
  overflow: hidden;
}
.app-card-hover:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}
.app-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  border-bottom: 1px solid var(--border);
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}
.app-card-body {
  padding: 24px;
}
</style>
```

- [ ] **Step 2: 创建 `AppSkeleton.vue`**

```vue
<template>
  <div class="skeleton-container">
    <div
      v-for="i in rows"
      :key="i"
      class="skeleton-row"
      :style="{ height: rowHeight + 'px', marginBottom: gap + 'px' }"
    />
  </div>
</template>

<script setup lang="ts">
withDefaults(defineProps<{ rows?: number; rowHeight?: number; gap?: number }>(), {
  rows: 6,
  rowHeight: 20,
  gap: 12,
})
</script>

<style scoped>
.skeleton-row {
  border-radius: 8px;
  width: 100%;
}
</style>
```

使用时需在外层加 `animate-shimmer` class：`class="animate-shimmer"` 可作用于 `AppSkeleton` 本身或内部行。为简化，将 `AppSkeleton` 改为行自身带 `animate-shimmer`。

```vue
<template>
  <div class="skeleton-container">
    <div
      v-for="i in rows"
      :key="i"
      class="skeleton-row animate-shimmer"
      :style="{ height: rowHeight + 'px', marginBottom: gap + 'px' }"
    />
  </div>
</template>
```

- [ ] **Step 3: 创建 `CountUp.vue`**

```vue
<template>
  <span>{{ displayValue }}</span>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'

const props = defineProps<{
  value: number
  duration?: number
}>()

const displayValue = ref(0)

function easeOutExpo(t: number) {
  return t === 1 ? 1 : 1 - Math.pow(2, -10 * t)
}

function animate(from: number, to: number, duration: number) {
  const startTime = performance.now()
  function step(now: number) {
    const elapsed = now - startTime
    const progress = Math.min(elapsed / duration, 1)
    displayValue.value = Math.floor(from + (to - from) * easeOutExpo(progress))
    if (progress < 1) {
      requestAnimationFrame(step)
    } else {
      displayValue.value = to
    }
  }
  requestAnimationFrame(step)
}

onMounted(() => animate(0, props.value, props.duration || 1200))
watch(() => props.value, (newVal, oldVal) => animate(oldVal ?? 0, newVal, props.duration || 1200))
</script>
```

- [ ] **Step 4: 创建 `MiniTrend.vue`**

```vue
<template>
  <div class="mini-trend">
    <div
      v-for="(item, idx) in data"
      :key="idx"
      class="mini-trend-bar"
      :style="barStyle(item)"
      :title="`${item.date}: 新建${item.created}, 完成${item.completed}`"
    />
  </div>
</template>

<script setup lang="ts">
interface TrendItem {
  date: string
  created: number
  completed: number
}
const props = defineProps<{ data: TrendItem[] }>()

function barStyle(item: TrendItem) {
  const max = Math.max(...props.data.map(d => Math.max(d.created, d.completed || 0)), 1)
  const height = `${(item.created / max) * 100}%`
  return { height }
}
</script>

<style scoped>
.mini-trend {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 6px;
  height: 64px;
}
.mini-trend-bar {
  flex: 1;
  background: linear-gradient(to top, var(--accent), rgba(0,113,227,0.3));
  border-radius: 4px 4px 0 0;
  min-height: 4px;
  transition: height 0.8s cubic-bezier(0.25, 0.1, 0.25, 1.0);
}
</style>
```

- [ ] **Step 5: 创建 `StatusTag.vue`**

```vue
<template>
  <span class="status-tag" :class="typeClass">
    <span class="status-dot" />
    <span>{{ label }}</span>
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{ type: 'success' | 'warning' | 'danger' | 'info' | 'purple'; label: string }>()

const typeClass = computed(() => `status-tag--${props.type}`)
</script>

<style scoped>
.status-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  border-radius: 9999px;
  font-size: 12px;
  font-weight: 500;
}
.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}
.status-tag--success { background: rgba(52,199,89,0.12); color: var(--success); }
.status-tag--success .status-dot { background: var(--success); }
.status-tag--warning { background: rgba(255,149,0,0.12); color: var(--warning); }
.status-tag--warning .status-dot { background: var(--warning); }
.status-tag--danger { background: rgba(255,59,48,0.12); color: var(--danger); }
.status-tag--danger .status-dot { background: var(--danger); }
.status-tag--info { background: rgba(0,113,227,0.12); color: var(--accent); }
.status-tag--info .status-dot { background: var(--accent); }
.status-tag--purple { background: rgba(175,82,222,0.12); color: #af52de; }
.status-tag--purple .status-dot { background: #af52de; }
</style>
```

- [ ] **Step 6: 创建 `ThemeToggle.vue`**

```vue
<template>
  <button class="theme-toggle" @click="toggleTheme" :title="isDark ? '切换浅色' : '切换深色'">
    <el-icon v-if="isDark" size="18"><Sunny /></el-icon>
    <el-icon v-else size="18"><Moon /></el-icon>
  </button>
</template>

<script setup lang="ts">
import { useTheme } from '@/composables/useTheme'
import { Sunny, Moon } from '@element-plus/icons-vue'

const { isDark, toggleTheme } = useTheme()
</script>

<style scoped>
.theme-toggle {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: 1px solid var(--border);
  background: var(--bg-tertiary);
  color: var(--text-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
}
.theme-toggle:hover {
  background: var(--accent-light);
  color: var(--accent);
}
</style>
```

- [ ] **Step 7: 创建 `SystemLogo.vue`**

```vue
<template>
  <div class="system-logo">
    <div class="logo-icon">
      <el-icon size="22"><Memo /></el-icon>
    </div>
    <span class="logo-text">工单管理</span>
  </div>
</template>

<script setup lang="ts">
import { Memo } from '@element-plus/icons-vue'
</script>

<style scoped>
.system-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 16px;
  height: 80px;
}
.logo-icon {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: var(--accent);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
}
.logo-text {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: -0.01em;
}
</style>
```

- [ ] **Step 8: 在临时页面验证通用组件**

在任意页面临时引入 `<AppCard>`、`<CountUp :value="123" />`、`<StatusTag type="success" label="正常" />` 等，确认样式正常。

---

## Task 4: 布局改造

**Files:**
- Modify: `admin-frontend/src/layout/index.vue`

**Interfaces:**
- Consumes: `ThemeToggle`、`SystemLogo`、CSS 变量、Element Plus `el-menu`。
- Produces: 改造后的侧边栏 + Header，根路由切换动画已由 `App.vue` 提供。

- [ ] **Step 1: 重写 `layout/index.vue`**

```vue
<template>
  <div class="layout-wrapper">
    <aside class="sidebar">
      <SystemLogo />
      <nav class="menu-wrapper">
        <el-menu
          :default-active="activeMenu"
          router
          class="app-menu"
        >
          <el-menu-item index="/dashboard">
            <el-icon><DataLine /></el-icon>
            <span>数据概览</span>
          </el-menu-item>
          <el-menu-item index="/work-order">
            <el-icon><List /></el-icon>
            <span>工单管理</span>
          </el-menu-item>
          <el-menu-item index="/device">
            <el-icon><Monitor /></el-icon>
            <span>设备台账</span>
          </el-menu-item>
          <el-menu-item index="/personnel">
            <el-icon><User /></el-icon>
            <span>人员管理</span>
          </el-menu-item>
          <el-menu-item index="/code-table">
            <el-icon><Setting /></el-icon>
            <span>码表管理</span>
          </el-menu-item>
          <el-menu-item index="/sla">
            <el-icon><Timer /></el-icon>
            <span>SLA 配置</span>
          </el-menu-item>
          <el-menu-item index="/team">
            <el-icon><Avatar /></el-icon>
            <span>班组查看</span>
          </el-menu-item>
        </el-menu>
      </nav>
    </aside>

    <main class="main-area">
      <header class="app-header">
        <div class="header-left">
          <h1 class="page-title">{{ currentTitle }}</h1>
          <span class="page-date">{{ today }}</span>
        </div>
        <div class="header-right">
          <el-tag class="project-tag" effect="plain" round>
            {{ userStore.userInfo.projectGroup }}
          </el-tag>
          <span class="user-name">{{ userStore.userInfo.account }}</span>
          <ThemeToggle />
          <el-button type="danger" size="small" round @click="handleLogout">退出</el-button>
        </div>
      </header>
      <div class="content-area">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import SystemLogo from '@/components/icons/SystemLogo.vue'
import ThemeToggle from '@/components/ThemeToggle.vue'
import { DataLine, List, Monitor, User, Setting, Timer, Avatar } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)
const currentTitle = computed(() => (route.meta.title as string) || '工单管理系统')

const today = new Date().toLocaleDateString('zh-CN', {
  year: 'numeric',
  month: 'long',
  day: 'numeric',
  weekday: 'long',
})

function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout-wrapper {
  display: flex;
  min-height: 100vh;
  background-color: var(--bg-primary);
}
.sidebar {
  width: 240px;
  background-color: var(--bg-secondary);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  border-right: 1px solid var(--border);
}
.menu-wrapper {
  flex: 1;
  padding: 0 12px;
}
.app-menu {
  background: transparent;
  border-right: none;
}
.app-menu :deep(.el-menu-item) {
  height: 48px;
  border-radius: 12px;
  margin-bottom: 4px;
  color: var(--text-secondary);
  transition: all 0.2s ease;
}
.app-menu :deep(.el-menu-item:hover) {
  background-color: rgba(0,0,0,0.04);
  color: var(--text-primary);
}
.app-menu :deep(.el-menu-item.is-active) {
  background-color: var(--accent-light);
  color: var(--accent);
  font-weight: 600;
}
.dark .app-menu :deep(.el-menu-item:hover) {
  background-color: rgba(255,255,255,0.06);
}
.main-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}
.app-header {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32px;
  background-color: rgba(255,255,255,0.72);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid var(--border);
  position: sticky;
  top: 0;
  z-index: 100;
}
.dark .app-header {
  background-color: rgba(0,0,0,0.72);
}
.header-left {
  display: flex;
  align-items: baseline;
  gap: 16px;
}
.page-title {
  font-size: 22px;
  font-weight: 700;
  letter-spacing: -0.02em;
  margin: 0;
  color: var(--text-primary);
}
.page-date {
  font-size: 13px;
  color: var(--text-secondary);
}
.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}
.project-tag {
  border-radius: 9999px;
  color: var(--text-secondary);
  border-color: var(--border);
}
.user-name {
  font-size: 14px;
  color: var(--text-primary);
  font-weight: 500;
}
.content-area {
  flex: 1;
  padding: 32px;
  overflow-y: auto;
}
@media (min-width: 1440px) {
  .content-area {
    padding: 48px;
  }
}
</style>
```

- [ ] **Step 2: 验证布局**

运行 dev server，登录后应看到新侧边栏、Header、玻璃效果、主题切换按钮。

---

## Task 5: 登录页改造

**Files:**
- Modify: `admin-frontend/src/views/login/index.vue`

**Interfaces:**
- Consumes: `CountUp`（不用）、CSS 变量、Element Plus 表单组件。
- Produces: Apple 风登录页。

- [ ] **Step 1: 重写 `login/index.vue`**

```vue
<template>
  <div class="login-page">
    <div class="login-bg" />
    <div class="login-card-wrapper">
      <div class="login-card">
        <div class="login-icon">
          <el-icon size="32" color="#fff"><Memo /></el-icon>
        </div>
        <h1 class="login-title">工单管理系统</h1>
        <p class="login-subtitle">欢迎回来，请登录您的账号</p>

        <el-form :model="form" class="login-form">
          <el-form-item>
            <el-input
              v-model="form.email"
              placeholder="请输入邮箱账号"
              size="large"
              class="login-input"
            />
          </el-form-item>
          <el-form-item>
            <div class="code-row">
              <el-input
                v-model="form.code"
                placeholder="6位验证码"
                maxlength="6"
                size="large"
                class="login-input"
              />
              <el-button
                type="primary"
                size="large"
                round
                :loading="sending"
                @click="handleSendCode"
              >
                发送验证码
              </el-button>
            </div>
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              size="large"
              round
              class="login-submit"
              :loading="loading"
              @click="handleLogin"
            >
              登 录
            </el-button>
          </el-form-item>
        </el-form>

        <p class="login-hint">演示环境验证码可输入任意 6 位数字</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
import { Memo } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const form = reactive({ email: '', code: '' })
const sending = ref(false)
const loading = ref(false)

async function handleSendCode() {
  if (!form.email) {
    return ElMessage.warning('请输入邮箱')
  }
  sending.value = true
  try {
    await userStore.sendCode(form.email)
    ElMessage.success('验证码已发送')
  } finally {
    sending.value = false
  }
}

async function handleLogin() {
  if (!form.email || !form.code) {
    return ElMessage.warning('请填写完整信息')
  }
  loading.value = true
  try {
    await userStore.login(form.email, form.code)
    router.push('/')
  } finally {
    loading.value = false
  }
}
</script>

<script lang="ts">
export default { name: 'LoginPage' }
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  background-color: var(--bg-primary);
}
.login-bg {
  position: absolute;
  inset: -50%;
  background: radial-gradient(circle at 20% 30%, rgba(0,113,227,0.18), transparent 40%),
              radial-gradient(circle at 80% 70%, rgba(175,82,222,0.15), transparent 40%),
              radial-gradient(circle at 50% 50%, rgba(0,113,227,0.08), transparent 50%);
  animation: gradient-flow 20s ease infinite;
  background-size: 200% 200%;
  z-index: 0;
}
.login-card-wrapper {
  position: relative;
  z-index: 1;
  animation: fade-in-up 0.6s cubic-bezier(0.25, 0.1, 0.25, 1.0) both;
}
.login-card {
  width: 420px;
  padding: 48px 40px;
  border-radius: 28px;
  background: rgba(255,255,255,0.82);
  backdrop-filter: blur(24px);
  border: 1px solid rgba(255,255,255,0.3);
  box-shadow: var(--shadow-lg);
  text-align: center;
}
.dark .login-card {
  background: rgba(30,30,30,0.85);
  border-color: rgba(255,255,255,0.08);
}
.login-icon {
  width: 64px;
  height: 64px;
  border-radius: 18px;
  background: linear-gradient(135deg, var(--accent), #2997ff);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 24px;
  box-shadow: 0 8px 24px rgba(0,113,227,0.3);
}
.login-title {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 8px;
  letter-spacing: -0.02em;
  color: var(--text-primary);
}
.login-subtitle {
  font-size: 15px;
  color: var(--text-secondary);
  margin: 0 0 32px;
}
.login-form {
  text-align: left;
}
.code-row {
  display: flex;
  gap: 12px;
  width: 100%;
}
.code-row .el-input {
  flex: 1;
}
.login-submit {
  width: 100%;
  margin-top: 8px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, var(--accent), #2997ff);
  border: none;
  transition: transform 0.15s ease, box-shadow 0.2s ease;
}
.login-submit:hover {
  box-shadow: 0 6px 20px rgba(0,113,227,0.35);
}
.login-submit:active {
  transform: scale(0.98);
}
.login-hint {
  font-size: 13px;
  color: var(--text-tertiary);
  margin-top: 24px;
}
</style>
```

- [ ] **Step 2: 调整 Element Plus 输入框聚焦样式**

确保 `base.css` 中 `.el-input__wrapper.is-focus` 的样式已生效。

- [ ] **Step 3: 验证登录页**

退出登录或直接访问 `/login`，应看到毛玻璃卡片、渐变背景、胶囊按钮。

---

## Task 6: 后端 Dashboard 统计接口

**Files:**
- Create: `backend/src/main/java/edu/cdut/aiback/vo/DashboardStatisticsVO.java`
- Create: `backend/src/main/java/edu/cdut/aiback/service/StatisticsService.java`
- Create: `backend/src/main/java/edu/cdut/aiback/service/impl/StatisticsServiceImpl.java`
- Create: `backend/src/main/java/edu/cdut/aiback/controller/StatisticsController.java`

**Interfaces:**
- Consumes: 现有 `WorkOrderMapper`、`DeviceMapper`、`PersonnelMapper`、`TeamMapper`。
- Produces: `GET /api/statistics/dashboard` 返回 `DashboardStatisticsVO` JSON。

- [ ] **Step 1: 创建 VO 类 `DashboardStatisticsVO.java`**

```java
package edu.cdut.aiback.vo;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class DashboardStatisticsVO {
    private Long workOrderTotal;
    private Long workOrderToday;
    private Map<String, Long> statusCounts;
    private Long slaOverdueCount;
    private Long deviceTotal;
    private Long personnelTotal;
    private Long teamTotal;
    private List<DailyTrend> last7Days;

    @Data
    public static class DailyTrend {
        private String date;
        private Long created;
        private Long completed;
    }
}
```

- [ ] **Step 2: 创建 Service 接口 `StatisticsService.java`**

```java
package edu.cdut.aiback.service;

import edu.cdut.aiback.vo.DashboardStatisticsVO;

public interface StatisticsService {
    DashboardStatisticsVO getDashboardStatistics();
}
```

- [ ] **Step 3: 创建 Service 实现 `StatisticsServiceImpl.java`**

```java
package edu.cdut.aiback.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import edu.cdut.aiback.entity.WorkOrder;
import edu.cdut.aiback.mapper.*;
import edu.cdut.aiback.service.StatisticsService;
import edu.cdut.aiback.vo.DashboardStatisticsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final WorkOrderMapper workOrderMapper;
    private final DeviceMapper deviceMapper;
    private final PersonnelMapper personnelMapper;
    private final TeamMapper teamMapper;

    @Override
    public DashboardStatisticsVO getDashboardStatistics() {
        DashboardStatisticsVO vo = new DashboardStatisticsVO();

        // 工单总数
        vo.setWorkOrderTotal(workOrderMapper.selectCount(null));

        // 今日新增
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        QueryWrapper<WorkOrder> todayWrapper = new QueryWrapper<>();
        todayWrapper.ge("created_at", todayStart);
        vo.setWorkOrderToday(workOrderMapper.selectCount(todayWrapper));

        // 各状态数量
        Map<String, Long> statusCounts = new LinkedHashMap<>();
        String[] statuses = {"published", "claimed", "in_progress", "completing", "pending_confirm", "confirmed", "closed", "pending_force_close"};
        for (String status : statuses) {
            QueryWrapper<WorkOrder> sw = new QueryWrapper<>();
            sw.eq("status", status);
            statusCounts.put(status, workOrderMapper.selectCount(sw));
        }
        vo.setStatusCounts(statusCounts);

        // SLA 超期（简化：已发布超过 60 分钟 或 非已确认/已关闭超过紧急程度阈值）
        // 更精确的逻辑可复用现有 WorkOrderService 的 SLA 判断，这里先用简化版
        vo.setSlaOverdueCount(countSlaOverdue());

        // 资源统计
        vo.setDeviceTotal(deviceMapper.selectCount(null));
        vo.setPersonnelTotal(personnelMapper.selectCount(null));
        vo.setTeamTotal(teamMapper.selectCount(null));

        // 近 7 天趋势
        vo.setLast7Days(buildLast7DaysTrend());

        return vo;
    }

    private Long countSlaOverdue() {
        // 简化实现：status 为 published 且 created_at 早于 60 分钟前
        LocalDateTime oneHourAgo = LocalDateTime.now().minusMinutes(60);
        QueryWrapper<WorkOrder> wrapper = new QueryWrapper<>();
        wrapper.eq("status", "published").lt("created_at", oneHourAgo);
        return workOrderMapper.selectCount(wrapper);
    }

    private List<DashboardStatisticsVO.DailyTrend> buildLast7DaysTrend() {
        List<DashboardStatisticsVO.DailyTrend> list = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            String dateStr = date.format(fmt);

            QueryWrapper<WorkOrder> createdWrapper = new QueryWrapper<>();
            createdWrapper.apply("DATE(created_at) = {0}", dateStr);
            long created = workOrderMapper.selectCount(createdWrapper);

            QueryWrapper<WorkOrder> completedWrapper = new QueryWrapper<>();
            completedWrapper.apply("DATE(complete_time) = {0}", dateStr);
            long completed = workOrderMapper.selectCount(completedWrapper);

            DashboardStatisticsVO.DailyTrend trend = new DashboardStatisticsVO.DailyTrend();
            trend.setDate(dateStr);
            trend.setCreated(created);
            trend.setCompleted(completed);
            list.add(trend);
        }
        return list;
    }
}
```

> 如果 `WorkOrderMapper`、`DeviceMapper`、`PersonnelMapper`、`TeamMapper` 名称不同，需按实际 Mapper 名称修改。

- [ ] **Step 4: 创建 Controller `StatisticsController.java`**

```java
package edu.cdut.aiback.controller;

import edu.cdut.aiback.common.Result;
import edu.cdut.aiback.service.StatisticsService;
import edu.cdut.aiback.vo.DashboardStatisticsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/dashboard")
    public Result<DashboardStatisticsVO> dashboard() {
        return Result.ok(statisticsService.getDashboardStatistics());
    }
}
```

- [ ] **Step 5: 启动后端测试接口**

```bash
cd backend
mvnw.cmd spring-boot:run
```

使用浏览器或 curl 访问：

```bash
curl http://localhost:9090/api/statistics/dashboard
```

预期返回 JSON，字段与 `DashboardStatisticsVO` 一致。

---

## Task 7: Dashboard 页面

**Files:**
- Create: `admin-frontend/src/api/statistics.ts`
- Create: `admin-frontend/src/views/dashboard/index.vue`
- Modify: `admin-frontend/src/router/index.ts`

**Interfaces:**
- Consumes: `/api/statistics/dashboard`、通用组件。
- Produces: `/dashboard` 路由和 Dashboard 页面。

- [ ] **Step 1: 创建 `src/api/statistics.ts`**

```typescript
import request from './request'

export interface DashboardStatistics {
  workOrderTotal: number
  workOrderToday: number
  statusCounts: Record<string, number>
  slaOverdueCount: number
  deviceTotal: number
  personnelTotal: number
  teamTotal: number
  last7Days: Array<{ date: string; created: number; completed: number }>
}

export function getDashboardStatistics() {
  return request.get<{ data: DashboardStatistics }>('/statistics/dashboard')
}
```

- [ ] **Step 2: 修改 `src/router/index.ts`**

在 `routes` 数组开头增加 Dashboard，并把 `/` 的 redirect 改为 `/dashboard`：

```typescript
{
  path: '/dashboard',
  name: 'Dashboard',
  component: () => import('@/views/dashboard/index.vue'),
  meta: { title: '数据概览' }
},
{
  path: '/',
  component: () => import('@/layout/index.vue'),
  redirect: '/dashboard',
  children: [ ... ]
}
```

- [ ] **Step 3: 创建 `src/views/dashboard/index.vue`**

完整 Dashboard 页面代码较长，核心结构如下：

```vue
<template>
  <div class="dashboard-page">
    <!-- Hero -->
    <div class="dashboard-hero">
      <div>
        <h2 class="hero-title">你好，{{ userStore.userInfo.account }}</h2>
        <p class="hero-subtitle">{{ summaryText }}</p>
      </div>
      <el-button type="primary" size="large" round @click="$router.push('/work-order/create')">
        新建工单
      </el-button>
    </div>

    <!-- Bento Grid -->
    <div v-if="loading" class="dashboard-grid">
      <AppCard v-for="i in 5" :key="i" class="dashboard-card">
        <AppSkeleton :rows="4" />
      </AppCard>
    </div>

    <div v-else class="dashboard-grid">
      <!-- 工单总览 -->
      <AppCard class="dashboard-card card-large">
        <template #header>
          <span>工单总览</span>
        </template>
        <div class="stat-primary">
          <CountUp :value="stats.workOrderTotal" />
          <span class="stat-delta">+{{ stats.workOrderToday }} 今日新增</span>
        </div>
        <MiniTrend :data="stats.last7Days" />
      </AppCard>

      <!-- SLA 预警 -->
      <AppCard class="dashboard-card card-medium" :hoverable="true">
        <template #header>
          <span>SLA 预警</span>
          <span v-if="stats.slaOverdueCount > 0" class="warning-dot" />
        </template>
        <div class="stat-primary" :class="{ danger: stats.slaOverdueCount > 0 }">
          <CountUp :value="stats.slaOverdueCount" />
        </div>
        <p class="card-desc">
          {{ stats.slaOverdueCount > 0 ? '存在超期工单，请及时处理' : '全部正常，暂无超期工单' }}
        </p>
      </AppCard>

      <!-- 工单状态分布 -->
      <AppCard class="dashboard-card card-medium">
        <template #header>
          <span>工单状态</span>
        </template>
        <div class="status-bars">
          <div
            v-for="item in statusItems"
            :key="item.key"
            class="status-bar-item"
          >
            <div class="status-bar-header">
              <StatusTag :type="item.type" :label="item.label" />
              <span class="status-bar-value">{{ item.value }}</span>
            </div>
            <div class="status-bar-track">
              <div
                class="status-bar-fill"
                :style="{ width: item.percent + '%', background: item.color }"
              />
            </div>
          </div>
        </div>
      </AppCard>

      <!-- 资源统计 -->
      <AppCard class="dashboard-card card-small">
        <template #header>
          <span>资源统计</span>
        </template>
        <div class="resource-grid">
          <div v-for="r in resources" :key="r.label" class="resource-item">
            <el-icon size="22" :color="r.color"><component :is="r.icon" /></el-icon>
            <div class="resource-value"><CountUp :value="r.value" /></div>
            <div class="resource-label">{{ r.label }}</div>
          </div>
        </div>
      </AppCard>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/store/user'
import AppCard from '@/components/AppCard.vue'
import AppSkeleton from '@/components/AppSkeleton.vue'
import CountUp from '@/components/CountUp.vue'
import MiniTrend from '@/components/MiniTrend.vue'
import StatusTag from '@/components/StatusTag.vue'
import { Monitor, User, Avatar } from '@element-plus/icons-vue'
import { getDashboardStatistics, type DashboardStatistics } from '@/api/statistics'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const stats = ref<DashboardStatistics>({
  workOrderTotal: 0,
  workOrderToday: 0,
  statusCounts: {},
  slaOverdueCount: 0,
  deviceTotal: 0,
  personnelTotal: 0,
  teamTotal: 0,
  last7Days: [],
})
const loading = ref(true)

const summaryText = computed(() => {
  if (stats.value.slaOverdueCount > 0) {
    return `今日新增 ${stats.value.workOrderToday} 个工单，${stats.value.slaOverdueCount} 个工单已超期`
  }
  return `今日新增 ${stats.value.workOrderToday} 个工单，暂无超期工单`
})

const statusItems = computed(() => {
  const statusConfig = [
    { key: 'published', label: '待认领', type: 'info' as const, color: '#0071e3' },
    { key: 'claimed', label: '进行中', type: 'warning' as const, color: '#ff9500' },
    { key: 'in_progress', label: '作业中', type: 'warning' as const, color: '#ff9500' },
    { key: 'pending_confirm', label: '待确认', type: 'purple' as const, color: '#af52de' },
    { key: 'confirmed', label: '已确认', type: 'success' as const, color: '#34c759' },
  ]
  const max = Math.max(...statusConfig.map(s => stats.value.statusCounts[s.key] || 0), 1)
  return statusConfig.map(s => {
    const value = stats.value.statusCounts[s.key] || 0
    return { ...s, value, percent: (value / max) * 100 }
  })
})

const resources = computed(() => [
  { label: '设备', value: stats.value.deviceTotal, icon: 'Monitor', color: '#0071e3' },
  { label: '人员', value: stats.value.personnelTotal, icon: 'User', color: '#34c759' },
  { label: '班组', value: stats.value.teamTotal, icon: 'Avatar', color: '#af52de' },
])

onMounted(async () => {
  try {
    const res = await getDashboardStatistics()
    stats.value = res.data.data
  } catch (e) {
    ElMessage.error('加载统计数据失败')
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.dashboard-page {
  max-width: 1600px;
}
.dashboard-hero {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: 32px;
}
.hero-title {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 6px;
  color: var(--text-primary);
  letter-spacing: -0.02em;
}
.hero-subtitle {
  font-size: 15px;
  color: var(--text-secondary);
  margin: 0;
}
.dashboard-grid {
  display: grid;
  grid-template-columns: repeat(12, 1fr);
  gap: 24px;
}
.dashboard-card {
  min-height: 220px;
}
.card-large {
  grid-column: span 8;
}
.card-medium {
  grid-column: span 4;
}
.card-small {
  grid-column: span 12;
}
@media (max-width: 1200px) {
  .card-large, .card-medium, .card-small {
    grid-column: span 6;
  }
}
@media (max-width: 768px) {
  .card-large, .card-medium, .card-small {
    grid-column: span 12;
  }
}
.stat-primary {
  font-size: 48px;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: -0.03em;
  margin-bottom: 8px;
}
.stat-primary.danger {
  color: var(--danger);
}
.stat-delta {
  display: inline-block;
  font-size: 14px;
  color: var(--success);
  font-weight: 600;
  margin-left: 12px;
}
.warning-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--danger);
  box-shadow: 0 0 0 4px rgba(255,59,48,0.2);
}
.card-desc {
  font-size: 14px;
  color: var(--text-secondary);
  margin-top: 12px;
}
.status-bars {
  display: flex;
  flex-direction: column;
  gap: 18px;
}
.status-bar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}
.status-bar-value {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}
.status-bar-track {
  height: 8px;
  background: var(--border);
  border-radius: 4px;
  overflow: hidden;
}
.status-bar-fill {
  height: 100%;
  border-radius: 4px;
  transition: width 1s cubic-bezier(0.25, 0.1, 0.25, 1.0);
}
.resource-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}
.resource-item {
  text-align: center;
  padding: 16px;
  border-radius: 16px;
  background: var(--bg-primary);
}
.resource-value {
  font-size: 28px;
  font-weight: 700;
  margin: 12px 0 4px;
  color: var(--text-primary);
}
.resource-label {
  font-size: 13px;
  color: var(--text-secondary);
}
</style>
```

- [ ] **Step 4: 验证 Dashboard**

登录后应看到新的 Dashboard，数字 count-up、趋势图、状态条、资源统计均正常显示真实数据。

---

## Task 8: 内容页样式统一

**Files:**
- Modify: `admin-frontend/src/views/workorder/list.vue`
- Modify: `admin-frontend/src/views/workorder/create.vue`
- Modify: `admin-frontend/src/views/workorder/detail.vue`
- Modify: `admin-frontend/src/views/device/index.vue`
- Modify: `admin-frontend/src/views/personnel/index.vue`
- Modify: `admin-frontend/src/views/codetable/index.vue`
- Modify: `admin-frontend/src/views/sla/index.vue`
- Modify: `admin-frontend/src/views/team/index.vue`

**Interfaces:**
- Consumes: `AppCard`、`StatusTag`、CSS 变量。
- Produces: 所有内容页统一 Apple 风卡片、表格、按钮样式。

- [ ] **Step 1: 改造 `workorder/list.vue`**

将最外层 `<div class="workorder-list">` 下的两个 `el-card` 替换为 `AppCard`。例如：

```vue
<template>
  <div class="workorder-list">
    <AppCard class="filter-card">
      <el-form :inline="true" :model="query" size="default">
        ...
        <el-form-item>
          <el-button type="primary" round @click="handleQuery">查询</el-button>
          <el-button round @click="handleReset">重置</el-button>
          <el-button type="success" round @click="$router.push('/work-order/create')">新建工单</el-button>
        </el-form-item>
      </el-form>
    </AppCard>

    <AppCard class="table-card">
      <el-table ...>
        ...
      </el-table>
      <div class="pagination-wrapper">
        <el-pagination ... />
      </div>
    </AppCard>
  </div>
</template>
```

添加 scoped 样式：

```css
.workorder-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}
.filter-card :deep(.app-card-body) {
  padding-bottom: 12px;
}
.table-card {
  flex: 1;
}
.pagination-wrapper {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
}
```

将 `el-table` 的 `stripe` 属性改为 `:stripe="false"`，行 hover 效果由自定义 CSS 覆盖：

```css
:deep(.el-table__row:hover > td) {
  background-color: rgba(0,113,227,0.04) !important;
}
```

将状态 `el-tag` 替换为 `StatusTag` 组件（可选，可保持 el-tag 但改为 pill 形状）。

- [ ] **Step 2: 改造其余页面**

按同样逻辑：
- 每个页面外层用 `AppCard` 包裹。
- 按钮改为 `round`。
- 表格取消 `stripe`，加 hover 样式。
- 分页器加 `pagination-wrapper` 样式右对齐。
- 弹窗标题、输入框圆角已在 Element Plus 变量中控制。

- [ ] **Step 3: 验证所有页面**

逐页检查：工单列表、新建工单、工单详情、设备台账、人员管理、码表管理、SLA 配置、班组查看。确保视觉上统一、功能无回归。

---

## Task 9: 最终测试与交付

**Files:** 全项目

- [ ] **Step 1: 运行后端测试**

```bash
cd backend
mvnw.cmd test
```

预期所有测试通过。

- [ ] **Step 2: 运行前端 dev server 并完整走查**

```bash
cd admin-frontend
npm run dev
```

检查项：
- 登录页 Apple 风视觉正常。
- 登录后进入 Dashboard，数据真实且动画正常。
- 深色模式切换正常，所有文字对比度可接受。
- 路由切换有淡入上滑动画。
- 各内容页卡片、表格、按钮样式统一。
- 新建工单、设备导入导出、人员管理等功能仍可用。

- [ ] **Step 3: 使用一键启动器验证**

在项目根目录双击 `双击运行演示.exe`，确认：
- 后端、前端自动启动。
- 浏览器自动打开登录页。
- 系统整体视觉符合预期。

- [ ] **Step 4: 提交代码**

```bash
git add .
git commit -m "feat: Apple风格管理后台redesign，新增Dashboard、深色模式、动效及后端统计接口"
```

---

## 自检覆盖

| 规格要求 | 对应任务 |
|---|---|
| Tailwind + Element Plus 主题覆盖 | Task 1、2 |
| 登录页 Apple 风毛玻璃卡片 | Task 5 |
| 侧边栏 + Header 改造 | Task 4 |
| Dashboard 参考 Apple Health 桌面化 | Task 7 |
| Dashboard 真实数据联动 | Task 6、7 |
| 深色模式手动切换 + 对比度 | Task 2、4 |
| 路由切换动画 | Task 2 |
| 卡片/表格悬浮动效 | Task 3、8 |
| 数字 count-up、趋势图动画 | Task 3、7 |
| 骨架屏 loading | Task 3、7 |
| 内容页统一样式 | Task 8 |

---

## 风险与回退

- **风险 1**：Element Plus 深色 CSS 变量可能与自定义变量冲突。回退：在 `base.css` 中提高自定义选择器优先级或移除 Element Plus dark CSS 导入。
- **风险 2**：`StatisticsServiceImpl` 中 `apply("DATE(created_at) = ...")` 在 MySQL 中工作正常，但在 H2 测试中可能失败。回退：单元测试时使用 H2 兼容语法，或在测试配置中排除该接口测试。
- **风险 3**：Tailwind 与 Element Plus 类名冲突。回退：在 `tailwind.config.js` 中使用 `prefix: 'tw-'`。
