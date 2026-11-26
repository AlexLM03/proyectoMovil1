# Arquitectura de Acambapan

Documento de arquitectura y modelo de datos de la app Android **Acambapan**
(ventas en línea de una panadería), con su Web API y su base de datos.

- App: Kotlin + Jetpack Compose, `minSdk 29`, `targetSdk 37`.
- Persistencia local: **Room (SQLite)**.
- Servicio: **PHP 8 + MySQL/MariaDB** en XAMPP (`C:\xampp\virtualhost\panaderia`).
- Cliente HTTP: **Retrofit + OkHttp + Gson**.

---

## 1. Arquitectura en capas

La app sigue el flujo que pide la entrega:

```
Application → Activity → Compose → ViewModel → Repository → Room (local)
                                                          ↘ Retrofit (remoto)
```

```mermaid
flowchart TB
    APP["AcambapanApplication<br/>(crea ContenedorApp)"]
    ACT["MainActivity<br/>(toma el contenedor)"]
    UI["ui.screens + ui.components<br/>(Compose, sin lógica de datos)"]
    VM["ui.viewmodel<br/>(estado + casos de uso)"]
    REPO["data.repository<br/>Catalogo · Pedido · Insumo · Sesion · Carrito · Notificacion"]
    LOCAL["data.local<br/>Room: entidades, DAOs, AcambapanDatabase"]
    REMOTE["data.remote<br/>ApiServicio (Retrofit) + DTOs"]
    SES["data.sesion<br/>AlmacenSesion (token + perfil)"]
    NET["data.conectividad<br/>ObservadorConectividad"]
    MODEL["data.modelo<br/>modelos de dominio"]

    APP --> ACT --> UI --> VM --> REPO
    REPO --> LOCAL
    REPO --> REMOTE
    REPO --> SES
    REPO --> NET
    VM --> MODEL
    LOCAL --> MODEL
    REMOTE --> MODEL
```

Reglas que sostienen el diseño:

1. **La interfaz no conoce datos**: las pantallas reciben estado y devuelven
   eventos; nada de SQL ni URLs en Compose.
2. **El repositorio es la única puerta a los datos**: decide si responde Room o
   la API, y mantiene la copia local al día.
3. **Una sola fuente de verdad local**: la interfaz siempre observa `Flow` de
   Room, por eso funciona sin conexión y se actualiza sola al sincronizar.
4. **Inyección manual**: `ContenedorApp` arma base de datos, API y repositorios
   una sola vez; los ViewModel reciben sus dependencias por constructor con una
   `fabrica(...)`. Sin frameworks de DI, fácil de leer y de defender.

### Paquetes

```
com.devnoway.posproyect
├── AcambapanApplication.kt     Application: crea el contenedor
├── MainActivity.kt             Activity: monta el tema y el NavHost
├── di/ContenedorApp.kt         composición de dependencias
├── data/
│   ├── modelo/                 modelos de dominio (Producto, Pedido, ...)
│   ├── local/                   Room: entidades, DAOs y base
│   ├── remote/                  Retrofit, DTOs y manejo de errores de red
│   ├── repository/              catálogo, pedidos, insumos, sesión, carrito, avisos
│   ├── sesion/                  token y perfil persistentes
│   ├── conectividad/            estado de la red
│   └── mapeadores/              DTO ↔ Entidad ↔ Modelo
├── navigation/                  Rutas + NavHost + candado de administrador
└── ui/
    ├── viewmodel/               11 ViewModel con StateFlow
    ├── screens/                 11 pantallas
    ├── components/              piezas reutilizables
    └── theme/                   colores y tipografía de la marca
```

---

## 2. Diagrama de componentes

```mermaid
flowchart LR
    subgraph App Android
        UI[Pantallas Compose]
        VM[ViewModel + StateFlow]
        CAT[CatalogoRepository]
        PED[PedidoRepository]
        INS[InsumoRepository]
        SES[RepoSesión]
        CAR[CarritoRepository]
        ROOM[(Room / SQLite)]
        RET[Retrofit]
        TOK[AlmacenSesion]
    end

    subgraph Servidor XAMPP
        APACHE[Apache :88]
        PHP[API PHP]
        DB[(MySQL pantradicion_db)]
    end

    UI --> VM
    VM --> CAT & PED & INS & SES & CAR
    CAT --> ROOM & RET
    PED --> ROOM & RET
    INS --> ROOM & RET
    SES --> TOK & RET
    RET -->|HTTP JSON + Bearer| APACHE --> PHP --> DB
```

