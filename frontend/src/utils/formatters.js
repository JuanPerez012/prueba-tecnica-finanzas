export const TIPOS_IDENTIFICACION = [
  { value: 'CC', label: 'Cédula de ciudadanía' },
  { value: 'CE', label: 'Cédula de extranjería' },
  { value: 'TI', label: 'Tarjeta de identidad' },
  { value: 'PA', label: 'Pasaporte' },
  { value: 'NIT', label: 'NIT' }
]

export function etiquetaTipoIdentificacion(codigo) {
  return TIPOS_IDENTIFICACION.find((t) => t.value === codigo)?.label ?? codigo
}

export function calcularEdad(fechaNacimientoISO) {
  if (!fechaNacimientoISO) return null
  const nacimiento = new Date(fechaNacimientoISO)
  const hoy = new Date()
  let edad = hoy.getFullYear() - nacimiento.getFullYear()
  const aunNoCumple =
    hoy.getMonth() < nacimiento.getMonth() ||
    (hoy.getMonth() === nacimiento.getMonth() && hoy.getDate() < nacimiento.getDate())
  if (aunNoCumple) edad -= 1
  return edad
}

export function formatFecha(fechaISO) {
  if (!fechaISO) return '—'
  const fecha = new Date(fechaISO)
  return fecha.toLocaleDateString('es-CO', { year: 'numeric', month: '2-digit', day: '2-digit' })
}

export function formatFechaHora(fechaISO) {
  if (!fechaISO) return '—'
  const fecha = new Date(fechaISO)
  return fecha.toLocaleString('es-CO', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

export function idConsecutivo(id) {
  return `#${String(id).padStart(5, '0')}`
}

export const TIPOS_CUENTA = [
  { value: 'AHORROS', label: 'Cuenta de Ahorros' },
  { value: 'CORRIENTE', label: 'Cuenta Corriente' }
]

export function etiquetaTipoCuenta(codigo) {
  return TIPOS_CUENTA.find((t) => t.value === codigo)?.label ?? codigo
}

export const ESTADOS_CUENTA = [
  { value: 'ACTIVA', label: 'Activa' },
  { value: 'INACTIVA', label: 'Inactiva' },
  { value: 'CANCELADA', label: 'Cancelada' }
]

export function etiquetaEstadoCuenta(codigo) {
  return ESTADOS_CUENTA.find((e) => e.value === codigo)?.label ?? codigo
}

export function formatMoneda(valor) {
  if (valor === null || valor === undefined) return '—'
  return new Intl.NumberFormat('es-CO', {
    style: 'currency',
    currency: 'COP',
    maximumFractionDigits: 0
  }).format(valor)
}
