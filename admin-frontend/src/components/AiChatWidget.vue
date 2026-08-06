<template>
  <div class="ai-chat-widget">
    <Transition name="pop">
      <div v-if="open" class="chat-panel">
        <div class="chat-header">
          <span>AI 助手</span>
          <el-button text circle size="small" @click="open = false">
            <el-icon><Close /></el-icon>
          </el-button>
        </div>
        <div ref="messagesRef" class="chat-messages">
          <div
            v-for="(msg, idx) in messages"
            :key="idx"
            class="chat-message"
            :class="msg.role"
          >
            <div class="chat-bubble" v-html="renderMarkdown(msg.content)" />
          </div>
          <div v-if="loading" class="chat-message assistant">
            <div class="chat-bubble thinking">思考中...</div>
          </div>
        </div>
        <div class="chat-input-row">
          <el-input
            v-model="input"
            placeholder="问点什么，例如：今天有几条超期工单？"
            size="large"
            @keyup.enter="send"
          />
          <el-button type="primary" size="large" round :loading="loading" @click="send">
            发送
          </el-button>
        </div>
      </div>
    </Transition>
    <el-button
      v-if="!open"
      class="chat-fab"
      type="primary"
      circle
      size="large"
      @click="open = true"
    >
      <el-icon size="24"><ChatDotRound /></el-icon>
    </el-button>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { ChatDotRound, Close } from '@element-plus/icons-vue'
import { chat } from '@/api/ai'

interface Message {
  role: 'user' | 'assistant'
  content: string
}

const open = ref(false)
const input = ref('')
const messages = ref<Message[]>([
  { role: 'assistant', content: '你好，我是工单系统 AI 助手，可以帮你查数据、给派单建议。' }
])
const loading = ref(false)
const messagesRef = ref<HTMLElement>()

function renderMarkdown(text: string) {
  // 简单渲染：把换行变成 <br>
  return text.replace(/\n/g, '<br>')
}

async function send() {
  const text = input.value.trim()
  if (!text) return
  messages.value.push({ role: 'user', content: text })
  input.value = ''
  loading.value = true
  scroll()
  try {
    const res = await chat(text)
    messages.value.push({ role: 'assistant', content: res.data })
  } catch (e) {
    ElMessage.error('AI 服务暂时不可用')
  } finally {
    loading.value = false
    scroll()
  }
}

function scroll() {
  nextTick(() => {
    const el = messagesRef.value
    if (el) el.scrollTop = el.scrollHeight
  })
}

watch(messages, scroll, { deep: true })
</script>

<script lang="ts">
export default { name: 'AiChatWidget' }
</script>

<style scoped>
.ai-chat-widget {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 1000;
}
.chat-fab {
  width: 56px;
  height: 56px;
  box-shadow: 0 8px 24px rgba(0, 113, 227, 0.35);
}
.chat-panel {
  width: 380px;
  height: 520px;
  background: var(--card-bg, rgba(255, 255, 255, 0.95));
  border-radius: 24px;
  box-shadow: var(--shadow-lg);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(0, 0, 0, 0.06);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.dark .chat-panel {
  background: rgba(30, 30, 30, 0.95);
  border-color: rgba(255, 255, 255, 0.08);
}
.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  font-weight: 600;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}
.chat-messages {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.chat-message {
  display: flex;
}
.chat-message.user {
  justify-content: flex-end;
}
.chat-bubble {
  max-width: 80%;
  padding: 12px 16px;
  border-radius: 18px;
  font-size: 14px;
  line-height: 1.5;
  background: #f2f2f7;
  color: var(--text-primary);
}
.dark .chat-bubble {
  background: #2c2c2e;
}
.chat-message.user .chat-bubble {
  background: var(--accent, #0071e3);
  color: #fff;
}
.chat-message.assistant .chat-bubble {
  border-bottom-left-radius: 4px;
}
.chat-message.user .chat-bubble {
  border-bottom-right-radius: 4px;
}
.thinking {
  color: var(--text-secondary);
}
.chat-input-row {
  display: flex;
  gap: 8px;
  padding: 12px 16px 20px;
  border-top: 1px solid rgba(0, 0, 0, 0.06);
}
.pop-enter-active,
.pop-leave-active {
  transition: all 0.25s cubic-bezier(0.25, 0.1, 0.25, 1);
}
.pop-enter-from,
.pop-leave-to {
  opacity: 0;
  transform: translateY(20px) scale(0.96);
}
</style>
