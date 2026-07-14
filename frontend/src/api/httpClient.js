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

export async function peticion(url, options = {}) {
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

export function crearRecursoApi(baseUrl) {
  return {
    listar() {
      return peticion(baseUrl)
    },
    obtener(id) {
      return peticion(`${baseUrl}/${id}`)
    },
    crear(payload) {
      return peticion(baseUrl, { method: 'POST', body: JSON.stringify(payload) })
    },
    actualizar(id, payload) {
      return peticion(`${baseUrl}/${id}`, { method: 'PUT', body: JSON.stringify(payload) })
    },
    eliminar(id) {
      return peticion(`${baseUrl}/${id}`, { method: 'DELETE' })
    }
  }
}
