import { crearRecursoApi, peticion } from './httpClient'

const BASE_URL = import.meta.env.VITE_API_BASE_URL_TRANSACCIONES || 'http://localhost:8080/api/v1/transacciones'

const recurso = crearRecursoApi(BASE_URL)

export const transaccionApi = {
  listar: recurso.listar,
  obtener: recurso.obtener,
  crear: recurso.crear,
  listarPorProducto(productoId) {
    return peticion(`${BASE_URL}/producto/${productoId}`)
  }
}
