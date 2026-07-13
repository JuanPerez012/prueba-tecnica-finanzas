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
