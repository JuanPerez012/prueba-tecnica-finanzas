<script setup>
defineProps({
  abierto: { type: Boolean, default: false },
  titulo: { type: String, required: true },
  mensaje: { type: String, required: true },
  cargando: { type: Boolean, default: false }
})

const emit = defineEmits(['confirmar', 'cancelar'])
</script>

<template>
  <div v-if="abierto" class="dialog-backdrop" @keydown.esc="emit('cancelar')">
    <div class="dialog" role="alertdialog" aria-modal="true" :aria-label="titulo">
      <h3 class="dialog__title">{{ titulo }}</h3>
      <p class="dialog__mensaje">{{ mensaje }}</p>
      <div class="dialog__acciones">
        <button type="button" class="btn btn--ghost" @click="emit('cancelar')">Cancelar</button>
        <button type="button" class="btn btn--danger" :disabled="cargando" @click="emit('confirmar')">
          {{ cargando ? 'Eliminando…' : 'Sí, eliminar' }}
        </button>
      </div>
    </div>
  </div>
</template>
