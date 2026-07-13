<script setup>
import { useToast } from '../composables/useToast'

const { toasts, descartar } = useToast()
</script>

<template>
  <div class="toast-stack" aria-live="polite" aria-atomic="true">
    <div v-for="t in toasts" :key="t.id" class="toast" :class="`toast--${t.tipo}`">
      <p class="toast__mensaje">{{ t.mensaje }}</p>
      <ul v-if="t.detalles && t.detalles.length" class="toast__detalles">
        <li v-for="(d, i) in t.detalles" :key="i">{{ d }}</li>
      </ul>
      <button type="button" class="toast__cerrar" aria-label="Cerrar notificación" @click="descartar(t.id)">×</button>
    </div>
  </div>
</template>
