<script setup>
import { reactive, watch, computed } from 'vue'
import { TIPOS_CUENTA, ESTADOS_CUENTA, etiquetaTipoCuenta, formatMoneda } from '../utils/formatters'

const props = defineProps({
  producto: {
    type: Object,
    default: null
  },
  clientes: {
    type: Array,
    default: () => []
  },
  guardando: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['submit', 'cancelar'])

const vacio = () => ({
  tipoCuenta: 'AHORROS',
  clienteId: '',
  saldoInicial: '0',
  exentaGmf: false,
  estado: 'ACTIVA'
})

const form = reactive(vacio())
const errores = reactive({})

const esEdicion = computed(() => Boolean(props.producto))

watch(
  () => props.producto,
  (nuevo) => {
    Object.assign(
      form,
      nuevo
        ? { ...vacio(), estado: nuevo.estado, exentaGmf: nuevo.exentaGmf }
        : vacio()
    )
    limpiarErrores()
  },
  { immediate: true }
)

function limpiarErrores() {
  Object.keys(errores).forEach((k) => delete errores[k])
}

function validarCreacion() {
  limpiarErrores()

  if (!form.tipoCuenta) errores.tipoCuenta = 'Selecciona un tipo de cuenta'
  if (!form.clienteId) errores.clienteId = 'Selecciona el cliente propietario'

  const saldo = Number(form.saldoInicial)
  if (form.saldoInicial !== '' && (Number.isNaN(saldo) || saldo < 0)) {
    errores.saldoInicial = 'El saldo inicial no puede ser negativo'
  }

  return Object.keys(errores).length === 0
}

function validarEdicion() {
  limpiarErrores()
  if (!form.estado) errores.estado = 'Selecciona un estado'
  return Object.keys(errores).length === 0
}

function alEnviar() {
  if (esEdicion.value) {
    if (!validarEdicion()) return
    emit('submit', { estado: form.estado, exentaGmf: form.exentaGmf })
  } else {
    if (!validarCreacion()) return
    emit('submit', {
      tipoCuenta: form.tipoCuenta,
      clienteId: Number(form.clienteId),
      saldoInicial: form.saldoInicial === '' ? 0 : Number(form.saldoInicial),
      exentaGmf: form.exentaGmf
    })
  }
}

function alCancelar() {
  Object.assign(form, vacio())
  limpiarErrores()
  emit('cancelar')
}
</script>

<template>
  <section class="slip" aria-labelledby="form-title">
    <h2 id="form-title" class="slip__title">
      {{ esEdicion ? `Editar cuenta ${producto.numeroCuenta}` : 'Registrar producto' }}
    </h2>

    <form class="slip-form" @submit.prevent="alEnviar" novalidate>
      <template v-if="esEdicion">
        <dl class="slip-readonly">
          <div>
            <dt>Número de cuenta</dt>
            <dd class="mono">{{ producto.numeroCuenta }}</dd>
          </div>
          <div>
            <dt>Tipo de cuenta</dt>
            <dd>{{ etiquetaTipoCuenta(producto.tipoCuenta) }}</dd>
          </div>
          <div>
            <dt>Cliente propietario</dt>
            <dd>{{ producto.clienteNombreCompleto }}</dd>
          </div>
          <div>
            <dt>Saldo actual</dt>
            <dd class="mono">{{ formatMoneda(producto.saldo) }}</dd>
          </div>
        </dl>
        <p class="slip-form__hint">
          El saldo y el tipo de cuenta no son editables aquí: el saldo solo cambia por
          transacciones, y el número/tipo de cuenta se fijan al crearla.
        </p>

        <div class="field-row">
          <div class="field">
            <label for="estado">Estado</label>
            <select id="estado" v-model="form.estado" :aria-invalid="Boolean(errores.estado)">
              <option v-for="e in ESTADOS_CUENTA" :key="e.value" :value="e.value">{{ e.label }}</option>
            </select>
            <p v-if="errores.estado" class="field__error">{{ errores.estado }}</p>
          </div>
          <div class="field field--checkbox">
            <label class="checkbox">
              <input v-model="form.exentaGmf" type="checkbox" />
              Exenta de GMF
            </label>
          </div>
        </div>
      </template>

      <template v-else>
        <div class="field-row">
          <div class="field">
            <label for="tipoCuenta">Tipo de cuenta</label>
            <select id="tipoCuenta" v-model="form.tipoCuenta" :aria-invalid="Boolean(errores.tipoCuenta)">
              <option v-for="t in TIPOS_CUENTA" :key="t.value" :value="t.value">{{ t.label }}</option>
            </select>
            <p v-if="errores.tipoCuenta" class="field__error">{{ errores.tipoCuenta }}</p>
          </div>
          <div class="field">
            <label for="clienteId">Cliente propietario</label>
            <select id="clienteId" v-model="form.clienteId" :aria-invalid="Boolean(errores.clienteId)">
              <option value="" disabled>Selecciona un cliente…</option>
              <option v-for="c in clientes" :key="c.id" :value="c.id">
                {{ c.nombres }} {{ c.apellidos }} · {{ c.numeroIdentificacion }}
              </option>
            </select>
            <p v-if="errores.clienteId" class="field__error">{{ errores.clienteId }}</p>
            <p v-if="!clientes.length" class="field__hint">
              No hay clientes registrados todavía. Crea uno en la sección de Clientes primero.
            </p>
          </div>
        </div>

        <div class="field-row">
          <div class="field">
            <label for="saldoInicial">Saldo inicial</label>
            <input
              id="saldoInicial"
              v-model="form.saldoInicial"
              type="number"
              min="0"
              step="0.01"
              placeholder="0"
              :aria-invalid="Boolean(errores.saldoInicial)"
            />
            <p v-if="errores.saldoInicial" class="field__error">{{ errores.saldoInicial }}</p>
          </div>
          <div class="field field--checkbox">
            <label class="checkbox">
              <input v-model="form.exentaGmf" type="checkbox" />
              Exenta de GMF
            </label>
          </div>
        </div>
      </template>

      <div class="slip-form__actions">
        <button v-if="esEdicion" type="button" class="btn btn--ghost" @click="alCancelar">Cancelar</button>
        <button type="submit" class="btn btn--brass" :disabled="guardando">
          {{ guardando ? 'Guardando…' : esEdicion ? 'Guardar cambios' : 'Registrar producto' }}
        </button>
      </div>
    </form>
  </section>
</template>
