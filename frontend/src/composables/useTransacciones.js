import { ref } from 'vue'
import { transaccionApi } from '../api/transaccionApi'
import { ApiError } from '../api/httpClient'
import { etiquetaTipoTransaccion } from '../utils/formatters'
import { useToast } from './useToast'

export function useTransacciones() {
  const transacciones = ref([])
  const cargando = ref(false)
  const guardando = ref(false)
  const toast = useToast()

  async function cargar() {
    cargando.value = true
    try {
      transacciones.value = await transaccionApi.listar()
    } catch (err) {
      reportarError('No se pudo cargar el listado de transacciones', err)
    } finally {
      cargando.value = false
    }
  }

  async function crear(payload) {
    guardando.value = true
    try {
      const creada = await transaccionApi.crear(payload)
      transacciones.value = [creada, ...transacciones.value]
      toast.exito(`${etiquetaTipoTransaccion(creada.tipoTransaccion)} registrada por ${formatearMontoToast(creada.monto)}`)
      return creada
    } catch (err) {
      reportarError('No se pudo registrar la transacción', err)
      throw err
    } finally {
      guardando.value = false
    }
  }

  function formatearMontoToast(monto) {
    return new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 }).format(
      monto
    )
  }

  function reportarError(mensajeGenerico, err) {
    if (err instanceof ApiError) {
      toast.error(err.message || mensajeGenerico, err.detalles)
    } else {
      toast.error(mensajeGenerico)
    }
  }

  return { transacciones, cargando, guardando, cargar, crear }
}
