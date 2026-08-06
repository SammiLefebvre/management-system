const AMAP_SCRIPT_ID = 'amap-script'

export function useAMapLoader() {
  const key = import.meta.env.VITE_AMAP_KEY

  function load(): Promise<typeof window.AMap> {
    if (!key) return Promise.reject(new Error('VITE_AMAP_KEY 未配置'))
    if (window.AMap) return Promise.resolve(window.AMap)
    const existing = document.getElementById(AMAP_SCRIPT_ID)
    if (existing) {
      return new Promise((resolve, reject) => {
        existing.addEventListener('load', () => resolve(window.AMap))
        existing.addEventListener('error', reject)
      })
    }
    return new Promise((resolve, reject) => {
      const script = document.createElement('script')
      script.id = AMAP_SCRIPT_ID
      script.src = `https://webapi.amap.com/maps?v=2.0&key=${key}`
      script.onload = () => resolve(window.AMap)
      script.onerror = reject
      document.head.appendChild(script)
    })
  }

  return { load, key }
}
