export function formatDateTime(dateStr) {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

function pad(n) {
  return n < 10 ? '0' + n : n
}

export function statusLabel(status) {
  const map = {
    draft: '草稿', published: '待认领', claimed: '进行中-待签到',
    in_progress: '进行中-作业中', completing: '进行中-待完工',
    pending_confirm: '待确认', confirmed: '已确认',
    pending_force_close: '待关闭确认', closed: '已关闭'
  }
  return map[status] || status
}

export function chooseLocation() {
  return new Promise((resolve, reject) => {
    uni.chooseLocation({
      success: resolve,
      fail: reject
    })
  })
}

export function getCurrentLocation() {
  return new Promise((resolve, reject) => {
    uni.getLocation({
      type: 'gcj02',
      success: resolve,
      fail: reject
    })
  })
}

export function chooseImage(count = 1) {
  return new Promise((resolve, reject) => {
    uni.chooseImage({
      count,
      sizeType: ['compressed'],
      sourceType: ['camera', 'album'],
      success: resolve,
      fail: reject
    })
  })
}