---

## 3. Diagrama de despliegue

```mermaid
flowchart TB
    subgraph Cloudflare
        T["Túnel ch1984<br/>acambapan.devnoway.com"]
    end

    subgraph Equipo de desarrollo (Windows)
        subgraph XAMPP
            A["Apache 2.4<br/>Listen 88 · ServerAlias acambapan.devnoway.com"]
            P["PHP 8.2<br/>panaderia/api/index.php"]
            M[("MariaDB 10.4<br/>pantradicion_db")]
        end
        A --> P --> M
        ADB["adb reverse (solo desarrollo local)"]
    end

    subgraph Emulador Android
        E["App Acambapan<br/>Room acambapan.db"]
    end

    subgraph Teléfono físico
        F["App Acambapan<br/>Room acambapan.db"]
    end

    T -->|http://localhost:88| A
    E -->|"https://acambapan.devnoway.com/api/"| T
    F -->|"https://acambapan.devnoway.com/api/"| T
    E -.->|"http://10.0.2.2:88/api/ (opcional)"| A
    E -.->|"http://127.0.0.1:88/api/ (opcional)"| ADB --> A
```

La app consume la API por **HTTPS a través del túnel**, así funciona igual en el
emulador, en un teléfono con datos móviles o en otra red. El origen sigue siendo
el XAMPP del equipo (`localhost:88`); el túnel termina el TLS.

La URL no está incrustada en las pantallas: se genera en
`BuildConfig.API_BASE_URL` y se puede cambiar al compilar:

```bat
gradlew :app:assembleDebug -PapiBaseUrl=http://10.0.2.2:88/api/
```

---

## 4. Modelo de datos

10 tablas: las 8 del script original más `notificaciones` y
`producto_multimedia` (modelo y DAOs pedidos para la entrega).

```mermaid
erDiagram
    CATEGORIAS ||--o{ PRODUCTOS : clasifica
    PRODUCTOS ||--o{ TANDAS_HORNEADO : programa
    PRODUCTOS ||--o{ DETALLES_PEDIDO : aparece
    PRODUCTOS ||--o{ PRODUCTO_MULTIMEDIA : muestra
    PRODUCTOS ||--o{ PRODUCTO_INSUMOS : usa
    INSUMOS ||--o{ PRODUCTO_INSUMOS : compone
    USUARIOS ||--o{ PEDIDOS : compra
    USUARIOS ||--o{ PEDIDOS : reparte
    PEDIDOS ||--o{ DETALLES_PEDIDO : contiene
    USUARIOS ||--o{ NOTIFICACIONES : recibe
    PEDIDOS ||--o{ NOTIFICACIONES : genera

    CATEGORIAS {
        int id PK
        varchar nombre
        varchar icono_emoji
        boolean activo
    }
    PRODUCTOS {
        int id PK
        int categoria_id FK
        varchar nombre
        text descripcion
        decimal precio
        varchar emoji
        boolean disponible
    }
    TANDAS_HORNEADO {
        int id PK
        int producto_id FK
        time hora_salida
        int piezas_estimadas
        int piezas_disponibles
        enum estado
    }
    USUARIOS {
        int id PK
        varchar nombre
        varchar correo
        varchar password_hash
        varchar telefono
        varchar direccion
        enum rol
        boolean activo
    }
    PEDIDOS {
        varchar folio PK
        int cliente_id FK
        varchar cliente_nombre
        varchar cliente_telefono
        enum tipo_entrega
        varchar direccion_entrega
        enum tipo_empaque
        decimal subtotal
        decimal costo_envio
        decimal total
        enum estado
        int repartidor_id FK
        timestamp fecha_pedido
    }
    DETALLES_PEDIDO {
        int id PK
        varchar pedido_folio FK
        int producto_id FK
        int cantidad
        decimal precio_unitario
    }
    INSUMOS {
        int id PK
        varchar nombre
        decimal stock
        decimal minimo
        varchar unidad
    }
    PRODUCTO_INSUMOS {
        int producto_id PK
        int insumo_id PK
        decimal cantidad_requerida
    }
    NOTIFICACIONES {
        int id PK
        int usuario_id FK
        varchar pedido_folio FK
        varchar titulo
        text mensaje
        boolean leida
    }
    PRODUCTO_MULTIMEDIA {
        int id PK
        int producto_id FK
        enum tipo
        varchar url
        int orden
    }
```

