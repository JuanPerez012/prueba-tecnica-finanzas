<script setup>
import { onMounted } from 'vue'
import TransaccionForm from '../components/TransaccionForm.vue'
import TransaccionTable from '../components/TransaccionTable.vue'
import { useTransacciones } from '../composables/useTransacciones'
import { useProductos } from '../composables/useProductos'

const { transacciones, cargando, guardando, cargar, crear } = useTransacciones()
const { productos, cargar: cargarProductos } = useProductos()

onMounted(() => {
  cargar()
  cargarProductos()
})

async function alEnviarFormulario(payload) {
  try {
    await crear(payload)
    await cargarProductos()
  } catch {
    // el error se reporta vía toast dentro de useTransacciones
  }
}
</script>

<template>
  <div class="view-shell">
    <div class="app-layout">
      <TransaccionForm :productos="productos" :guardando="guardando" @submit="alEnviarFormulario" />

      <TransaccionTable :transacciones="transacciones" :productos="productos" :cargando="cargando" />
    </div>
  </div>
</template>
