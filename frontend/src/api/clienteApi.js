const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1/clientes'

/**
 * Error de API con la forma que devuelve GlobalExceptionHandler del backend:
 * { timestamp, status, error, message, path, detalles }
 */
export class ApiError extends Error {
  constructor(message, { status, detalles } = {}) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.detalles = detalles ?? []
  }
}

async function manejarRespuesta(response) {
  if (response.status === 204) return null

  const texto = await response.text()
  const cuerpo = texto ? JSON.parse(texto) : null

  if (!response.ok) {
    const mensaje = cuerpo?.message || `Error inesperado (HTTP ${response.status})`
    throw new ApiError(mensaje, { status: response.status, detalles: cuerpo?.detalles })
  }

  return cuerpo
}

async function peticion(url, options = {}) {
  let response
  try {
    response = await fetch(url, {
      headers: { 'Content-Type': 'application/json' },
      ...options
    })
  } catch (redError) {
    throw new ApiError(
      'No fue posible conectar con el servidor. Verifica que el backend esté corriendo.',
      { status: 0 }
    )
  }
  return manejarRespuesta(response)
}

export const clienteApi = {
  listar() {
    return peticion(BASE_URL)
  },
  obtener(id) {
    return peticion(`${BASE_URL}/${id}`)
  },
  crear(payload) {
    return peticion(BASE_URL, { method: 'POST', body: JSON.stringify(payload) })
  },
  actualizar(id, payload) {
    return peticion(`${BASE_URL}/${id}`, { method: 'PUT', body: JSON.stringify(payload) })
  },
  eliminar(id) {
    return peticion(`${BASE_URL}/${id}`, { method: 'DELETE' })
  }
}
