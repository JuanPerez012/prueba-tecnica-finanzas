<script setup>
import { ref, onMounted } from 'vue'
import AppHeader from './components/AppHeader.vue'
import ClienteForm from './components/ClienteForm.vue'
import ClienteTable from './components/ClienteTable.vue'
import ConfirmDialog from './components/ConfirmDialog.vue'
import ToastStack from './components/ToastStack.vue'
import { useClientes } from './composables/useClientes'

const { clientes, cargando, guardando, eliminandoId, cargar, crear, actualizar, eliminar } = useClientes()

const clienteEnEdicion = ref(null)
const clientePendienteDeEliminar = ref(null)

onMounted(cargar)

async function alEnviarFormulario(payload) {
  if (clienteEnEdicion.value) {
    await actualizar(clienteEnEdicion.value.id, payload)
    clienteEnEdicion.value = null
  } else {
    await crear(payload)
  }
}

function alEditar(cliente) {
  clienteEnEdicion.value = cliente
  document.getElementById('form-title')?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

function alCancelarEdicion() {
  clienteEnEdicion.value = null
}

function pedirConfirmacionEliminar(cliente) {
  clientePendienteDeEliminar.value = cliente
}

async function confirmarEliminacion() {
  const cliente = clientePendienteDeEliminar.value
  clientePendienteDeEliminar.value = null
  if (!cliente) return
  try {
    await eliminar(cliente)
    if (clienteEnEdicion.value?.id === cliente.id) clienteEnEdicion.value = null
  } catch {
    // el error ya se reporta vía toast dentro de useClientes
  }
}
</script>

<template>
  <div class="app-shell">
    <AppHeader />

    <main class="app-layout">
      <ClienteForm :cliente="clienteEnEdicion" :guardando="guardando" @submit="alEnviarFormulario" @cancelar="alCancelarEdicion" />

      <ClienteTable
        :clientes="clientes"
        :cargando="cargando"
        :eliminando-id="eliminandoId"
        :id-seleccionado="clienteEnEdicion?.id ?? null"
        @editar="alEditar"
        @eliminar="pedirConfirmacionEliminar"
      />
    </main>

    <ConfirmDialog
      :abierto="Boolean(clientePendienteDeEliminar)"
      titulo="Eliminar cliente"
      :mensaje="
        clientePendienteDeEliminar
          ? `¿Eliminar a ${clientePendienteDeEliminar.nombres} ${clientePendienteDeEliminar.apellidos}? Esta acción no se puede deshacer. Si el cliente tiene productos financieros asociados, el backend rechazará la eliminación.`
          : ''
      "
      :cargando="Boolean(eliminandoId)"
      @confirmar="confirmarEliminacion"
      @cancelar="clientePendienteDeEliminar = null"
    />

    <ToastStack />
  </div>
</template>