### Equivalencia Room ↔ MySQL

| Tabla MySQL | Entidad Room | DAO |
| --- | --- | --- |
| `categorias` | `CategoriaEntity` | `CatalogoDao` |
| `productos` | `ProductoEntity` | `CatalogoDao` |
| `tandas_horneado` | `TandaEntity` | `TandaDao` |
| `usuarios` | `UsuarioEntity` | `UsuarioDao` |
| `pedidos` | `PedidoEntity` | `PedidoDao` |
| `detalles_pedido` | `DetallePedidoEntity` | `PedidoDao` |
| `insumos` | `InsumoEntity` | `InsumoDao` |
| `producto_insumos` | `ProductoInsumoEntity` | `RecetaDao` |
| `notificaciones` | `NotificacionEntity` | `NotificacionDao` |
| `producto_multimedia` | `MultimediaEntity` | `MultimediaDao` |

Room guarda una columna extra en `pedidos`: `pendiente_sincronizar` (1 = se creó
sin conexión y todavía no llegó al servidor). No existe en MySQL porque es estado
del cliente.

---

## 5. Flujo online / offline (pedido)

```mermaid
sequenceDiagram
    actor C as Cliente
    participant UI as Carrito (Compose)
    participant VM as CarritoViewModel
    participant R as PedidoRepository
    participant DB as Room
    participant API as API PHP
    participant MY as MySQL

    C->>UI: Confirmar pedido
    UI->>VM: confirmarPedido()
    VM->>R: crear(NuevoPedido)
    R->>DB: withTransaction(pedido + detalles, pendiente=1)
    alt hay conexión
        R->>API: POST /pedidos (folio APP-...)
        API->>MY: INSERT pedido + detalles + notificación
        API-->>R: 200/201 con el pedido
        R->>DB: guardar pedido sincronizado (pendiente=0)
    else sin conexión
        R-->>VM: folio local
        Note over DB: queda en cola (pendiente=1)
    end
    VM-->>UI: folioCreado → navega a seguimiento
    C->>UI: Reintentar envío
    UI->>VM: refrescar()
    VM->>R: enviarPendiente(folio)
    R->>API: POST /pedidos (mismo folio = idempotente)
```

Otras operaciones:

| Operación | Sin conexión | Con conexión |
| --- | --- | --- |
| Ver catálogo, tandas, inventario, perfil | Room | Room + `GET` para refrescar |
| Crear pedido | Room (cola) | Room + `POST` |
| Cambiar estado de pedido (admin) | Aviso: requiere red | `PUT` + actualización local |
| Crear/editar/eliminar insumo | Aviso: requiere red | `POST/PUT/DELETE` + copia local |
| Iniciar sesión / registrarse | Aviso: requiere red | `POST /auth/...` + token guardado |

---

## 6. Navegación

```mermaid
flowchart LR
    LOGIN[login] -->|cliente| CAT[catalogo]
    LOGIN -->|invitado| CAT
    LOGIN -->|admin| PANEL[admin_panel]
    LOGIN --> REG[registro] --> CAT
    CAT --> DET["detalle_producto/{id}"] --> CAR[carrito] --> SEG[seguimiento]
    CAT --> PERFIL[perfil] --> SEG
    PERFIL -->|modo administrador| PANEL
    PANEL --> DASH[admin_dash] & PED[admin_pedidos] & INV[admin_inventario]
    PANEL -.->|sin rol ADMIN| CANDADO[acceso restringido] --> LOGIN_ADMIN[login_admin]
```

Las rutas de administrador están protegidas en el `NavHost`: si la sesión no
tiene rol `ADMIN`, se muestra la pantalla de candado en lugar del contenido.

