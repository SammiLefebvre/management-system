import request from './request'

export function uploadFile(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/api/file/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
