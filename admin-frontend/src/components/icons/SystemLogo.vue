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

<script lang="ts">
export default { name: 'SystemLogo' }
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