---

## 7. Reglas de negocio implementadas

1. **Envío**: $30 a domicilio; gratis desde $250. Para recoger no hay envío.
2. **Folio**: el servidor genera `ACB-####`; la app genera `APP-xxxx` cuando crea
   el pedido sin conexión. El servidor acepta el folio del cliente y responde el
   mismo pedido si se repite (idempotencia, sin duplicados).
3. **Totales**: el servidor recalcula `subtotal` y `total`; no confía en el
   cliente. `detalles_pedido.subtotal` es columna generada en MySQL.
4. **Estados**: `RECIBIDO → EN_PREPARACION → EN_CAMINO → ENTREGADO`; cualquier
   estado no final puede pasar a `CANCELADO`. El servidor valida y responde 409.
5. **Inventario**: no se puede borrar un insumo usado en una receta.
6. **Avisos**: cada cambio de estado crea una fila en `notificaciones`.
7. **Sesión**: token HMAC de 12 h; el rol se revalida en cada petición.

---

## 8. Trazabilidad con la entrega

| Criterio de `peticiones de entrega nuevas.txt` | Dónde está |
| --- | --- |
| Documento de arquitectura y diagramas (BD, paquetes, componentes, despliegue) | este documento |
| 100 % de UI definida y ≥ 75 % funcional | 11 pantallas en `ui/screens`, todas conectadas a su ViewModel; catálogo, detalle, carrito, pedido, perfil, login, registro, panel, dash, pedidos e inventario funcionan contra datos reales |
| Navegación concluida (NavHost) | `navigation/NavegacionAcambapan.kt` + `Rutas.kt` |
| Repository local (SQLite-Room) y remoto (Retrofit) | `data/repository` + `data/local` + `data/remote` |
| Dependencias: application → activity → compose → viewmodel → repository → local/remoto | `AcambapanApplication`, `MainActivity`, `di/ContenedorApp` |
| Entidades, DAOs y base; transacciones | 10 entidades, 8 DAOs, `AcambapanDatabase`; transacciones con `withTransaction` al crear pedidos y guardar del servidor |
| ≥ 50 % de funcionalidades de BD (agregar, editar, ver, eliminar, listar) | catálogo (ver/listar), pedidos (crear/listar/ver/cambiar estado/cancelar), insumos (crear/editar/listar/eliminar), notificaciones (listar/marcar leída), multimedia (listar por producto) |
| Modelo y DAOs listos para notificaciones y multimedia | `NotificacionEntity` + `NotificacionDao`, `MultimediaEntity` + `MultimediaDao` |
| ≥ 50 % de peticiones a la Web API (agregar, editar, ver, eliminar, listar, recuperar) | `/auth`, `/categorias`, `/productos`, `/tandas`, `/pedidos` (POST/GET/PUT/DELETE), `/insumos` (CRUD), `/resumen`, `/notificaciones` |
| Modelos y configuración JSON | `data/remote/dto/Dtos.kt` + Gson en `ApiCliente` |

### Pendiente (siguiente entrega)

- Pantallas de direcciones, métodos de pago y bandeja de notificaciones.
- Mostrar la multimedia del producto (el modelo y los datos ya existen).
- Seguimiento en tiempo real (push/WebSocket); hoy se refresca al abrir o con ⟳.
- Pasarela de pago y app del repartidor.
- Migraciones de Room (`version = 1`, `fallbackToDestructiveMigration` no usado).
- Tests instrumentados de DAOs y de la API.

---

## 9. Cómo compilar

```bat
rem APK de depuración (la URL por defecto es el túnel)
gradlew :app:assembleDebug

rem Contra el XAMPP local (desarrollo sin internet)
gradlew :app:assembleDebug -PapiBaseUrl=http://10.0.2.2:88/api/
```

Nota de toolchain: el proyecto usa **AGP 9 con Kotlin integrado**, y KSP registra
sus fuentes generadas con `kotlin.sourceSets`. Por eso `gradle.properties`
incluye `android.disallowKotlinSourceSets=false`; sin esa bandera la
configuración falla antes de compilar. Requiere JDK 17 o superior (funciona con
el JBR de Android Studio).
