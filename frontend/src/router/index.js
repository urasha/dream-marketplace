import { createRouter, createWebHistory } from 'vue-router'

const HomePage = () => import('../components/pages/HomePage.vue')
const DreamDetailPage = () => import('../components/pages/DreamDetailPage.vue')
const CreateDreamPage = () => import('../components/pages/CreateDreamPage.vue')
const CreateLotPage = () => import('../components/pages/CreateLotPage.vue')
const LotDetailPage = () => import('../components/pages/LotDetailPage.vue')
const PurchaseFlow = () => import('../components/pages/PurchaseFlow.vue')
const ProfilePage = () => import('../components/pages/ProfilePage.vue')
const NotificationsPage = () => import('../components/pages/NotificationsPage.vue')
const AdminPage = () => import('../components/pages/AdminPage.vue')

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'home', component: HomePage },
    { path: '/dreams/create', name: 'create-dream', component: CreateDreamPage },
    { path: '/dreams/:id', name: 'dream-detail', component: DreamDetailPage, props: (route) => ({ dreamId: Number(route.params.id) || null }) },
    { path: '/lots/create', name: 'create-lot', component: CreateLotPage },
    { path: '/lots/:id', name: 'lot-detail', component: LotDetailPage, props: (route) => ({ lotId: Number(route.params.id) || null }) },
    { path: '/purchase/:id', name: 'purchase', component: PurchaseFlow, props: (route) => ({ lotId: Number(route.params.id) || null }) },
    { path: '/profile', name: 'profile', component: ProfilePage },
    { path: '/notifications', name: 'notifications', component: NotificationsPage },
    { path: '/admin', name: 'admin', component: AdminPage },
    { path: '/:pathMatch(.*)*', redirect: '/' },
  ],
})
