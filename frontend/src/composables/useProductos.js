import { ref } from 'vue'
import { productoApi } from '../api/productoApi'
import { ApiError } from '../api/httpClient'
import { useToast } from './useToast'

export function useProductos() {
  const productos = ref([])
  const cargando = ref(false)
  const guardando = ref(false)
  const eliminandoId = ref(null)
  const toast = useToast()

  async function cargar() {
    cargando.value = true
    try {
      productos.value = await productoApi.listar()
    } catch (err) {
      reportarError('No se pudo cargar el listado de productos', err)
    } finally {
      cargando.value = false
    }
  }

  async function crear(payload) {
    guardando.value = true
    try {
      const creado = await productoApi.crear(payload)
      productos.value = [creado, ...productos.value]
      toast.exito(`Cuenta ${creado.numeroCuenta} registrada`)
      return creado
    } catch (err) {
      reportarError('No se pudo registrar el producto', err)
      throw err
    } finally {
      guardando.value = false
    }
  }

  async function actualizar(id, payload) {
    guardando.value = true
    try {
      const actualizado = await productoApi.actualizar(id, payload)
      const idx = productos.value.findIndex((p) => p.id === id)
      if (idx !== -1) productos.value[idx] = actualizado
      toast.exito(`Cuenta ${actualizado.numeroCuenta} actualizada`)
      return actualizado
    } catch (err) {
      reportarError('No se pudo actualizar el producto', err)
      throw err
    } finally {
      guardando.value = false
    }
  }

  async function eliminar(producto) {
    eliminandoId.value = producto.id
    try {
      await productoApi.eliminar(producto.id)
      productos.value = productos.value.filter((p) => p.id !== producto.id)
      toast.exito(`Cuenta ${producto.numeroCuenta} eliminada`)
    } catch (err) {
      reportarError('No se pudo eliminar el producto', err)
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

  return { productos, cargando, guardando, eliminandoId, cargar, crear, actualizar, eliminar }
}
