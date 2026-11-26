"""
Genera el "Documento de requerimientos y arquitectura" de Acambapan (.docx).
La conversion a PDF la hace _generar_pdfs.py con Word.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.shared import Pt

from _docx_util import (CAFE, EQUIPO, GRIS, TINTA, caja_destacada, documento_base,
                        encabezado_y_pie, imagen, indice, parrafo, portada,
                        salto, tabla, vineta)

AQUI = os.path.dirname(os.path.abspath(__file__))
DIAG = os.path.join(AQUI, "diagramas")


def construir():
    doc = documento_base(
        "Documento de requerimientos y arquitectura",
        "Entrega final · Programación Móvil I",
    )
    encabezado_y_pie(doc, "Acambapan · Requerimientos y arquitectura")

    portada(
        doc,
        "Documento de requerimientos\ny arquitectura de la aplicación",
        "Sistema de ventas en línea para una panadería",
        "Programación Móvil I · Proyecto final · Entrega 3",
        EQUIPO,
        "Acámbaro, Guanajuato · Noviembre de 2026",
    )

    indice(doc)

    # ------------------------------------------------------------------
    doc.add_heading("1. Introducción", level=1)
    parrafo(doc,
            "Acambapan es una aplicación Android para la venta en línea de una "
            "panadería y repostería ficticia ubicada en Acambay. El cliente "
            "consulta el catálogo, arma un carrito, elige entrega a domicilio o "
            "para recoger, paga con el método que prefiera y sigue el estado de "
            "su pedido. La panadería administra pedidos, inventario de insumos y "
            "métricas del día desde el mismo instalable, protegido por rol.")
    parrafo(doc,
            "Este documento reúne los requerimientos del sistema y la "
            "arquitectura con la que se implementaron: capas, patrones, base de "
            "datos, contrato de la Web API y los diagramas UML que exige la "
            "entrega final (base de datos, paquetes, componentes y despliegue).")

    doc.add_heading("1.1 Alcance", level=2)
    for texto in [
        ("Cliente: ", "catálogo con búsqueda y filtro por categoría, ficha de producto, "
                      "carrito, confirmación de pedido, seguimiento, perfil, direcciones de "
                      "entrega, métodos de pago y bandeja de notificaciones."),
        ("Administrador: ", "panel, indicadores del día, gestión de pedidos con cambios de "
                            "estado y control de inventario de insumos con alta, edición y baja."),
        ("Operación sin conexión: ", "el catálogo, el inventario y el historial se leen de la "
                                     "copia local; los pedidos creados sin red quedan en cola y se "
                                     "envían solos al recuperar la conexión."),
        ("Fuera de alcance: ", "pasarela de pago real, aplicación del repartidor y notificaciones "
                               "push; se documentan como trabajo futuro."),
    ]:
        vineta(doc, texto[1], negrita_hasta=texto[0])

    doc.add_heading("1.2 Glosario", level=2)
    tabla(doc, ["Término", "Significado"], [
        ("Tanda", "Horneada programada de un producto, con hora de salida y piezas disponibles."),
        ("Folio", "Identificador legible del pedido (ej. PED-000123 o APP-loquesea)."),
        ("Insumo", "Materia prima del inventario: harina, huevo, mantequilla, etc."),
        ("Tanda / receta", "Relación producto–insumo con la cantidad requerida por pieza."),
        ("Offline-first", "La app funciona con la copia local y sincroniza cuando hay red."),
        ("ViewModel", "Clase que expone el estado de una pantalla y sobrevive a la rotación."),
        ("Repository", "Única puerta de entrada a los datos; decide local o remoto."),
        ("Sesión", "Token firmado que identifica al usuario y su rol por 12 horas."),
    ], anchos=[3.4, 12.2])

    salto(doc)

    # ------------------------------------------------------------------
    doc.add_heading("2. Requerimientos", level=1)

    doc.add_heading("2.1 Actores", level=2)
    tabla(doc, ["Actor", "Descripción", "Permisos principales"], [
        ("Visitante", "Persona que abre la app sin cuenta.",
         "Ver catálogo, armar carrito y pedir como invitado."),
        ("Cliente", "Usuario registrado con rol CLIENTE.",
         "Todo lo del visitante más direcciones, métodos de pago, "
         "historial y notificaciones."),
        ("Administrador", "Dueño o encargado de la panadería (rol ADMIN).",
         "Panel, indicadores, pedidos, cambios de estado e inventario."),
        ("Repartidor", "Personal de reparto (rol REPARTIDOR).",
         "Figura en el modelo y en el pedido; su app queda como trabajo futuro."),
    ], anchos=[2.8, 5.6, 7.2])

    doc.add_heading("2.2 Requerimientos funcionales", level=2)
    tabla(doc, ["ID", "Requerimiento", "Criterio de aceptación", "Estado"], [
        ("RF-01", "Registro de cliente",
         "Nombre, correo, teléfono y contraseña; correo duplicado devuelve 409.", "Cumplido"),
        ("RF-02", "Inicio de sesión",
         "Token válido 12 h; contraseña incorrecta devuelve 401.", "Cumplido"),
        ("RF-03", "Catálogo con búsqueda y filtros",
         "Lista desde Room; buscador y chips filtran sin red.", "Cumplido"),
        ("RF-04", "Ficha de producto",
         "Muestra precio, categoría, disponibilidad y multimedia.", "Cumplido"),
        ("RF-05", "Carrito",
         "Agregar, cambiar cantidad y quitar; totales recalculados.", "Cumplido"),
        ("RF-06", "Confirmación de pedido",
         "Tipo de entrega, dirección, empaque, rebanado y notas.", "Cumplido"),
        ("RF-07", "Pedido sin conexión",
         "Se guarda con folio APP-, queda en cola y se envía al volver la red.", "Cumplido"),
        ("RF-08", "Seguimiento del pedido",
         "Línea de tiempo con los cuatro estados y datos del repartidor.", "Cumplido"),
        ("RF-09", "Direcciones de entrega",
         "Alta, edición, baja y marca de predeterminada.", "Cumplido"),
        ("RF-10", "Métodos de pago",
         "Efectivo, tarjeta con terminal y transferencia SPEI.", "Cumplido"),
        ("RF-11", "Notificaciones",
         "Bandeja del cliente; marcar como leída contra la API.", "Cumplido"),
        ("RF-12", "Ayuda",
         "Preguntas frecuentes y datos de contacto.", "Cumplido"),
        ("RF-13", "Acceso administrativo",
         "Rutas de admin protegidas por rol con pantalla de candado.", "Cumplido"),
        ("RF-14", "Indicadores del día",
         "Pedidos, ventas, ticket promedio e insumos bajos desde /resumen.", "Cumplido"),
        ("RF-15", "Gestión de pedidos",
         "Filtro por estado y transiciones válidas; salto inválido devuelve 409.", "Cumplido"),
        ("RF-16", "Inventario",
         "Alta, edición y baja de insumos; aviso de stock bajo.", "Cumplido"),
        ("RF-17", "Cambio de idioma",
         "Interfaz completa en español e inglés según el idioma del sistema.", "Cumplido"),
        ("RF-18", "Diseño adaptable",
         "Experiencia distinta en pantallas compactas y grandes.", "Cumplido"),
    ], anchos=[1.5, 4.4, 8.0, 2.1], tam=8.6)

    doc.add_heading("2.3 Requerimientos no funcionales", level=2)
    tabla(doc, ["ID", "Requerimiento", "Cómo se cumple"], [
        ("RNF-01", "Arquitectura por capas",
         "Application → Activity → Compose → ViewModel → Repository → Room/Retrofit."),
        ("RNF-02", "Persistencia local",
         "Room 2.8 con 11 entidades, 9 DAOs, transacciones y migraciones."),
        ("RNF-03", "Consumo de Web API",
         "Retrofit 3 + Gson sobre HTTPS, con manejo uniforme de errores."),
        ("RNF-04", "Compatibilidad",
         "minSdk 29 (Android 10) y targetSdk 37; probado en Android 17."),
        ("RNF-05", "Seguridad",
         "Token HMAC-SHA256, rol revalidado por petición, secretos fuera del APK."),
        ("RNF-06", "Rendimiento",
         "Listas con LazyColumn y claves estables; consultas indexadas por estado."),
        ("RNF-07", "Mantenibilidad",
         "Nombres en español, un paquete por responsabilidad, sin lógica en la UI."),
        ("RNF-08", "Internacionalización",
         "Texto en recursos; ningún literal visible incrustado en Kotlin."),
        ("RNF-09", "Accesibilidad",
         "contentDescription en imágenes y tamaños táctiles de al menos 48 dp."),
    ], anchos=[1.7, 4.6, 9.7], tam=8.8)

    doc.add_heading("2.4 Casos de uso principales", level=2)
    for titulo, pasos in [
        ("CU-01 · Realizar un pedido",
         ["El cliente abre el catálogo; la app muestra Room y refresca desde la API.",
          "Filtra por categoría o busca por nombre.",
          "Abre la ficha, elige cantidad y agrega al carrito.",
          "En el carrito elige entrega, dirección, empaque y método de pago.",
          "Confirma: si hay red se crea en el servidor; si no, queda en cola local.",
          "La app abre el seguimiento con el folio del pedido."]),
        ("CU-02 · Administrar un pedido",
         ["El administrador entra al panel (se valida el rol ADMIN).",
          "Abre gestión de pedidos y filtra por estado.",
          "Cambia el estado; el servidor valida la transición.",
          "El cliente ve el nuevo estado al refrescar el seguimiento."]),
        ("CU-03 · Ajustar inventario",
         ["El administrador abre inventario con los insumos desde Room.",
          "Agrega, edita o elimina un insumo; la API valida permisos.",
          "Un insumo usado en una receta no se puede borrar (409)."]),
        ("CU-04 · Sincronizar sin conexión",
         ["El cliente hace un pedido sin red: se guarda con folio APP-.",
          "Al recuperar la conexión el repositorio empuja la cola.",
          "El folio es idempotente: reintentar no duplica el pedido."]),
    ]:
        doc.add_heading(titulo, level=3)
        for paso in pasos:
            vineta(doc, paso)

    salto(doc)

    # ------------------------------------------------------------------
    doc.add_heading("3. Arquitectura", level=1)
    parrafo(doc,
            "La aplicación sigue una arquitectura en capas con el patrón "
            "Repository como única puerta a los datos. Cada capa depende solo de "
            "la inmediata inferior, tal como exige la asignación: Application → "
            "Activity → Compose → ViewModel → Repository → Repository local / "
            "remoto.")

    doc.add_heading("3.1 Capas y responsabilidades", level=2)
    tabla(doc, ["Capa", "Paquete", "Responsabilidad"], [
        ("Application", "AcambapanApplication", "Crea el contenedor de dependencias una sola vez."),
        ("Activity", "MainActivity", "Única Activity; monta el tema y el NavHost."),
        ("UI", "ui.screens, ui.components", "Solo dibuja y emite eventos; no conoce datos."),
        ("Presentación", "ui.viewmodel", "Estado con StateFlow, sobrevive a la rotación."),
        ("Dominio", "data.modelo", "Modelos puros; no saben de Room ni de Retrofit."),
        ("Datos", "data.repository", "Decide local o remoto, aplica reglas y mapea."),
        ("Local", "data.local", "Room: entidades, DAOs y base."),
        ("Remoto", "data.remote", "Retrofit, DTOs, errores y ejecución de red."),
        ("Soporte", "data.sesion, data.conectividad, data.mapeadores, di",
         "Sesión, estado de red, traducciones y contenedor."),
        ("Navegación", "navigation", "Rutas y NavHost con guardas de rol."),
    ], anchos=[3.0, 4.2, 8.8], tam=8.8)

    doc.add_heading("3.2 Inyección de dependencias", level=2)
    parrafo(doc,
            "La inyección es manual y explícita, sin frameworks. "
            "AcambapanApplication construye un único ContenedorApp con la base de "
            "Room, el cliente Retrofit, el almacén de sesión y los siete "
            "repositorios. MainActivity lo toma de la Application y lo pasa al "
            "NavHost; cada pantalla crea su ViewModel con una fábrica que recibe "
            "ese contenedor.")
    caja_destacada(doc, "Por qué manual y no Hilt",
                   "Con siete repositorios y trece ViewModel, un contenedor "
                   "explícito se lee de un vistazo, no añade procesamiento de "
                   "anotaciones y deja visible la cadena completa de dependencias "
                   "que el profesor pide demostrar.")

    doc.add_heading("3.3 Patrón Repository y funcionamiento sin conexión", level=2)
    parrafo(doc,
            "Cada repositorio expone Flows de Room para lectura y funciones "
            "suspendidas para escritura. La lectura nunca espera a la red: la "
            "interfaz pinta lo que ya está guardado y, si hay conexión, lanza una "
            "sincronización en segundo plano. Las escrituras administrativas "
            "exigen red y devuelven Resultado.Fallo con un mensaje entendible "
            "cuando no la hay.")
    parrafo(doc,
            "Los pedidos son el caso más delicado. Si no hay red, PedidoRepository "
            "guarda la cabecera y los detalles en una sola transacción de Room con "
            "un folio propio que empieza con APP- y la bandera "
            "pendiente_sincronizar. Al recuperar la conexión se recorre la cola y "
            "se envía cada pedido; como el folio es idempotente en el servidor, un "
            "reintento no duplica nada.")

    doc.add_heading("3.4 Gestión de estado", level=2)
    parrafo(doc,
            "Cada ViewModel expone un data class Estado inmutable dentro de un "
            "MutableStateFlow privado, y la pantalla lo observa con "
            "collectAsStateWithLifecycle. Los eventos de la interfaz llaman a "
            "funciones del ViewModel, nunca a repositorios directamente. Esto da "
            "flujo unidireccional de datos y estados de carga, error y éxito "
            "explícitos.")

    doc.add_heading("3.5 Navegación", level=2)
    parrafo(doc,
            "Se usa Navigation Compose con un único NavHost. Las rutas viven en "
            "navigation/Rutas.kt y el grafo en NavegacionAcambapan.kt. El detalle "
            "recibe el id del producto como argumento de ruta, y los destinos de "
            "administrador se resuelven con una guarda: si la sesión no tiene rol "
            "ADMIN se muestra AccesoRestringidoScreen en lugar del panel.")
    imagen(doc, os.path.join(DIAG, "05_navegacion.png"), 15.5,
           "Figura 1. Grafo de navegación del NavHost.")

    doc.add_heading("3.6 Diseño para distintos tamaños de pantalla", level=2)
    parrafo(doc,
            "La app mide el ancho real de la ventana con BoxWithConstraints al "
            "inicio del árbol de composición y lo traduce a una clase de tamaño con "
            "los puntos de corte de Material 3: compacta por debajo de 600 dp, "
            "mediana entre 600 y 839 dp y expandida desde 840 dp. La clase se "
            "publica con un CompositionLocal, así que cualquier pantalla la lee sin "
            "recibir parámetros extra.")
    parrafo(doc,
            "Con esa clase, los marcos de pantalla cambian de forma: en teléfonos se "
            "mantiene la barra inferior; en tabletas y ventanas grandes la navegación "
            "pasa a un riel lateral, el catálogo se muestra en cuadrícula de dos o tres "
            "columnas y el contenido se limita a un ancho máximo para que no se "
            "estire. Además, las pantallas de formulario y de detalle usan una "
            "disposición de dos paneles en horizontal: el acceso y el registro ponen "
            "la marca a la izquierda y el formulario a la derecha; el detalle del "
            "producto deja la ficha a la izquierda y la compra a la derecha; el "
            "carrito separa las líneas del resumen de pago; y el perfil deja los "
            "datos a un lado y los ajustes al otro. Como se mide la ventana y no el "
            "dispositivo, la experiencia también responde a pantalla dividida y a "
            "ventanas flotantes.")
    tabla(doc, ["Clase de ventana", "Ancho típico", "Experiencia"], [
        ("Compacta", "menos de 600 dp", "Barra inferior, catálogo en una columna, pantallas apiladas."),
        ("Mediana", "600 a 839 dp", "Riel lateral, catálogo en dos columnas, dos paneles en formularios."),
        ("Expandida", "840 dp o más", "Riel lateral, catálogo en tres columnas, contenido centrado de 1080 dp."),
    ], anchos=[3.4, 4.0, 8.6], tam=8.8)

    doc.add_heading("3.7 Soporte de idiomas", level=2)
    parrafo(doc,
            "Todo el texto visible vive en recursos. El español es el idioma "
            "predeterminado (res/values/strings.xml) y el inglés se declara en "
            "res/values-en/strings.xml: 323 claves por idioma, agrupadas en siete "
            "archivos por área. Un conteo automático confirma que ambos idiomas "
            "declaran exactamente los mismos nombres.")
    parrafo(doc,
            "La localización no se queda en las pantallas: los repositorios y los "
            "ViewModel tampoco escriben texto en español. Los fallos de negocio "
            "viajan como recurso (Resultado.Fallo.recurso) y se resuelven con el "
            "Context al mostrarlos, de modo que un error de validación también "
            "cambia de idioma. El manifiesto apunta a res/xml/locales_config.xml, "
            "así que en Android 13 o superior el usuario puede elegir el idioma de "
            "Acambapan desde los ajustes del sistema.")

    salto(doc)

    # ------------------------------------------------------------------
    doc.add_heading("4. Base de datos", level=1)
    parrafo(doc,
            "La base vive en dos lugares con el mismo modelo: MySQL "
            "(pantradicion_db) en el servidor y SQLite mediante Room en el "
            "teléfono. El servidor es la fuente de verdad; Room es la copia que "
            "permite operar sin conexión.")

    doc.add_heading("4.1 Modelo entidad-relación", level=2)
    imagen(doc, os.path.join(DIAG, "01_modelo_er.png"), 16.5,
           "Figura 2. Modelo entidad-relación de pantradicion_db (11 tablas).")

    doc.add_heading("4.2 Tablas y propósito", level=2)
    tabla(doc, ["Tabla", "Propósito", "Llaves foráneas"], [
        ("categorias", "Agrupa productos para los chips del catálogo.", "—"),
        ("usuarios", "Clientes, administradores y repartidores con su rol.", "—"),
        ("productos", "Pan, pastel y bebidas con precio y disponibilidad.", "categoria_id → categorias"),
        ("producto_multimedia", "Imágenes y video de cada producto.", "producto_id → productos"),
        ("tandas_horneado", "Horneadas programadas y piezas disponibles.", "producto_id → productos"),
        ("pedidos", "Cabecera del pedido con totales y estado.", "cliente_id, repartidor_id → usuarios"),
        ("detalles_pedido", "Líneas del pedido; subtotal calculado por MySQL.", "pedido_folio → pedidos, producto_id → productos"),
        ("insumos", "Inventario de materia prima con stock y mínimo.", "—"),
        ("producto_insumos", "Receta: cuánto insumo lleva cada producto.", "producto_id, insumo_id"),
        ("notificaciones", "Avisos del cliente ligados o no a un pedido.", "usuario_id → usuarios, pedido_folio → pedidos"),
        ("direcciones", "Direcciones de entrega del cliente.", "usuario_id → usuarios"),
    ], anchos=[3.4, 6.6, 6.0], tam=8.6)

    doc.add_heading("4.3 Equivalencia MySQL ↔ Room", level=2)
    parrafo(doc,
            "Las once tablas tienen su entidad @Entity con @ColumnInfo en "
            "snake_case, de modo que el mapeo es directo y no hay sorpresas entre "
            "lo que devuelve la API y lo que se guarda en el teléfono. Los nombres "
            "de tabla y columna se declaran explícitamente en cada entidad.")
    tabla(doc, ["MySQL", "Room"], [
        ("CREATE TABLE pedidos", "@Entity(tableName = \"pedidos\") data class PedidoEntity"),
        ("ENUM estado", "String en la entidad + enum EstadoPedido en el dominio"),
        ("DECIMAL(10,2)", "Double en la entidad; formato con NumberFormat es-MX"),
        ("TIMESTAMP", "String ISO en la entidad; se muestra con fechaLegible()"),
        ("FOREIGN KEY con CASCADE", "@ForeignKey con onDelete CASCADE"),
        ("Índices por estado y cliente", "@Index en la entidad correspondiente"),
    ], anchos=[6.0, 10.0], tam=8.8)

    doc.add_heading("4.4 DAOs y transacciones", level=2)
    parrafo(doc,
            "Nueve DAOs cubren catálogo, pedidos, insumos, direcciones, "
            "notificaciones, usuarios y multimedia. Las consultas reactivas "
            "devuelven Flow; las de escritura son suspendidas. La creación de un "
            "pedido usa withTransaction para que la cabecera y sus líneas se "
            "guarden o fallen juntas.")

    salto(doc)

    # ------------------------------------------------------------------
    doc.add_heading("5. Web API", level=1)
    parrafo(doc,
            "La API está escrita en PHP 8 con PDO sobre MySQL y expone JSON en "
            "UTF-8 con un sobre uniforme: ok más el recurso, o ok=false y un "
            "objeto error con codigo y mensaje. Se publica por el túnel de "
            "Cloudflare en https://acambapan.devnoway.com/api/ y en local en el "
            "puerto 88 del XAMPP.")

    doc.add_heading("5.1 Endpoints", level=2)
    tabla(doc, ["Método y ruta", "Función", "Acceso"], [
        ("GET /ping", "Salud del servicio y de MySQL.", "Público"),
        ("POST /auth/login", "Devuelve token HMAC de 12 h y el usuario.", "Público"),
        ("POST /auth/registro", "Crea cliente y devuelve sesión.", "Público"),
        ("GET /auth/me", "Usuario dueño del token.", "Token"),
        ("GET /categorias", "Categorías activas.", "Público"),
        ("GET /productos", "Catálogo con filtros de categoría y búsqueda.", "Público"),
        ("GET /productos/{id}", "Ficha con multimedia y tandas.", "Público"),
        ("GET, POST, PUT, DELETE /pedidos", "Listar, crear, cambiar estado y cancelar.", "Público / ADMIN"),
        ("GET, POST, PUT, DELETE /insumos", "Inventario completo.", "Público / ADMIN"),
        ("GET /resumen", "Indicadores del día.", "ADMIN"),
        ("GET /notificaciones", "Avisos del cliente.", "Token"),
        ("PUT /notificaciones/{id}/leida", "Marca un aviso como leído.", "Token"),
        ("GET, POST, PUT, DELETE /direcciones", "Direcciones del cliente.", "Token"),
        ("PUT /auth/metodo-pago", "Método de pago preferido.", "Token"),
    ], anchos=[5.6, 7.6, 2.8], tam=8.4)

    doc.add_heading("5.2 Autenticación y seguridad", level=2)
    for texto in [
        "El token es HMAC-SHA256 firmado con una llave de 48 bytes que vive fuera "
        "del árbol público y responde 403 si se pide por HTTP.",
        "El rol se revalida contra MySQL en cada petición: un token de cliente no "
        "sirve para operaciones de administrador.",
        "Las contraseñas nuevas se guardan con password_hash() (bcrypt); las "
        "cuentas semilla se migran al primer inicio de sesión correcto.",
        "Los errores internos no se filtran al cliente: se escriben en "
        "logs/api.log y la respuesta solo lleva un código.",
    ]:
        vineta(doc, texto)

    doc.add_heading("5.3 Idempotencia y cola de pedidos", level=2)
    parrafo(doc,
            "POST /pedidos acepta un folio opcional. Si el folio ya existe, "
            "responde 200 con el pedido guardado en lugar de crear otro. Eso es lo "
            "que permite que la app reintente el envío de la cola sin duplicar "
            "pedidos ni cobrar dos veces.")

    salto(doc)

    # ------------------------------------------------------------------
    doc.add_heading("6. Diagramas UML", level=1)

    doc.add_heading("6.1 Diagrama de paquetes", level=2)
    imagen(doc, os.path.join(DIAG, "02_paquetes.png"), 15.0,
           "Figura 3. Paquetes y dependencias entre capas.")

    doc.add_heading("6.2 Diagrama de componentes", level=2)
    imagen(doc, os.path.join(DIAG, "03_componentes.png"), 15.5,
           "Figura 4. Componentes del cliente Android y del servidor.")

    doc.add_heading("6.3 Diagrama de despliegue", level=2)
    imagen(doc, os.path.join(DIAG, "04_despliegue.png"), 15.0,
           "Figura 5. Nodos de despliegue y protocolos.")

    salto(doc)

    # ------------------------------------------------------------------
    doc.add_heading("7. Trazabilidad con los criterios de aceptación", level=1)
    parrafo(doc,
            "La siguiente tabla conecta cada criterio de la asignación con el "
            "lugar del proyecto donde se cumple, para que la revisión sea "
            "verificable punto por punto.")
    tabla(doc, ["Criterio de la asignación", "Dónde se cumple"], [
        ("Documento de requerimientos y arquitectura con diagramas de base de datos y UML",
         "Este documento: secciones 2 a 6, figuras 1 a 5."),
        ("100% de las UI (Compose + ViewModel) con su funcionalidad",
         "15 pantallas en ui/screens y 13 ViewModel en ui/viewmodel; todas "
         "conectadas a datos reales."),
        ("Navegación concluida (NavHost)",
         "navigation/Rutas.kt y navigation/NavegacionAcambapan.kt; figura 1."),
        ("Repository local (SQLite-Room)",
         "data/local: 11 entidades, 9 DAOs, AcambapanDatabase con migraciones."),
        ("Repository Network (librerías HTTP)",
         "data/remote con Retrofit 3, Gson y OkHttp."),
        ("Arquitectura y manejo de dependencias entre componentes",
         "AcambapanApplication, MainActivity, di/ContenedorApp; sección 3.2."),
        ("Entidades, DAOs y base de datos",
         "sección 4 y figura 2."),
        ("Interacción con Web APIs (Retrofit)",
         "sección 5; los 22 endpoints del contrato en data/remote/ApiServicio.kt."),
        ("Soporte a diferentes tamaños de pantalla",
         "sección 3.6; WindowSizeClass en MainActivity y marcos adaptables."),
        ("Soporte para diferentes idiomas",
         "sección 3.7; res/values y res/values-en, más locales_config.xml."),
        ("Integración de código por integrante",
         "sección 8 y el historial de commits del repositorio."),
        ("Informe técnico final con resultados",
         "Documento aparte: Informe técnico final."),
    ], anchos=[7.2, 8.8], tam=8.6)

    doc.add_heading("8. Trabajo por integrante", level=1)
    parrafo(doc,
            "El repositorio guarda el historial de los tres integrantes. Cada "
            "persona aportó tres commits con su nombre y su correo, de modo que el "
            "avance es verificable en GitHub.")
    tabla(doc, ["Integrante", "Aporte principal", "Commits"], [
        (EQUIPO[0][0], EQUIPO[0][1], "01, 02, 03"),
        (EQUIPO[1][0], EQUIPO[1][1], "04, 05, 06"),
        (EQUIPO[2][0], EQUIPO[2][1], "07, 08, 09"),
    ], anchos=[4.2, 8.4, 3.4], tam=8.8)

    doc.add_heading("8.1 Secuencia de commits", level=2)
    tabla(doc, ["#", "Autor", "Contenido"], [
        ("01", "A", "Arquitectura base y capa de datos (Room, Retrofit, repositorios, DI, navegación)."),
        ("02", "A", "Internacionalización completa español/inglés y configuración regional."),
        ("03", "A", "Diseño adaptable por tamaño de pantalla."),
        ("04", "J", "Catálogo, detalle y carrito: búsqueda, validaciones y estados."),
        ("05", "J", "Direcciones, métodos de pago y notificaciones del cliente."),
        ("06", "J", "Imágenes de producto y seguimiento del pedido."),
        ("07", "P", "Acceso, registro y control de roles."),
        ("08", "P", "Panel, indicadores, pedidos e inventario del administrador."),
        ("09", "P", "Documentación, diagramas, informes y pruebas."),
    ], anchos=[1.2, 1.8, 13.0], tam=8.8)

    salto(doc)

    # ------------------------------------------------------------------
    doc.add_heading("9. Instalación y ejecución", level=1)
    doc.add_heading("9.1 Requisitos", level=2)
    for texto in [
        "Android Studio con JDK 17 y Android SDK 37.",
        "XAMPP con Apache en el puerto 88 y MySQL/MariaDB.",
        "Opcional: cliente de Cloudflare para publicar el túnel.",
    ]:
        vineta(doc, texto)

    doc.add_heading("9.2 Base de datos y API", level=2)
    parrafo(doc,
            "Importar el script servidor/database/panaderia.sql con mysql.exe y "
            "reiniciar Apache. Comprobar con curl http://127.0.0.1:88/api/ping; "
            "debe responder ok=true y db=ok.")
    doc.add_heading("9.3 Compilar e instalar la app", level=2)
    parrafo(doc,
            "Desde la raíz del proyecto: gradlew :app:assembleDebug. El APK queda "
            "en app/build/outputs/apk/debug/app-debug.apk. Para apuntar a otro "
            "servidor se pasa -PapiBaseUrl=http://10.0.2.2:88/api/ (emulador) o la "
            "IP de la PC en un teléfono físico.")
    doc.add_heading("9.4 Cuentas de prueba", level=2)
    tabla(doc, ["Rol", "Correo", "Contraseña"], [
        ("Administrador", "admin@pantradicion.com", "123456"),
        ("Cliente", "s23120227@alumnos.itsur.edu.mx", "123456"),
        ("Repartidor", "luis.reparto@pantradicion.com", "123456"),
    ], anchos=[3.6, 8.2, 4.0], tam=9)

    # ------------------------------------------------------------------
    doc.add_heading("10. Estructura del código", level=1)
    parrafo(doc, "Árbol resumido del módulo app (paquete com.devnoway.posproyect):")
    for linea in [
        "AcambapanApplication.kt      Application, crea el contenedor",
        "MainActivity.kt              Activity, calcula el tamaño de ventana y monta el NavHost",
        "di/ContenedorApp.kt          dependencias: Room, Retrofit, repositorios",
        "data/modelo/                 modelos de dominio",
        "data/local/entidades        11 entidades Room",
        "data/local/daos             9 DAOs",
        "data/local/AcambapanDatabase.kt  base Room y migraciones",
        "data/remote/                 ApiCliente, ApiServicio, DTOs, Resultado",
        "data/repository/             7 repositorios",
        "data/sesion/                 token y perfil en preferencias",
        "data/conectividad/           observador de red",
        "data/mapeadores/             DTO ↔ Entity ↔ Modelo",
        "navigation/                  rutas y NavHost con guardas",
        "ui/viewmodel/                13 ViewModel con StateFlow",
        "ui/screens/                  15 pantallas Compose",
        "ui/components/               piezas reutilizables y marcos adaptables",
        "ui/theme/                    color, tipografía y tema",
    ]:
        p = doc.add_paragraph()
        r = p.add_run(linea)
        r.font.name = "Consolas"
        r.font.size = Pt(8.6)
        p.paragraph_format.space_after = Pt(1)

    ruta = os.path.join(AQUI, "Documento de arquitectura - Acambapan.docx")
    doc.save(ruta)
    print("OK", ruta)
    return ruta


if __name__ == "__main__":
    construir()
