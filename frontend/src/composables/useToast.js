import { reactive } from 'vue'

let contador = 0
const toasts = reactive([])

function push(tipo, mensaje, detalles = []) {
  const id = ++contador
  toasts.push({ id, tipo, mensaje, detalles })
  window.setTimeout(() => descartar(id), 5500)
  return id
}

function descartar(id) {
  const index = toasts.findIndex((t) => t.id === id)
  if (index !== -1) toasts.splice(index, 1)
}

export function useToast() {
  return {
    toasts,
    exito: (mensaje) => push('exito', mensaje),
    error: (mensaje, detalles) => push('error', mensaje, detalles),
    info: (mensaje) => push('info', mensaje),
    descartar
  }
}
