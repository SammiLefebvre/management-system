import { ElMessage, ElMessageBox } from 'element-plus'

const STORAGE_KEY = 'hf_api_token'

export function getHuggingFaceToken(): string | null {
  return localStorage.getItem(STORAGE_KEY)
}

export function setHuggingFaceToken(token: string) {
  localStorage.setItem(STORAGE_KEY, token)
}

export function clearHuggingFaceToken() {
  localStorage.removeItem(STORAGE_KEY)
}

export async function ensureHuggingFaceToken(): Promise<string> {
  let token = getHuggingFaceToken()
  if (token) return token

  const { value } = await ElMessageBox.prompt(
    '使用 Hugging Face 大模型需要你的 API Token。Token 仅用于本次请求，不会保存在服务器。',
    '需要 Hugging Face API Key',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      inputPlaceholder: 'hf_xxxxxxxxxxxxxxxx',
      inputPattern: /^hf_[A-Za-z0-9_\-]+$/,
      inputErrorMessage: 'Token 格式不正确，应以 hf_ 开头'
    }
  )

  token = (value || '').trim()
  if (!token) {
    throw new Error('未提供 API Token')
  }
  setHuggingFaceToken(token)
  ElMessage.success('Token 已保存到本地浏览器')
  return token
}
