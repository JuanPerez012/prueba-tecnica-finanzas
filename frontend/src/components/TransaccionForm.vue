<script setup>
import { reactive, watch, computed } from 'vue'
import { TIPOS_TRANSACCION, etiquetaTipoCuenta, formatMoneda } from '../utils/formatters'

const props = defineProps({
  productos: {
    type: Array,
    default: () => []
  },
  guardando: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['submit'])

const vacio = () => ({
  tipoTransaccion: 'CONSIGNACION',
  productoOrigenId: '',
  productoDestinoId: '',
  monto: '',
  descripcion: ''
})

const form = reactive(vacio())
const errores = reactive({})

const productosActivos = computed(() => props.productos.filter((p) => p.estado === 'ACTIVA'))

const requiereOrigen = computed(() => form.tipoTransaccion === 'RETIRO' || form.tipoTransaccion === 'TRANSFERENCIA')
const requiereDestino = computed(() => form.tipoTransaccion === 'CONSIGNACION' || form.tipoTransaccion === 'TRANSFERENCIA')

watch(
  () => form.tipoTransaccion,
  () => {
    if (!requiereOrigen.value) form.productoOrigenId = ''
    if (!requiereDestino.value) form.productoDestinoId = ''
    limpiarErrores()
  }
)

function limpiarErrores() {
  Object.keys(errores).forEach((k) => delete errores[k])
}

function opcionCuenta(p) {
  return `${p.numeroCuenta} · ${etiquetaTipoCuenta(p.tipoCuenta)} · ${p.clienteNombreCompleto} · saldo: ${formatMoneda(p.saldo)}`
}

function validar() {
  limpiarErrores()

  if (!form.tipoTransaccion) errores.tipoTransaccion = 'Selecciona un tipo de transacción'

  const monto = Number(form.monto)
  if (form.monto === '' || Number.isNaN(monto) || monto <= 0) {
    errores.monto = 'El monto debe ser mayor a $0'
  }

  if (requiereOrigen.value && !form.productoOrigenId) {
    errores.productoOrigenId = 'Selecciona la cuenta origen'
  }

  if (requiereDestino.value && !form.productoDestinoId) {
    errores.productoDestinoId = 'Selecciona la cuenta destino'
  }

  if (
    form.tipoTransaccion === 'TRANSFERENCIA' &&
    form.productoOrigenId &&
    form.productoDestinoId &&
    form.productoOrigenId === form.productoDestinoId
  ) {
    errores.productoDestinoId = 'La cuenta destino debe ser distinta de la cuenta origen'
  }

  return Object.keys(errores).length === 0
}

function alEnviar() {
  if (!validar()) return

  emit('submit', {
    tipoTransaccion: form.tipoTransaccion,
    monto: Number(form.monto),
    productoOrigenId: requiereOrigen.value ? Number(form.productoOrigenId) : null,
    productoDestinoId: requiereDestino.value ? Number(form.productoDestinoId) : null,
    descripcion: form.descripcion.trim() || null
  })

  Object.assign(form, vacio())
  limpiarErrores()
}
</script>

<template>
  <section class="slip" aria-labelledby="form-title">
    <h2 id="form-title" class="slip__title">Registrar transacción</h2>

    <form class="slip-form" @submit.prevent="alEnviar" novalidate>
      <div class="field">
        <label for="tipoTransaccion">Tipo de transacción</label>
        <select id="tipoTransaccion" v-model="form.tipoTransaccion" :aria-invalid="Boolean(errores.tipoTransaccion)">
          <option v-for="t in TIPOS_TRANSACCION" :key="t.value" :value="t.value">{{ t.label }}</option>
        </select>
        <p v-if="errores.tipoTransaccion" class="field__error">{{ errores.tipoTransaccion }}</p>
      </div>

      <div class="field-row">
        <div v-if="requiereOrigen" class="field">
          <label for="productoOrigenId">Cuenta origen</label>
          <select id="productoOrigenId" v-model="form.productoOrigenId" :aria-invalid="Boolean(errores.productoOrigenId)">
            <option value="" disabled>Selecciona la cuenta origen…</option>
            <option v-for="p in productosActivos" :key="p.id" :value="p.id">{{ opcionCuenta(p) }}</option>
          </select>
          <p v-if="errores.productoOrigenId" class="field__error">{{ errores.productoOrigenId }}</p>
        </div>

        <div v-if="requiereDestino" class="field">
          <label for="productoDestinoId">Cuenta destino</label>
          <select id="productoDestinoId" v-model="form.productoDestinoId" :aria-invalid="Boolean(errores.productoDestinoId)">
            <option value="" disabled>Selecciona la cuenta destino…</option>
            <option v-for="p in productosActivos" :key="p.id" :value="p.id">{{ opcionCuenta(p) }}</option>
          </select>
          <p v-if="errores.productoDestinoId" class="field__error">{{ errores.productoDestinoId }}</p>
        </div>
      </div>

      <p v-if="!productosActivos.length" class="field__hint">
        No hay cuentas activas disponibles. Crea o activa una cuenta en la sección de Productos primero.
      </p>

      <div class="field-row">
        <div class="field">
          <label for="monto">Monto</label>
          <input
            id="monto"
            v-model="form.monto"
            type="number"
            min="0.01"
            step="0.01"
            placeholder="0"
            :aria-invalid="Boolean(errores.monto)"
          />
          <p v-if="errores.monto" class="field__error">{{ errores.monto }}</p>
        </div>
        <div class="field">
          <label for="descripcion">Descripción (opcional)</label>
          <input id="descripcion" v-model="form.descripcion" type="text" maxlength="255" placeholder="Nota u observación…" />
        </div>
      </div>

      <div class="slip-form__actions">
        <button type="submit" class="btn btn--brass" :disabled="guardando">
          {{ guardando ? 'Registrando…' : 'Registrar transacción' }}
        </button>
      </div>
    </form>
  </section>
</template>
