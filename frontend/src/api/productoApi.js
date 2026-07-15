import { crearRecursoApi, peticion } from './httpClient'

const BASE_URL = import.meta.env.VITE_API_BASE_URL_PRODUCTOS || 'http://localhost:8080/api/v1/productos'

const recurso = crearRecursoApi(BASE_URL)

export const productoApi = {
  listar: recurso.listar,
  obtener: recurso.obtener,
  crear: recurso.crear,
  actualizar: recurso.actualizar,
  listarPorCliente(clienteId) {
    return peticion(`${BASE_URL}/cliente/${clienteId}`)
  }
}
