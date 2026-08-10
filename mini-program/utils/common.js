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

/** 详情页作业步骤提示（对齐状态机：claimed→签到, in_progress→排查, completing→完工） */
export function stepTip(status) {
  const map = {
    published: '可认领此工单；认领后需到现场签到开始作业',
    claimed: '第 1 步：到现场签到（定位 + 拍照）',
    in_progress: '第 2 步：填写排查过程并上传排查照片',
    completing: '第 3 步：填写维修结果并上传结束照片（后端自动水印）',
    pending_confirm: '已提交完工，等待内场确认归档',
    draft: '草稿可继续编辑后发布'
  }
  return map[status] || ''
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

/**
 * @param {number} count
 * @param {{ cameraOnly?: boolean }} [opts]
 */
export function chooseImage(count = 1, opts = {}) {
  return new Promise((resolve, reject) => {
    uni.chooseImage({
      count,
      sizeType: ['compressed'],
      sourceType: opts.cameraOnly ? ['camera'] : ['camera', 'album'],
      success: resolve,
      fail: reject
    })
  })
}

