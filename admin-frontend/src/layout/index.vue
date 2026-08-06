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
          <el-menu-item index="/data-screen">
            <el-icon><MapLocation /></el-icon>
            <span>数据大屏</span>
          </el-menu-item>
          <el-menu-item index="/reports">
            <el-icon><Document /></el-icon>
            <span>报表中心</span>
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
    <AiChatWidget />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import SystemLogo from '@/components/icons/SystemLogo.vue'
import ThemeToggle from '@/components/ThemeToggle.vue'
import AiChatWidget from '@/components/AiChatWidget.vue'
import { DataLine, List, Monitor, User, Setting, Timer, Avatar, MapLocation, Document } from '@element-plus/icons-vue'

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

<script lang="ts">
export default { name: 'LayoutPage' }
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
