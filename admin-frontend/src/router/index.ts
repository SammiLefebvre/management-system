import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/dashboard/index.vue'),
    meta: { title: '数据概览' }
  },
  {
    path: '/',
    component: () => import('@/layout/index.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'data-screen',
        name: 'DataScreen',
        component: () => import('@/views/data-screen/index.vue'),
        meta: { title: '数据大屏' }
      },
      {
        path: 'reports',
        name: 'Reports',
        component: () => import('@/views/reports/index.vue'),
        meta: { title: '报表中心' }
      },
      {
        path: 'work-order',
        name: 'WorkOrder',
        component: () => import('@/views/workorder/list.vue'),
        meta: { title: '工单管理' }
      },
      {
        path: 'work-order/create',
        name: 'WorkOrderCreate',
        component: () => import('@/views/workorder/create.vue'),
        meta: { title: '新建工单' }
      },
      {
        path: 'work-order/:id',
        name: 'WorkOrderDetail',
        component: () => import('@/views/workorder/detail.vue'),
        meta: { title: '工单详情' }
      },
      {
        path: 'device',
        name: 'Device',
        component: () => import('@/views/device/index.vue'),
        meta: { title: '设备台账' }
      },
      {
        path: 'personnel',
        name: 'Personnel',
        component: () => import('@/views/personnel/index.vue'),
        meta: { title: '人员管理' }
      },
      {
        path: 'code-table',
        name: 'CodeTable',
        component: () => import('@/views/codetable/index.vue'),
        meta: { title: '码表管理' }
      },
      {
        path: 'sla',
        name: 'SlaConfig',
        component: () => import('@/views/sla/index.vue'),
        meta: { title: 'SLA 配置' }
      },
      {
        path: 'team',
        name: 'Team',
        component: () => import('@/views/team/index.vue'),
        meta: { title: '班组查看' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/')
  } else {
    next()
  }
})

export default router
