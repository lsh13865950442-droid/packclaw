import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue')
  },
  {
    path: '/settings',
    name: 'Settings',
    component: () => import('../views/Settings.vue')
  },
  {
    path: '/skills',
    name: 'Skills',
    component: () => import('../views/Skills.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
