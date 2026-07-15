<script setup>
import { ref, computed } from 'vue'
import { etiquetaTipoCuenta, etiquetaEstadoCuenta, formatMoneda, formatFechaHora, idConsecutivo } from '../utils/formatters'

const props = defineProps({
  productos: {
    type: Array,
    default: () => []
  },
  cargando: {
    type: Boolean,
    default: false
  },
  eliminandoId: {
    type: [Number, String, null],
    default: null
  },
  idSeleccionado: {
    type: [Number, String, null],
    default: null
  }
})

const emit = defineEmits(['editar', 'eliminar'])

const busqueda = ref('')

const productosFiltrados = computed(() => {
  const q = busqueda.value.trim().toLowerCase()
  if (!q) return props.productos
  return props.productos.filter((p) => {
    return (
      p.numeroCuenta.toLowerCase().includes(q) ||
      p.clienteNombreCompleto?.toLowerCase().includes(q) ||
      p.clienteNumeroIdentificacion?.toLowerCase().includes(q)
    )
  })
})
</script>

<template>
  <section class="ledger" aria-labelledby="ledger-title">
    <div class="ledger__toolbar">
      <div class="ledger__heading">
        <h2 id="ledger-title" class="ledger__title">Productos</h2>
        <span class="ledger__tally" aria-live="polite">
          {{ productosFiltrados.length }}{{ busqueda ? ` de ${productos.length}` : '' }}
          {{ productosFiltrados.length === 1 ? 'registro' : 'registros' }}
        </span>
      </div>
      <input
        v-model="busqueda"
        type="search"
        class="ledger__search"
        placeholder="Buscar por número de cuenta, cliente o identificación…"
        aria-label="Buscar productos"
      />
    </div>

    <div v-if="cargando" class="ledger__state">Cargando registros…</div>

    <div v-else-if="productosFiltrados.length === 0" class="ledger__state ledger__state--empty">
      <template v-if="busqueda">No hay registros que coincidan con «{{ busqueda }}».</template>
      <template v-else>Aún no hay productos registrados. Usa el formulario para crear el primero.</template>
    </div>

    <div v-else class="ledger__table-wrap">
      <table class="ledger__table">
        <colgroup>
          <col style="width: 7%" />
          <col style="width: 11%" />
          <col style="width: 12%" />
          <col style="width: 9%" />
          <col style="width: 11%" />
          <col style="width: 8%" />
          <col style="width: 12%" />
          <col style="width: 12%" />
          <col style="width: 12%" />
          <col style="width: 150px" />
        </colgroup>
        <thead>
          <tr>
            <th scope="col">ID</th>
            <th scope="col">Tipo de cuenta</th>
            <th scope="col">Número de cuenta</th>
            <th scope="col">Estado</th>
            <th scope="col">Saldo</th>
            <th scope="col">Exenta GMF</th>
            <th scope="col">Creado</th>
            <th scope="col">Modificado</th>
            <th scope="col">Cliente</th>
            <th scope="col"><span class="sr-only">Acciones</span></th>
          </tr>
        </thead>
        <transition-group name="row" tag="tbody">
          <tr
            v-for="p in productosFiltrados"
            :key="p.id"
            :class="{ 'ledger__row--activo': p.id === idSeleccionado }"
          >
            <td>
              <span class="stamp">{{ idConsecutivo(p.id) }}</span>
            </td>
            <td>{{ etiquetaTipoCuenta(p.tipoCuenta) }}</td>
            <td class="mono">{{ p.numeroCuenta }}</td>
            <td>
              <span class="badge" :class="`badge--${p.estado.toLowerCase()}`">{{ etiquetaEstadoCuenta(p.estado) }}</span>
            </td>
            <td class="mono">{{ formatMoneda(p.saldo) }}</td>
            <td>{{ p.exentaGmf ? 'Sí' : 'No' }}</td>
            <td class="mono">{{ formatFechaHora(p.fechaCreacion) }}</td>
            <td class="mono">{{ formatFechaHora(p.fechaModificacion) }}</td>
            <td>
              <div class="ledger__cell-cliente">
                <span class="ledger__cell-nombre">{{ p.clienteNombreCompleto }}</span>
                <span class="mono ledger__cell-correo">{{ p.clienteNumeroIdentificacion }}</span>
              </div>
            </td>
            <td class="ledger__celda-acciones">
              <div class="ledger__acciones">
                <button type="button" class="btn btn--ghost btn--small" @click="emit('editar', p)">Editar</button>
                <button
                  type="button"
                  class="btn btn--danger btn--small"
                  :disabled="eliminandoId === p.id"
                  @click="emit('eliminar', p)"
                >
                  {{ eliminandoId === p.id ? 'Eliminando…' : 'Eliminar' }}
                </button>
              </div>
            </td>
          </tr>
        </transition-group>
      </table>
    </div>
  </section>
</template>
