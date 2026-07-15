import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import ClientesView from '../views/ClientesView.vue'
import ProductosView from '../views/ProductosView.vue'
import TransaccionesView from '../views/TransaccionesView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'inicio', component: HomeView },
    { path: '/clientes', name: 'clientes', component: ClientesView },
    { path: '/productos', name: 'productos', component: ProductosView },
    { path: '/transacciones', name: 'transacciones', component: TransaccionesView }
  ],
  scrollBehavior() {
    return { top: 0 }
  }
})

export default router
