<template>
  <div class="face-capture">
    <div class="camera-wrapper">
      <video v-show="!captured" ref="videoRef" autoplay playsinline class="camera-video" />
      <img v-if="captured" :src="captured" class="camera-preview" alt="captured" />
      <div v-if="!stream" class="camera-placeholder">请允许摄像头权限</div>
    </div>
    <div class="camera-actions">
      <el-button v-if="!captured" type="primary" round :loading="capturing" @click="capture">
        拍照
      </el-button>
      <el-button v-else round @click="retake">重拍</el-button>
      <el-button type="success" round :disabled="!captured" @click="confirm">确认</el-button>
    </div>
    <canvas ref="canvasRef" style="display: none;" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'

const emit = defineEmits<{
  capture: [base64: string]
}>()

const videoRef = ref<HTMLVideoElement>()
const canvasRef = ref<HTMLCanvasElement>()
const stream = ref<MediaStream | null>(null)
const captured = ref('')
const capturing = ref(false)

async function startCamera() {
  try {
    stream.value = await navigator.mediaDevices.getUserMedia({ video: true })
    if (videoRef.value) {
      videoRef.value.srcObject = stream.value
    }
  } catch (e) {
    console.error(e)
  }
}

function capture() {
  const video = videoRef.value
  const canvas = canvasRef.value
  if (!video || !canvas) return
  canvas.width = video.videoWidth || 640
  canvas.height = video.videoHeight || 480
  const ctx = canvas.getContext('2d')
  if (!ctx) return
  ctx.drawImage(video, 0, 0, canvas.width, canvas.height)
  captured.value = canvas.toDataURL('image/jpeg')
}

function retake() {
  captured.value = ''
}

function confirm() {
  if (captured.value) {
    emit('capture', captured.value)
  }
}

function stopCamera() {
  stream.value?.getTracks().forEach(t => t.stop())
  stream.value = null
}

onMounted(startCamera)
onUnmounted(stopCamera)
</script>

<script lang="ts">
export default { name: 'FaceCapture' }
</script>

<style scoped>
.face-capture {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}
.camera-wrapper {
  width: 320px;
  height: 240px;
  border-radius: 20px;
  overflow: hidden;
  background: #000;
  position: relative;
  box-shadow: var(--shadow-md);
}
.camera-video,
.camera-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.camera-placeholder {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 14px;
}
.camera-actions {
  display: flex;
  gap: 12px;
}
</style>
