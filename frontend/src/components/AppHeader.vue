<script setup>
import { RouterLink, useRoute } from 'vue-router'

const route = useRoute()
</script>

<template>
  <header class="app-header">
    <RouterLink to="/" class="app-header__title">Libro de Clientes</RouterLink>

    <nav v-if="route.name !== 'inicio'" class="app-header__nav" aria-label="Navegación principal">
      <RouterLink to="/clientes" class="app-header__link">Clientes</RouterLink>
      <RouterLink to="/productos" class="app-header__link">Productos</RouterLink>
    </nav>
  </header>
</template>
