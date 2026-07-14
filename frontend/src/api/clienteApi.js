import { crearRecursoApi } from './httpClient'

export { ApiError } from './httpClient'

const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1/clientes'

export const clienteApi = crearRecursoApi(BASE_URL)
