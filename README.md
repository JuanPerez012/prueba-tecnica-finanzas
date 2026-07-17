# Sistema de Gestión de Clientes, Productos y Transacciones Financieras

Aplicación desarrollada para una entidad financiera que permite administrar clientes, gestionar productos financieros (cuentas de ahorro y corriente) asociados a dichos clientes, y registrar movimientos transaccionales (consignaciones, retiros y transferencias) sobre esos productos.

---

## 1. Generalidades y requerimientos funcionales

La aplicación permite a los trabajadores de la entidad financiera:

- **Gestionar clientes**: crear, actualizar y eliminar clientes, con validación de mayoría de edad y de datos básicos (correo, nombres, apellidos).
- **Gestionar productos financieros**: crear cuentas de ahorro o cuentas corrientes asociadas a un cliente existente, activarlas/inactivarlas y cancelarlas cuando corresponda.
- **Registrar transacciones**: realizar consignaciones, retiros y transferencias entre cuentas, actualizando automáticamente los saldos involucrados.

### Clientes

| Atributo | Descripción |
|---|---|
| ID | Identificador único |
| Tipo de identificación | Enum (`TipoIdentificacion`) |
| Número de identificación | Documento del cliente |
| Nombres / Apellidos | Mínimo 2 caracteres |
| Correo electrónico | Formato válido `xxxx@xxxxx.xxx` |
| Fecha de nacimiento | Usada para validar mayoría de edad |
| Fecha de creación / modificación | Calculadas automáticamente |

**Reglas de negocio:**
- No se puede crear ni mantener en el sistema un cliente menor de edad.
- No se puede eliminar un cliente que tenga productos financieros asociados.
- Las fechas de creación y modificación se calculan automáticamente.

### Productos (cuentas)

| Atributo | Descripción |
|---|---|
| ID | Identificador único |
| Tipo de cuenta | `AHORROS` o `CORRIENTE` |
| Número de cuenta | 10 dígitos, autogenerado, único |
| Estado | `ACTIVA`, `INACTIVA`, `CANCELADA` |
| Saldo | Actualizado tras cada transacción |
| Exenta GMF | Booleano |
| Fecha de creación / modificación | Automáticas |
| Cliente propietario | Relación con `Cliente` |

**Reglas de negocio:**
- El número de cuenta es único, autogenerado y de 10 dígitos, iniciando en `53` (ahorros) o `33` (corriente).
- Toda cuenta debe estar asociada a un cliente existente.
- Las cuentas de ahorro no pueden tener saldo negativo.
- Las cuentas de ahorro se crean en estado `ACTIVA` por defecto.
- Solo se pueden cancelar cuentas con saldo igual a `$0`.
- El saldo se actualiza automáticamente después de cada transacción exitosa.

### Transacciones

Tipos soportados: **consignación**, **retiro** y **transferencia entre cuentas**.

**Reglas de negocio:**
- El saldo se actualiza después de cada transacción.
- Las transferencias solo pueden hacerse entre cuentas existentes.
- Toda transferencia genera dos movimientos: un débito en la cuenta origen y un crédito en la cuenta destino.

---

## 2. Arquitectura utilizada

El backend implementa una **arquitectura en capas (layered / MVC)**, organizada así:

```
controller/   → Expone los endpoints REST, recibe y valida las peticiones HTTP
dto/          → Objetos de transferencia de datos (request/response) por cada dominio
service/      → Lógica de negocio y reglas de dominio
repository/   → Acceso a datos vía Spring Data JPA
entity/       → Entidades JPA mapeadas a las tablas de la base de datos
mapper/       → Conversión entre entidades y DTOs
enums/        → Tipos controlados del dominio (estado de cuenta, tipo de cuenta, etc.)
validation/   → Validaciones personalizadas (p. ej. mayoría de edad)
exception/    → Manejo centralizado de errores (`GlobalExceptionHandler`)
port/         → Puerto de consulta que desacopla el módulo de clientes del de productos
```

Adicionalmente, el módulo de clientes se comunica con el de productos a través de un **puerto de consulta** (`ProductoConsultaPort` / `ProductoConsultaPortAdapter`), evitando una dependencia directa entre servicios y aislando esa comunicación detrás de una interfaz — un acercamiento inspirado en el enfoque de puertos y adaptadores de la arquitectura hexagonal, aplicado puntualmente donde aporta valor (para eliminar el acoplamiento circular entre `Cliente` y `Producto`), sin llevar todo el proyecto a hexagonal completa.

El frontend sigue una estructura por responsabilidad típica de Vue 3:

```
views/        → Páginas de la aplicación (una por módulo funcional)
components/   → Componentes reutilizables (formularios, tablas, diálogos)
composables/  → Lógica de estado y llamadas a la API reutilizable (Composition API)
api/          → Cliente HTTP y funciones de acceso a cada recurso del backend
router/       → Configuración de rutas
utils/        → Utilidades (formateadores, etc.)
```

