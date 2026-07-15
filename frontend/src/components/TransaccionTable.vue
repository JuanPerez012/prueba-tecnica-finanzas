<script setup>
import { ref, computed } from 'vue'
import {
  etiquetaTipoTransaccion,
  formatMoneda,
  formatFechaHora,
  idConsecutivo
} from '../utils/formatters'

const props = defineProps({
  transacciones: {
    type: Array,
    default: () => []
  },
  productos: {
    type: Array,
    default: () => []
  },
  cargando: {
    type: Boolean,
    default: false
  }
})

const busqueda = ref('')
const mostrarDeCanceladas = ref(false)

const idsCuentasCanceladas = computed(
  () => new Set(props.productos.filter((p) => p.estado === 'CANCELADA').map((p) => p.id))
)

function involucraCuentaCancelada(transaccion) {
  return (
    idsCuentasCanceladas.value.has(transaccion.productoOrigenId) ||
    idsCuentasCanceladas.value.has(transaccion.productoDestinoId)
  )
}

const hayOcultasPorCancelacion = computed(
  () => !mostrarDeCanceladas.value && props.transacciones.some(involucraCuentaCancelada)
)

const transaccionesFiltradas = computed(() => {
  const base = mostrarDeCanceladas.value
    ? props.transacciones
    : props.transacciones.filter((t) => !involucraCuentaCancelada(t))

  const q = busqueda.value.trim().toLowerCase()
  if (!q) return base
  return base.filter((t) => {
    return (
      etiquetaTipoTransaccion(t.tipoTransaccion).toLowerCase().includes(q) ||
      t.productoOrigenNumeroCuenta?.toLowerCase().includes(q) ||
      t.productoDestinoNumeroCuenta?.toLowerCase().includes(q) ||
      t.descripcion?.toLowerCase().includes(q)
    )
  })
})

function saldoResultante(transaccion, productoId) {
  if (!productoId) return null
  return transaccion.movimientos?.find((m) => m.productoId === productoId)?.saldoResultante ?? null
}
</script>

<template>
  <section class="ledger" aria-labelledby="ledger-title">
    <div class="ledger__toolbar">
      <div class="ledger__heading">
        <h2 id="ledger-title" class="ledger__title">Movimientos</h2>
        <span class="ledger__tally" aria-live="polite">
          {{ transaccionesFiltradas.length }}{{ busqueda ? ` de ${transacciones.length}` : '' }}
          {{ transaccionesFiltradas.length === 1 ? 'registro' : 'registros' }}
        </span>
      </div>
      <div class="ledger__toolbar-controls">
        <label class="checkbox checkbox--inline">
          <input v-model="mostrarDeCanceladas" type="checkbox" />
          Mostrar de cuentas canceladas
        </label>
        <input
          v-model="busqueda"
          type="search"
          class="ledger__search"
          placeholder="Buscar por tipo, número de cuenta o descripción…"
          aria-label="Buscar transacciones"
        />
      </div>
    </div>

    <p v-if="hayOcultasPorCancelacion" class="ledger__hint">
      Hay movimientos de cuentas canceladas ocultos. Actívalos con «Mostrar de cuentas canceladas» para verlos.
    </p>

    <div v-if="cargando" class="ledger__state">Cargando registros…</div>

    <div v-else-if="transaccionesFiltradas.length === 0" class="ledger__state ledger__state--empty">
      <template v-if="busqueda">No hay registros que coincidan con «{{ busqueda }}».</template>
      <template v-else>Aún no hay transacciones registradas. Usa el formulario para crear la primera.</template>
    </div>

    <div v-else class="ledger__table-wrap">
      <table class="ledger__table">
        <colgroup>
          <col style="width: 7%" />
          <col style="width: 11%" />
          <col style="width: 12%" />
          <col style="width: 22%" />
          <col style="width: 22%" />
          <col style="width: 14%" />
          <col style="width: 12%" />
        </colgroup>
        <thead>
          <tr>
            <th scope="col">Folio</th>
            <th scope="col">Tipo</th>
            <th scope="col">Monto</th>
            <th scope="col">Cuenta origen</th>
            <th scope="col">Cuenta destino</th>
            <th scope="col">Descripción</th>
            <th scope="col">Fecha</th>
          </tr>
        </thead>
        <transition-group name="row" tag="tbody">
          <tr v-for="t in transaccionesFiltradas" :key="t.id">
            <td>
              <span class="stamp">{{ idConsecutivo(t.id) }}</span>
            </td>
            <td>
              <span class="badge" :class="`badge--${t.tipoTransaccion.toLowerCase()}`">
                {{ etiquetaTipoTransaccion(t.tipoTransaccion) }}
              </span>
            </td>
            <td class="mono">{{ formatMoneda(t.monto) }}</td>
            <td>
              <div v-if="t.productoOrigenId" class="ledger__cell-cliente">
                <span class="mono ledger__cell-nombre">{{ t.productoOrigenNumeroCuenta }}</span>
                <span class="mono ledger__cell-correo">saldo: {{ formatMoneda(saldoResultante(t, t.productoOrigenId)) }}</span>
              </div>
              <span v-else>—</span>
            </td>
            <td>
              <div v-if="t.productoDestinoId" class="ledger__cell-cliente">
                <span class="mono ledger__cell-nombre">{{ t.productoDestinoNumeroCuenta }}</span>
                <span class="mono ledger__cell-correo">saldo: {{ formatMoneda(saldoResultante(t, t.productoDestinoId)) }}</span>
              </div>
              <span v-else>—</span>
            </td>
            <td>{{ t.descripcion || '—' }}</td>
            <td class="mono">{{ formatFechaHora(t.fechaCreacion) }}</td>
          </tr>
        </transition-group>
      </table>
    </div>
  </section>
</template>
