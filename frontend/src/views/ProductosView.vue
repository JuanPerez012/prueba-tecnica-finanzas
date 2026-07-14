<script setup>
import { ref, onMounted } from 'vue'
import ProductoForm from '../components/ProductoForm.vue'
import ProductoTable from '../components/ProductoTable.vue'
import ConfirmDialog from '../components/ConfirmDialog.vue'
import { useProductos } from '../composables/useProductos'
import { useClientes } from '../composables/useClientes'

const { productos, cargando, guardando, eliminandoId, cargar, crear, actualizar, eliminar } = useProductos()
const { clientes, cargar: cargarClientes } = useClientes()

const productoEnEdicion = ref(null)
const productoPendienteDeEliminar = ref(null)

onMounted(() => {
  cargar()
  cargarClientes()
})

async function alEnviarFormulario(payload) {
  if (productoEnEdicion.value) {
    await actualizar(productoEnEdicion.value.id, payload)
    productoEnEdicion.value = null
  } else {
    await crear(payload)
  }
}

function alEditar(producto) {
  productoEnEdicion.value = producto
  document.getElementById('form-title')?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

function alCancelarEdicion() {
  productoEnEdicion.value = null
}

function pedirConfirmacionEliminar(producto) {
  productoPendienteDeEliminar.value = producto
}

async function confirmarEliminacion() {
  const producto = productoPendienteDeEliminar.value
  productoPendienteDeEliminar.value = null
  if (!producto) return
  try {
    await eliminar(producto)
    if (productoEnEdicion.value?.id === producto.id) productoEnEdicion.value = null
  } catch {
    // el error se reporta vía toast dentro de useProductos
  }
}
</script>

<template>
  <div class="view-shell">
    <div class="app-layout">
      <ProductoForm
        :producto="productoEnEdicion"
        :clientes="clientes"
        :guardando="guardando"
        @submit="alEnviarFormulario"
        @cancelar="alCancelarEdicion"
      />

      <ProductoTable
        :productos="productos"
        :cargando="cargando"
        :eliminando-id="eliminandoId"
        :id-seleccionado="productoEnEdicion?.id ?? null"
        @editar="alEditar"
        @eliminar="pedirConfirmacionEliminar"
      />
    </div>

    <ConfirmDialog
      :abierto="Boolean(productoPendienteDeEliminar)"
      titulo="Eliminar producto"
      :mensaje="
        productoPendienteDeEliminar
          ? `¿Eliminar la cuenta ${productoPendienteDeEliminar.numeroCuenta}? Esta acción no se puede deshacer. Si el saldo no está en $0, el sistema rechazará la eliminación.`
          : ''
      "
      :cargando="Boolean(eliminandoId)"
      @confirmar="confirmarEliminacion"
      @cancelar="productoPendienteDeEliminar = null"
    />
  </div>
</template>