---

## 3. Patrones de diseño utilizados

| Patrón | Dónde se aplica | Propósito |
|---|---|---|
| **Strategy** | `ProcesadorTransaccion` con sus implementaciones `ConsignacionProcesador`, `RetiroProcesador`, `TransferenciaProcesador` | Encapsular el algoritmo específico de cada tipo de transacción, permitiendo agregar nuevos tipos sin modificar el servicio orquestador |
| **Template Method** | `AbstractProcesadorTransaccion` | Define el esqueleto común de procesamiento de una transacción, dejando que cada subclase implemente el paso específico |
| **DTO (Data Transfer Object)** | Paquete `dto/` (`ClienteCreateRequest`, `ProductoResponse`, etc.) | Desacoplar el modelo de persistencia (entidades) del contrato expuesto por la API |
| **Mapper** | `ClienteMapper`, `ProductoMapper`, `TransaccionMapper` | Centralizar y aislar la conversión entre entidades JPA y DTOs |
| **Repository** | Interfaces en `repository/` sobre Spring Data JPA | Abstraer el acceso a datos del resto de la aplicación |
| **Port / Adapter** | `ProductoConsultaPort` + `ProductoConsultaPortAdapter` | Desacoplar el servicio de clientes de la implementación concreta del servicio de productos |
| **Global Exception Handler (Chain of Responsibility / Interceptor)** | `GlobalExceptionHandler` con `@ControllerAdvice` | Centralizar el manejo y formato de errores de toda la API |
| **Validador personalizado (Constraint Validator)** | `@MayorEdad` + `MayorEdadValidator` | Extender Bean Validation con una regla de negocio propia, reutilizable vía anotación |
| **Composable (Composition API)** | `useClientes`, `useProductos`, `useTransacciones`, `useToast` en el frontend | Encapsular y reutilizar lógica de estado y llamadas a la API entre componentes Vue |

---

## 4. Stack tecnológico

### Backend

- **Lenguaje**: Java 21
- **Framework**: Spring Boot 3.5.4
- **Base de datos**: MySQL
- **Testing**: JUnit 5
- **Gestor de dependencias**: Maven

### Frontend

- **Framework**: Vue 3 (Composition API)
- **Enrutamiento**: Vue Router
- **Cliente HTTP**: `fetch` nativo, encapsulado en `api/httpClient.js`
- **Gestión de estado**: composables propios (sin librería externa tipo Pinia/Vuex)
- **Build tool**: Vite (estructura de proyecto estándar `src/`, `main.js`)

---

## 5. Pruebas unitarias

El backend cuenta con **79 pruebas JUnit** distribuidas en 9 clases, cubriendo:

- **Controllers**: `ClienteControllerTest`, `ProductoControllerTest`, `TransaccionControllerTest`
- **Services**: `ClienteServiceImplTest`, `ProductoServiceImplTest`, `TransaccionServiceImplTest`
- **Procesadores de transacción**: `ConsignacionProcesadorTest`, `RetiroProcesadorTest`, `TransferenciaProcesadorTest`

Para ejecutarlas con Docker (Carpeta backend):

```bash
docker compose -f docker-compose.test.yml up --build --abort-on-container-exit
```

## 6. Ejecución de módulos

**Base de datos (Carpeta backend):**

```bash
docker compose up -d
```

Esto levanta MySQL en el puerto `3306` con las credenciales definidas en `compose.yaml`.

**Backend (Carpeta backend):**

```bash
./mvnw spring-boot:run
```

La API queda disponible en `http://localhost:8080`.

**Frontend (Carpeta frontend):**

```bash
npm install
npm run dev
```

La aplicación queda disponible en la URL que indique Vite (por defecto `http://localhost:5173`).

---

## 7. Despliegue en la nube (Azure)

La aplicación está desplegada en Microsoft Azure, con cada módulo montado en su propio servicio:

| Módulo | Servicio de Azure |
|---|---|
| **Frontend** | Azure Static Web Apps |
| **Backend** | Azure App Service (JAR, sin Docker) |
| **Base de datos** | Azure Database for MySQL – Flexible Server |

**Aplicación en línea:** [https://lemon-flower-0feeb010f.7.azurestaticapps.net](https://lemon-flower-0feeb010f.7.azurestaticapps.net)

### Arquitectura de despliegue

```
Usuario
   │
   ▼
Azure Static Web Apps (frontend Vue 3 / Vite)
   │  HTTPS · REST (api)
   ▼
Azure App Service (backend Spring Boot, JAR ejecutable)
   │  JDBC · SSL requerido
   ▼
Azure Database for MySQL – Flexible Server
```