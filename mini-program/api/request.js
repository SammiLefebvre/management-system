const BASE_URL = 'http://localhost:9090'

/** 相对路径补全为可访问 URL（上传返回常为 /uploads/...） */
export function fullUrl(u) {
  if (!u) return u
  if (typeof u !== 'string') return u
  return u.startsWith('http') ? u : BASE_URL + u
}

function request(options) {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token')
    uni.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header: {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : ''
      },
      success: (res) => {
        if (res.statusCode === 401) {
          uni.removeStorageSync('token')
          uni.removeStorageSync('userInfo')
          uni.showToast({ title: '登录已过期', icon: 'none' })
          uni.reLaunch({ url: '/pages/index/index' })
          return reject(new Error('未登录'))
        }
        if (res.data.code !== 200) {
          uni.showToast({ title: res.data.message || '请求失败', icon: 'none' })
          return reject(new Error(res.data.message))
        }
        resolve(res.data)
      },
      fail: (err) => {
        uni.showToast({ title: '网络错误', icon: 'none' })
        reject(err)
      }
    })
  })
}

function uploadFile(filePath) {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token')
    uni.uploadFile({
      url: `${BASE_URL}/api/file/upload`,
      filePath,
      name: 'file',
      header: {
        'Authorization': token ? `Bearer ${token}` : ''
      },
      success: (res) => {
        const data = JSON.parse(res.data)
        if (data.code !== 200) {
          uni.showToast({ title: data.message || '上传失败', icon: 'none' })
          return reject(new Error(data.message))
        }
        // 兼容返回 string URL 或 { url }；存库用相对路径，展示侧自行 fullUrl
        const raw = typeof data.data === 'string' ? data.data : (data.data?.url || data.data)
        resolve(raw)
      },
      fail: (err) => {
        uni.showToast({ title: '上传失败', icon: 'none' })
        reject(err)
      }
    })
  })
}

export default {
  BASE_URL,
  fullUrl,
  get(url, data) { return request({ url, method: 'GET', data }) },
  post(url, data) { return request({ url, method: 'POST', data }) },
  put(url, data) { return request({ url, method: 'PUT', data }) },
  delete(url, data) { return request({ url, method: 'DELETE', data }) },
  upload: uploadFile
}
