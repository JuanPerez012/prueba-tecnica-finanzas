<script setup>
import { ref, computed } from 'vue'
import { etiquetaTipoIdentificacion, calcularEdad, formatFechaHora, idConsecutivo } from '../utils/formatters'

const props = defineProps({
  clientes: {
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

const clientesFiltrados = computed(() => {
  const q = busqueda.value.trim().toLowerCase()
  if (!q) return props.clientes
  return props.clientes.filter((c) => {
    const nombreCompleto = `${c.nombres} ${c.apellidos}`.toLowerCase()
    return nombreCompleto.includes(q) || c.numeroIdentificacion.toLowerCase().includes(q) || c.correoElectronico.toLowerCase().includes(q)
  })
})
</script>

<template>
  <section class="ledger" aria-labelledby="ledger-title">
    <div class="ledger__toolbar">
      <div class="ledger__heading">
        <h2 id="ledger-title" class="ledger__title">Clientes</h2>
        <span class="ledger__tally" aria-live="polite">
          {{ clientesFiltrados.length }}{{ busqueda ? ` de ${clientes.length}` : '' }}
          {{ clientesFiltrados.length === 1 ? 'registro' : 'registros' }}
        </span>
      </div>
      <input
        v-model="busqueda"
        type="search"
        class="ledger__search"
        placeholder="Buscar por nombre, identificación o correo…"
        aria-label="Buscar clientes"
      />
    </div>

    <div v-if="cargando" class="ledger__state">Cargando registros…</div>

    <div v-else-if="clientesFiltrados.length === 0" class="ledger__state ledger__state--empty">
      <template v-if="busqueda">No hay registros que coincidan con «{{ busqueda }}».</template>
      <template v-else>Aún no hay clientes registrados. Usa el formulario para crear el primero.</template>
    </div>

    <div v-else class="ledger__table-wrap">
    <table class="ledger__table">
      <colgroup>
        <col style="width: 9%" />
        <col style="width: 18%" />
        <col style="width: 17%" />
        <col style="width: 20%" />
        <col style="width: 7%" />
        <col style="width: 12%" />
        <col style="width: 12%" />
        <col style="width: 160px" />
      </colgroup>
      <thead>
        <tr>
          <th scope="col">ID</th>
          <th scope="col">Cliente</th>
          <th scope="col">Identificación</th>
          <th scope="col">Correo</th>
          <th scope="col">Edad</th>
          <th scope="col">Creado</th>
          <th scope="col">Modificado</th>
          <th scope="col"><span class="sr-only">Acciones</span></th>
        </tr>
      </thead>
      <transition-group name="row" tag="tbody">
        <tr
          v-for="c in clientesFiltrados"
          :key="c.id"
          :class="{ 'ledger__row--activo': c.id === idSeleccionado }"
        >
          <td>
            <span class="stamp">{{ idConsecutivo(c.id) }}</span>
          </td>
          <td class="ledger__cell-nombre">{{ c.nombres }} {{ c.apellidos }}</td>
          <td class="mono">{{ etiquetaTipoIdentificacion(c.tipoIdentificacion) }} · {{ c.numeroIdentificacion }}</td>
          <td class="mono ledger__cell-correo">{{ c.correoElectronico }}</td>
          <td class="mono">{{ calcularEdad(c.fechaNacimiento) }}</td>
          <td class="mono">{{ formatFechaHora(c.fechaCreacion) }}</td>
          <td class="mono">{{ formatFechaHora(c.fechaModificacion) }}</td>
          <td class="ledger__celda-acciones">
            <div class="ledger__acciones">
              <button type="button" class="btn btn--ghost btn--small" @click="emit('editar', c)">Editar</button>
              <button
                type="button"
                class="btn btn--danger btn--small"
                :disabled="eliminandoId === c.id"
                @click="emit('eliminar', c)"
              >
                {{ eliminandoId === c.id ? 'Eliminando…' : 'Eliminar' }}
              </button>
            </div>
          </td>
        </tr>
      </transition-group>
    </table>
    </div>
  </section>
</template>
