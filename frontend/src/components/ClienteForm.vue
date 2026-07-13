<script setup>
import { reactive, watch, computed } from 'vue'
import { TIPOS_IDENTIFICACION, calcularEdad } from '../utils/formatters'

const props = defineProps({
  cliente: {
    type: Object,
    default: null
  },
  guardando: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['submit', 'cancelar'])

const vacio = () => ({
  tipoIdentificacion: 'CC',
  numeroIdentificacion: '',
  nombres: '',
  apellidos: '',
  correoElectronico: '',
  fechaNacimiento: ''
})

const form = reactive(vacio())
const errores = reactive({})

const esEdicion = computed(() => Boolean(props.cliente))

watch(
  () => props.cliente,
  (nuevo) => {
    Object.assign(form, nuevo ? { ...vacio(), ...nuevo } : vacio())
    limpiarErrores()
  },
  { immediate: true }
)

function limpiarErrores() {
  Object.keys(errores).forEach((k) => delete errores[k])
}

const patronCorreo = /^[\w.+-]+@[\w-]+\.[a-zA-Z]{2,}$/

function validar() {
  limpiarErrores()

  if (!form.tipoIdentificacion) errores.tipoIdentificacion = 'Selecciona un tipo de identificación'

  if (!form.numeroIdentificacion.trim()) {
    errores.numeroIdentificacion = 'El número de identificación es obligatorio'
  } else if (form.numeroIdentificacion.length > 30) {
    errores.numeroIdentificacion = 'Máximo 30 caracteres'
  }

  if (!form.nombres.trim()) {
    errores.nombres = 'Los nombres son obligatorios'
  } else if (form.nombres.trim().length < 2) {
    errores.nombres = 'Mínimo 2 caracteres'
  }

  if (!form.apellidos.trim()) {
    errores.apellidos = 'Los apellidos son obligatorios'
  } else if (form.apellidos.trim().length < 2) {
    errores.apellidos = 'Mínimo 2 caracteres'
  }

  if (!form.correoElectronico.trim()) {
    errores.correoElectronico = 'El correo electrónico es obligatorio'
  } else if (!patronCorreo.test(form.correoElectronico.trim())) {
    errores.correoElectronico = 'Formato esperado: xxxx@xxxxx.xxx'
  }

  if (!form.fechaNacimiento) {
    errores.fechaNacimiento = 'La fecha de nacimiento es obligatoria'
  } else {
    const hoy = new Date().toISOString().slice(0, 10)
    if (form.fechaNacimiento > hoy) {
      errores.fechaNacimiento = 'La fecha debe estar en el pasado'
    } else if (calcularEdad(form.fechaNacimiento) < 18) {
      errores.fechaNacimiento = 'El cliente debe ser mayor de edad (18 años o más)'
    }
  }

  return Object.keys(errores).length === 0
}

function alEnviar() {
  if (!validar()) return
  emit('submit', { ...form, numeroIdentificacion: form.numeroIdentificacion.trim() })
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
      {{ esEdicion ? `Editar registro ${cliente.id}` : 'Registrar cliente' }}
    </h2>

    <form class="slip-form" @submit.prevent="alEnviar" novalidate>
      <div class="field-row">
        <div class="field">
          <label for="nombres">Nombres</label>
          <input id="nombres" v-model="form.nombres" type="text" placeholder="María José" :aria-invalid="Boolean(errores.nombres)" />
          <p v-if="errores.nombres" class="field__error">{{ errores.nombres }}</p>
        </div>
        <div class="field">
          <label for="apellidos">Apellidos</label>
          <input id="apellidos" v-model="form.apellidos" type="text" placeholder="Restrepo Ortiz" :aria-invalid="Boolean(errores.apellidos)" />
          <p v-if="errores.apellidos" class="field__error">{{ errores.apellidos }}</p>
        </div>
      </div>

      <div class="field-row">
        <div class="field">
          <label for="tipoIdentificacion">Tipo de identificación</label>
          <select id="tipoIdentificacion" v-model="form.tipoIdentificacion" :aria-invalid="Boolean(errores.tipoIdentificacion)">
            <option v-for="t in TIPOS_IDENTIFICACION" :key="t.value" :value="t.value">{{ t.label }}</option>
          </select>
          <p v-if="errores.tipoIdentificacion" class="field__error">{{ errores.tipoIdentificacion }}</p>
        </div>
        <div class="field">
          <label for="numeroIdentificacion">Número de identificación</label>
          <input
            id="numeroIdentificacion"
            v-model="form.numeroIdentificacion"
            type="text"
            inputmode="numeric"
            placeholder="1023456789"
            :aria-invalid="Boolean(errores.numeroIdentificacion)"
          />
          <p v-if="errores.numeroIdentificacion" class="field__error">{{ errores.numeroIdentificacion }}</p>
        </div>
      </div>

      <div class="field-row">
        <div class="field">
          <label for="correoElectronico">Correo electrónico</label>
          <input
            id="correoElectronico"
            v-model="form.correoElectronico"
            type="email"
            placeholder="cliente@correo.com"
            :aria-invalid="Boolean(errores.correoElectronico)"
          />
          <p v-if="errores.correoElectronico" class="field__error">{{ errores.correoElectronico }}</p>
        </div>
        <div class="field">
          <label for="fechaNacimiento">Fecha de nacimiento</label>
          <input
            id="fechaNacimiento"
            v-model="form.fechaNacimiento"
            type="date"
            :aria-invalid="Boolean(errores.fechaNacimiento)"
          />
          <p v-if="errores.fechaNacimiento" class="field__error">{{ errores.fechaNacimiento }}</p>
        </div>
      </div>

      <div class="slip-form__actions">
        <button v-if="esEdicion" type="button" class="btn btn--ghost" @click="alCancelar">Cancelar</button>
        <button type="submit" class="btn btn--brass" :disabled="guardando">
          {{ guardando ? 'Guardando…' : esEdicion ? 'Guardar cambios' : 'Registrar cliente' }}
        </button>
      </div>
    </form>
  </section>
</template>
