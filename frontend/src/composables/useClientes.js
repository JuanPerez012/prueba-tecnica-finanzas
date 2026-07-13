import { ref } from 'vue'
import { clienteApi, ApiError } from '../api/clienteApi'
import { useToast } from './useToast'

export function useClientes() {
  const clientes = ref([])
  const cargando = ref(false)
  const guardando = ref(false)
  const eliminandoId = ref(null)
  const toast = useToast()

  async function cargar() {
    cargando.value = true
    try {
      clientes.value = await clienteApi.listar()
    } catch (err) {
      reportarError('No se pudo cargar el listado de clientes', err)
    } finally {
      cargando.value = false
    }
  }

  async function crear(payload) {
    guardando.value = true
    try {
      const creado = await clienteApi.crear(payload)
      clientes.value = [creado, ...clientes.value]
      toast.exito(`Cliente ${creado.nombres} ${creado.apellidos} registrado`)
      return creado
    } catch (err) {
      reportarError('No se pudo registrar el cliente', err)
      throw err
    } finally {
      guardando.value = false
    }
  }

  async function actualizar(id, payload) {
    guardando.value = true
    try {
      const actualizado = await clienteApi.actualizar(id, payload)
      const idx = clientes.value.findIndex((c) => c.id === id)
      if (idx !== -1) clientes.value[idx] = actualizado
      toast.exito(`Cliente ${actualizado.nombres} ${actualizado.apellidos} actualizado`)
      return actualizado
    } catch (err) {
      reportarError('No se pudo actualizar el cliente', err)
      throw err
    } finally {
      guardando.value = false
    }
  }

  async function eliminar(cliente) {
    eliminandoId.value = cliente.id
    try {
      await clienteApi.eliminar(cliente.id)
      clientes.value = clientes.value.filter((c) => c.id !== cliente.id)
      toast.exito(`Cliente ${cliente.nombres} ${cliente.apellidos} eliminado`)
    } catch (err) {
      reportarError('No se pudo eliminar el cliente', err)
      throw err
    } finally {
      eliminandoId.value = null
    }
  }

  function reportarError(mensajeGenerico, err) {
    if (err instanceof ApiError) {
      toast.error(err.message || mensajeGenerico, err.detalles)
    } else {
      toast.error(mensajeGenerico)
    }
  }

  return { clientes, cargando, guardando, eliminandoId, cargar, crear, actualizar, eliminar }
}
