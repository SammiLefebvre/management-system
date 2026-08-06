const BASE_URL = 'http://localhost:9090'

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
        resolve(data.data)
      },
      fail: (err) => {
        uni.showToast({ title: '上传失败', icon: 'none' })
        reject(err)
      }
    })
  })
}

export default {
  get(url, data) { return request({ url, method: 'GET', data }) },
  post(url, data) { return request({ url, method: 'POST', data }) },
  put(url, data) { return request({ url, method: 'PUT', data }) },
  delete(url, data) { return request({ url, method: 'DELETE', data }) },
  upload: uploadFile
}
