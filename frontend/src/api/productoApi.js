import { crearRecursoApi, peticion } from './httpClient'

const BASE_URL = import.meta.env.VITE_API_BASE_URL_PRODUCTOS || 'http://localhost:8080/api/v1/productos'

export const productoApi = {
  ...crearRecursoApi(BASE_URL),
  listarPorCliente(clienteId) {
    return peticion(`${BASE_URL}/cliente/${clienteId}`)
  }
}
