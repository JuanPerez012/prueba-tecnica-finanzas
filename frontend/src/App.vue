<script setup>
import AppHeader from './components/AppHeader.vue'
import ToastStack from './components/ToastStack.vue'
</script>

<template>
  <div class="app-shell">
    <AppHeader />

    <main>
      <router-view />
    </main>

    <ToastStack />
  </div>
</template>
