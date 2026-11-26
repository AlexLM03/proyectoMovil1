"""
Genera tres guias individuales (una por integrante) explicando la parte que
cada quien tiene que saber defender: Compose, ViewModel, Repository, Room,
Retrofit, i18n y responsividad, con el codigo real de sus archivos.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.oxml import OxmlElement
from docx.oxml.ns import qn
from docx.shared import Pt

from _docx_util import (CAFE, GRIS, TINTA, caja_destacada, documento_base,
                        encabezado_y_pie, indice, parrafo, portada, salto,
                        tabla, vineta)

AQUI = os.path.dirname(os.path.abspath(__file__))


def codigo(doc, texto, titulo=None):
    if titulo:
        p = doc.add_paragraph()
        r = p.add_run(titulo)
        r.font.size = Pt(8.5)
        r.bold = True
        r.font.color.rgb = CAFE
        p.paragraph_format.space_after = Pt(2)

    t = doc.add_table(rows=1, cols=1)
    t.style = "Table Grid"
    celda = t.rows[0].cells[0]
    shd = OxmlElement("w:shd")
    shd.set(qn("w:val"), "clear")
    shd.set(qn("w:color"), "auto")
    shd.set(qn("w:fill"), "F7F4EF")
    celda._tc.get_or_add_tcPr().append(shd)
    celda.text = ""
    primero = True
    for linea in texto.splitlines():
        p = celda.paragraphs[0] if primero else celda.add_paragraph()
        primero = False
        r = p.add_run(linea if linea else " ")
        r.font.name = "Consolas"
        r.font.size = Pt(8)
        r.font.color.rgb = TINTA
        p.paragraph_format.space_after = Pt(0)
    doc.add_paragraph().paragraph_format.space_after = Pt(4)


def construir(persona):
    doc = documento_base("Guía individual", persona["rol"])
    encabezado_y_pie(doc, f"Acambapan · Guía individual · {persona['nombre']}")

    portada(
        doc,
        "Guía individual\nde estudio",
        f"Lo que {persona['nombre_corto']} tiene que saber explicar",
        "Programación Móvil I · Proyecto final · Entrega 3",
        [(persona["nombre"], persona["rol"])],
        "Acámbaro, Guanajuato · Noviembre de 2026",
    )

    indice(doc)

    # ---------------------------------------------------------------
    doc.add_heading("1. Tu parte en el proyecto", level=1)
    parrafo(doc, persona["intro"])
    tabla(doc, ["Commit", "Qué aportaste"], persona["commits"],
          anchos=[1.8, 14.2], tam=9)

    caja_destacada(doc, "Lo que sí es tuyo",
                   persona["caja"])

    # ---------------------------------------------------------------
    doc.add_heading("2. La arquitectura en 60 segundos", level=1)
    parrafo(doc,
            "Si te preguntan cómo está armada la app, esta es la cadena. Sirve "
            "para las tres personas, aunque cada quien domine una parte:")
    codigo(doc,
           "Application  →  Activity  →  Compose  →  ViewModel  →  Repository\n"
           "                                                          ↓\n"
           "                                              Room (local)  |  Retrofit (API)",
           "Cadena de dependencias")
    tabla(doc, ["Capa", "Archivo", "Qué hace"], [
        ("Application", "AcambapanApplication.kt", "Crea el contenedor de dependencias una sola vez"),
        ("Activity", "MainActivity.kt", "Mide la ventana y monta el NavHost"),
        ("UI", "ui/screens, ui/components", "Dibuja y avisa; no guarda datos"),
        ("Presentación", "ui/viewmodel", "Estado con StateFlow y reglas de pantalla"),
        ("Datos", "data/repository", "Decide local o remoto y mapea"),
        ("Local", "data/local", "Room: entidades, DAOs y base"),
        ("Remoto", "data/remote", "Retrofit, DTOs y manejo de errores"),
    ], anchos=[3.2, 5.0, 7.8], tam=8.8)
    parrafo(doc,
            "Regla de oro para defender: la pantalla nunca habla con Room ni con "
            "Retrofit. Solo conoce su ViewModel.")

    salto(doc)

    # ---------------------------------------------------------------
    doc.add_heading("3. Los conceptos que te tocan", level=1)
    for bloque in persona["conceptos"]:
        doc.add_heading(bloque["titulo"], level=2)
        for texto in bloque["texto"]:
            parrafo(doc, texto)
        if bloque.get("codigo"):
            codigo(doc, bloque["codigo"], bloque.get("pie"))
        for punto in bloque.get("vinetas", []):
            vineta(doc, punto)

    salto(doc)

    # ---------------------------------------------------------------
    doc.add_heading("4. Tu función, paso a paso", level=1)
    parrafo(doc, persona["recorrido_intro"])
    for i, paso in enumerate(persona["recorrido"], start=1):
        parrafo(doc, f"{i}. {paso}", espacio_despues=3)

    # ---------------------------------------------------------------
    doc.add_heading("5. Archivos que debes poder abrir y explicar", level=1)
    tabla(doc, ["Archivo", "Qué enseñas al abrirlo"], persona["archivos"],
          anchos=[6.4, 9.6], tam=8.6)

    # ---------------------------------------------------------------
    doc.add_heading("6. Preguntas probables", level=1)
    tabla(doc, ["Pregunta", "Tu respuesta"], persona["preguntas"],
          anchos=[5.6, 10.4], tam=8.6)

    # ---------------------------------------------------------------
    doc.add_heading("7. Dónde termina tu parte", level=1)
    parrafo(doc,
            "No hace falta saber todo. Si te preguntan algo de otra área, lo "
            "correcto es decir de quién es y cómo se conecta:")
    for texto in persona["limites"]:
        vineta(doc, texto)

    # ---------------------------------------------------------------
    doc.add_heading("8. Cómo comprobarlo tú mismo", level=1)
    for paso in [
        "Compilar: gradlew :app:assembleDebug (debe decir BUILD SUCCESSFUL).",
        "Abrir el archivo de tu tabla y leer la función que se menciona.",
        "Ejecutar la app y reproducir tu recorrido de la sección 4.",
        "Si algo no cuadra con esta guía, gana el código: lo que está en el "
        "repositorio es la verdad.",
    ]:
        vineta(doc, paso)

    ruta = os.path.join(AQUI, persona["archivo"])
    doc.save(ruta)
    print("OK", ruta)


# ===========================================================================
# Alexander
# ===========================================================================
ALEXANDER = {
    "nombre": "Alexander López Mora",
    "nombre_corto": "Alexander",
    "rol": "Arquitectura, capas de datos, idiomas y diseño adaptable",
    "archivo": "Guia individual - Alexander Lopez Mora.docx",
    "intro": (
        "Tu parte es la base sobre la que trabajan las otras dos: la arquitectura "
        "por capas, la base local con Room, el consumo de la Web API con Retrofit, "
        "los repositorios, la navegación y los dos criterios transversales, idiomas "
        "y tamaños de pantalla. Es la parte que más preguntas recibe porque de ahí "
        "cuelga todo lo demás."
    ),
    "commits": [
        ("01", "Arquitectura base: Application, Activity, contenedor de dependencias, "
               "Room (11 entidades, 9 DAOs, migraciones), Retrofit, 7 repositorios, "
               "13 ViewModel y NavHost con 15 pantallas"),
        ("02", "Internacionalización completa: 323 textos en español e inglés, "
               "enum traducidos y mensajes de error por recurso"),
        ("03", "Diseño adaptable: clases de tamaño, riel lateral, dos paneles y "
               "cuadrícula de catálogo"),
    ],
    "caja": (
        "Tú respondes por: por qué hay una capa de repositorios, cómo se guarda y "
        "se lee de Room, cómo se habla con la API, cómo se resuelve el idioma y "
        "cómo cambia la pantalla según el ancho. Todo lo que sea 'por qué está "
        "así' cae de tu lado."
    ),
    "conceptos": [
        {
            "titulo": "Inyección de dependencias manual",
            "texto": [
                "No usamos Hilt ni Koin. AcambapanApplication construye un único "
                "ContenedorApp y lo entrega a la Activity; de ahí baja a cada "
                "ViewModel. La ventaja es que la cadena completa se lee de un "
                "vistazo y no hay magia que explicar.",
            ],
            "codigo": (
                "class ContenedorApp(contexto: Context) {\n"
                "    val contexto: Context = contexto.applicationContext\n"
                "    private val baseDeDatos = AcambapanDatabase.obtener(contexto)\n"
                "    val almacenSesion = AlmacenSesion(contexto)\n"
                "    private val api: ApiServicio = ApiCliente.crear(almacenSesion)\n"
                "\n"
                "    val pedidoRepository = PedidoRepository(\n"
                "        baseDeDatos = baseDeDatos,\n"
                "        pedidoDao = baseDeDatos.pedidoDao(),\n"
                "        api = api,\n"
                "        conectividad = conectividad\n"
                "    )\n"
                "}"
            ),
            "pie": "di/ContenedorApp.kt (resumen)",
        },
        {
            "titulo": "Room: entidades, DAOs y base",
            "texto": [
                "Room es la copia de MySQL dentro del teléfono. Hay 11 entidades "
                "(@Entity) que replican las tablas, 9 DAOs (@Dao) con las "
                "consultas, y una base con versión y migración para no borrar "
                "datos del usuario al actualizar.",
                "Los nombres de columna se declaran explícitamente, así lo que "
                "devuelve la API y lo que se guarda coinciden sin sorpresas.",
            ],
            "codigo": (
                "@Entity(tableName = \"pedidos\")\n"
                "data class PedidoEntity(\n"
                "    @PrimaryKey val folio: String,\n"
                "    @ColumnInfo(name = \"cliente_id\") val clienteId: Int?,\n"
                "    @ColumnInfo(name = \"metodo_pago\") val metodoPago: String = \"EFECTIVO\",\n"
                "    @ColumnInfo(name = \"pendiente_sincronizar\")\n"
                "    val pendienteSincronizar: Boolean = false\n"
                ")\n"
                "\n"
                "@Dao\n"
                "interface PedidoDao {\n"
                "    @Query(\"SELECT * FROM pedidos WHERE pendiente_sincronizar = 1\")\n"
                "    suspend fun pendientesDeSincronizar(): List<PedidoEntity>\n"
                "}"
            ),
            "pie": "data/local (resumen representativo)",
            "vinetas": [
                "Lecturas con Flow: la pantalla se entera sola cuando cambia la tabla.",
                "Escrituras suspend y dentro de withTransaction cuando tocan dos tablas.",
                "Las migraciones se declaran en AcambapanDatabase, no se borra la base.",
            ],
        },
        {
            "titulo": "Retrofit y el manejo de errores",
            "texto": [
                "La API se declara como una interfaz con anotaciones. OkHttp pone "
                "el token en cada petición y registra la llamada; Gson convierte "
                "el JSON en DTOs.",
                "Lo importante: ninguna excepción de red llega a la pantalla. "
                "ejecutarRed las convierte en un Resultado, que puede ser Éxito o "
                "Fallo. Y el Fallo puede llevar un recurso de texto para que el "
                "error se muestre en el idioma del usuario.",
            ],
            "codigo": (
                "@GET(\"productos\")\n"
                "suspend fun productos(\n"
                "    @Query(\"categoriaId\") categoriaId: Int? = null,\n"
                "    @Query(\"buscar\") buscar: String? = null\n"
                "): ProductosRespuestaDto\n"
                "\n"
                "// En el repositorio:\n"
                "return ejecutarRed {\n"
                "    val lista = api.productos().productos.orEmpty()\n"
                "    catalogoDao.guardarProductos(lista.map { it.aEntidad() })\n"
                "    lista.size\n"
                "}"
            ),
            "pie": "data/remote/ApiServicio.kt y CatalogoRepository.kt (resumen)",
        },
        {
            "titulo": "Patrón Repository y funcionamiento sin conexión",
            "texto": [
                "El repositorio es la única puerta a los datos: decide si lee de "
                "Room o de la API y traduce DTO → entidad → modelo. La pantalla "
                "nunca sabe de dónde salió el dato.",
                "El caso estrella es el pedido sin red: se guarda con folio propio "
                "(APP-…) y la bandera pendiente_sincronizar. Al volver la conexión "
                "se empuja la cola, y como el folio es idempotente en el servidor, "
                "reintentar no duplica el pedido.",
            ],
            "codigo": (
                "suspend fun crear(nuevo: NuevoPedido): Resultado<String> {\n"
                "    val folio = \"APP-\" + System.currentTimeMillis().toString(36).uppercase()\n"
                "    baseDeDatos.withTransaction {\n"
                "        pedidoDao.guardarPedido(entidad)\n"
                "        pedidoDao.guardarDetalles(detalles)\n"
                "    }\n"
                "    if (conectividad.estaEnLinea()) empujarPedido(folio)\n"
                "    return Resultado.Exito(folio)\n"
                "}"
            ),
            "pie": "data/repository/PedidoRepository.kt (resumen)",
        },
        {
            "titulo": "Idiomas: el texto no vive en el código",
            "texto": [
                "Todo el texto visible está en res/values (español) y "
                "res/values-en (inglés), con las mismas claves. La app elige la "
                "carpeta según el idioma del sistema, y en Android 13 o superior "
                "el usuario puede cambiarlo solo para esta app.",
                "Los enums del dominio conservan su código (RECIBIDO, EFECTIVO) y "
                "una extensión dice qué recurso mostrar. Los mensajes de error "
                "viajan como recurso y se resuelven con texto(contexto).",
            ],
            "vinetas": [
                "323 claves por idioma, repartidas en siete archivos con prefijos.",
                "stringResource(R.string.clave) en Compose, con argumentos si lleva valores.",
                "locales_config.xml + android:localeConfig para el selector por app.",
            ],
        },
        {
            "titulo": "Responsividad: clases de tamaño",
            "texto": [
                "No se pregunta si es tableta, se mide el ancho disponible. Con "
                "BoxWithConstraints se calcula la clase (compacta <600 dp, mediana "
                "600–839, expandida ≥840) y se publica con un CompositionLocal.",
                "Los marcos cambian entre barra inferior y riel lateral; el "
                "catálogo pasa de una a tres columnas; y las pantallas de "
                "formulario usan dos paneles con DosColumnas.",
            ],
            "codigo": (
                "val clase = claseParaAncho(maxWidth.value)\n"
                "CompositionLocalProvider(LocalClaseVentana provides clase) {\n"
                "    contenido()\n"
                "}"
            ),
            "pie": "ui/adaptativo/ClaseVentana.kt (resumen)",
        },
    ],
    "recorrido_intro": (
        "La acción que mejor resume tu trabajo es un pedido hecho sin internet: "
        "toca todas las capas que diseñaste."
    ),
    "recorrido": [
        "El cliente confirma el carrito: CarritoViewModel llama a "
        "pedidoRepository.crear(...).",
        "PedidoRepository genera un folio APP- y guarda cabecera y líneas en una "
        "sola transacción de Room, con pendiente_sincronizar = true.",
        "La pantalla de seguimiento abre de inmediato porque lee de Room.",
        "Cuando vuelve la red, ObservadorConectividad lo detecta y el repositorio "
        "recorre pendientesDeSincronizar() y hace POST /pedidos.",
        "El servidor, al ver un folio que ya existe, responde 200 con el mismo "
        "pedido en vez de crear otro.",
        "El repositorio guarda la respuesta y apaga la bandera. La lista se "
        "actualiza sola porque es un Flow de Room.",
    ],
    "archivos": [
        ("di/ContenedorApp.kt", "Cómo se arma toda la app y qué recibe cada repositorio"),
        ("data/local/AcambapanDatabase.kt", "Versión de la base y migración de direcciones y metodo_pago"),
        ("data/local/entidades/Entidades.kt", "Las 11 entidades y sus columnas"),
        ("data/remote/ApiServicio.kt", "Los 24 endpoints del contrato"),
        ("data/remote/Resultado.kt", "Cómo un error de red se convierte en mensaje para el usuario"),
        ("data/repository/PedidoRepository.kt", "La cola offline y el folio idempotente"),
        ("ui/adaptativo/ClaseVentana.kt", "Cómo se decide compacta, mediana o expandida"),
        ("res/xml/locales_config.xml", "Los idiomas declarados para el selector"),
    ],
    "preguntas": [
        ("¿Por qué no usaron Hilt?",
         "Con siete repositorios el contenedor manual se lee de un vistazo, no "
         "añade procesamiento de anotaciones y deja visible la cadena de "
         "dependencias que pide la materia."),
        ("¿Qué pasa si el usuario está sin internet?",
         "La app lee de Room; el catálogo y el inventario funcionan. Un pedido "
         "queda en cola con folio APP- y se envía al volver la red."),
        ("¿Cómo evitan pedidos duplicados?",
         "El folio se genera en el teléfono y el servidor lo trata como "
         "idempotente: si ya existe responde 200 con el pedido guardado."),
        ("¿Dónde se guarda la contraseña?",
         "Nunca en el teléfono. El servidor valida y devuelve un token HMAC de "
         "12 horas; el teléfono guarda el token, no la contraseña."),
        ("¿Cómo se cambia el idioma?",
         "Android elige values o values-en según el idioma; en Android 13+ "
         "también por app gracias a locales_config.xml."),
        ("¿Por qué no hay una pantalla por tamaño?",
         "Porque se mide la ventana, no el dispositivo: un mismo layout "
         "adaptativo también funciona en pantalla dividida."),
    ],
    "limites": [
        "Las mejoras de catálogo, carrito e historial son de Janetzy.",
        "La pantalla de acceso y la búsqueda de pedidos del panel son de Ambar.",
        "Si te preguntan por una pantalla concreta que no tocaste, di de quién es "
        "y explica cómo llega a tu capa de datos: eso sí es tuyo.",
    ],
}

# ===========================================================================
# Janetzy
# ===========================================================================
JANETZY = {
    "nombre": "Janetzy Maldonado Nava",
    "nombre_corto": "Janetzy",
    "rol": "Experiencia del cliente: catálogo, carrito, detalle e historial",
    "archivo": "Guia individual - Janetzy Maldonado Nava.docx",
    "intro": (
        "Tu parte es el recorrido del cliente: lo que ve y toca desde que entra "
        "al catálogo hasta que revisa sus pedidos. Trabajaste sobre las pantallas "
        "de Compose y sus ViewModel, y en el camino tocaste el repositorio y la "
        "capa local para traer direcciones guardadas y la horneada del día."
    ),
    "commits": [
        ("04", "Catálogo y carrito: etiqueta Agotado, contador con filtros, botón "
               "Limpiar filtros, chips de direcciones guardadas y aviso de envío gratis"),
        ("05", "Detalle del producto: tarjeta de próxima horneada con hora de salida "
               "y piezas disponibles"),
        ("06", "Perfil: bloque con los últimos pedidos, folio, fecha y total"),
    ],
    "caja": (
        "Tú respondes por: cómo una pantalla de Compose muestra el estado de su "
        "ViewModel, cómo se filtra el catálogo en memoria, cómo el carrito toma "
        "las direcciones guardadas y cómo se calcula lo que falta para el envío "
        "gratis."
    ),
    "conceptos": [
        {
            "titulo": "Compose: estado y recomposición",
            "texto": [
                "En Compose la pantalla se redibuja cuando cambia un estado. Hay "
                "dos tipos: el del ViewModel (datos de negocio) y el local (algo "
                "de la interfaz, como la cantidad elegida).",
                "El del ViewModel se observa con collectAsStateWithLifecycle; el "
                "local se guarda con remember y mutableStateOf.",
            ],
            "codigo": (
                "val estado by viewModel.estado.collectAsStateWithLifecycle()\n"
                "var cantidad by remember { mutableIntStateOf(1) }"
            ),
            "pie": "DetalleProductoScreen.kt",
            "vinetas": [
                "collectAsStateWithLifecycle respeta el ciclo de vida: deja de "
                "observar cuando la pantalla no está visible.",
                "remember conserva el valor entre recomposiciones, pero se pierde "
                "si el sistema mata la Activity.",
            ],
        },
        {
            "titulo": "Listas perezosas: LazyColumn y LazyVerticalGrid",
            "texto": [
                "El catálogo no dibuja los 12 productos de golpe: usa una lista "
                "perezosa que solo compone lo visible. Con la misma lógica se "
                "cambia entre una columna y una cuadrícula de dos o tres.",
                "La key es importante: le dice a Compose qué elemento es cada uno "
                "para no confundir el estado al reordenar.",
            ],
            "codigo": (
                "LazyVerticalGrid(columns = GridCells.Fixed(columnas)) {\n"
                "    itemsCuadricula(estado.visibles, key = { it.id }) { producto ->\n"
                "        TarjetaProducto(\n"
                "            producto = producto,\n"
                "            onClick = { onAbrirProducto(producto.id) }\n"
                "        )\n"
                "    }\n"
                "}"
            ),
            "pie": "CatalogoScreen.kt (resumen)",
        },
        {
            "titulo": "ViewModel: estado inmutable y filtros derivados",
            "texto": [
                "Tu ViewModel del catálogo no guarda la lista filtrada: guarda los "
                "productos, la búsqueda y la categoría, y calcula visibles cada "
                "vez que se consulta. Eso evita estados inconsistentes.",
            ],
            "codigo": (
                "val visibles: List<Producto>\n"
                "    get() {\n"
                "        val texto = busqueda.trim()\n"
                "        return productos.filter { producto ->\n"
                "            (categoriaId == null || producto.categoriaId == categoriaId) &&\n"
                "                (texto.isEmpty() ||\n"
                "                    producto.nombre.contains(texto, ignoreCase = true) ||\n"
                "                    producto.descripcion.contains(texto, ignoreCase = true))\n"
                "        }\n"
                "    }"
            ),
            "pie": "CatalogoViewModel.kt",
            "vinetas": [
                "limpiarFiltros() deja búsqueda vacía y categoría nula en una sola actualización.",
                "El estado se cambia con copy(...) sobre un data class: nunca se muta a mano.",
            ],
        },
        {
            "titulo": "El carrito: direcciones guardadas y envío gratis",
            "texto": [
                "El carrito pide al repositorio de direcciones las del cliente y "
                "las ofrece como chips: un toque llena el campo de entrega.",
                "El cálculo de cuánto falta para el envío gratis es una propiedad "
                "derivada del estado, no un dato guardado: si cambia el subtotal, "
                "cambia solo.",
            ],
            "codigo": (
                "val faltaParaEnvioGratis: Double\n"
                "    get() = if (aDomicilio && costoEnvio > 0.0) {\n"
                "        (PedidoRepository.ENVIO_GRATIS_DESDE - subtotal).coerceAtLeast(0.0)\n"
                "    } else 0.0"
            ),
            "pie": "CarritoViewModel.kt",
        },
        {
            "titulo": "La horneada del día",
            "texto": [
                "El repositorio del catálogo ya exponía las tandas como Flow. En "
                "el detalle se filtra por producto y se elige la que tiene más "
                "piezas disponibles, para mostrar hora de salida y existencias.",
            ],
            "codigo": (
                "catalogoRepository.tandas.collect { tandas ->\n"
                "    val delProducto = tandas\n"
                "        .filter { it.productoId == productoId }\n"
                "        .maxByOrNull { it.piezasDisponibles }\n"
                "    _estado.update { it.copy(tanda = delProducto) }\n"
                "}"
            ),
            "pie": "DetalleProductoViewModel.kt",
        },
        {
            "titulo": "Textos en dos idiomas",
            "texto": [
                "Tus pantallas usan claves con prefijo cat_, det_, car_, seg_ y "
                "per_. El texto no está en Kotlin: está en strings_cliente_compra.xml "
                "y strings_cuenta.xml, con su versión en inglés.",
                "Cuando el texto lleva un número se pasan argumentos, no se concatena.",
            ],
            "codigo": (
                "stringResource(R.string.cat_contador_filtrado, visibles, total)\n"
                "// <string name=\"cat_contador_filtrado\">%1$d de %2$d artículos</string>"
            ),
            "pie": "CatalogoScreen.kt y res/values",
        },
    ],
    "recorrido_intro": (
        "Elige esta función para demostrar: armar un pedido aprovechando una "
        "dirección guardada."
    ),
    "recorrido": [
        "El cliente entra al catálogo: CatalogoScreen observa el estado del "
        "ViewModel y dibuja la lista o la cuadrícula.",
        "Escribe en el buscador: cambiarBusqueda actualiza el estado y visibles "
        "se recalcula; el contador pasa a 'N de M artículos'.",
        "Abre un producto: el detalle pide la ficha a Room y, aparte, las tandas "
        "para mostrar la próxima horneada.",
        "Agrega al carrito: CarritoViewModel recibe la línea y recalcula subtotal, "
        "envío y total.",
        "El carrito ofrece sus direcciones guardadas como chips; al tocar una se "
        "llena el campo de entrega.",
        "Confirma el pedido y el seguimiento muestra el folio. En el perfil, el "
        "bloque de últimos pedidos lo lista con fecha y total.",
    ],
    "archivos": [
        ("ui/screens/CatalogoScreen.kt", "Lista y cuadrícula, contador con filtros y cabecera que se desplaza"),
        ("ui/viewmodel/CatalogoViewModel.kt", "El filtro calculado en visibles y limpiarFiltros()"),
        ("ui/screens/CarritoScreen.kt", "Chips de direcciones y aviso de envío gratis"),
        ("ui/viewmodel/CarritoViewModel.kt", "Direcciones del cliente y faltaParaEnvioGratis"),
        ("ui/screens/DetalleProductoScreen.kt", "La tarjeta de próxima horneada"),
        ("ui/viewmodel/DetalleProductoViewModel.kt", "El filtrado de tandas por producto"),
        ("ui/screens/PerfilScreen.kt", "El bloque de últimos pedidos"),
    ],
    "preguntas": [
        ("¿Dónde vive el estado de la pantalla?",
         "En el ViewModel, expuesto como StateFlow. La pantalla solo lo observa "
         "con collectAsStateWithLifecycle."),
        ("¿Por qué el filtro no se guarda en el estado?",
         "Porque se puede calcular: guardar productos, búsqueda y categoría "
         "evita que la lista filtrada quede desincronizada."),
        ("¿Qué hace key = { it.id }?",
         "Le dice a Compose qué elemento es cada uno, para conservar el estado "
         "correcto cuando la lista cambia."),
        ("¿De dónde salen las direcciones del carrito?",
         "De DireccionRepository, que lee de Room; se ofrecen como chips y "
         "llenan el campo de entrega."),
        ("¿Cómo se calcula el envío gratis?",
         "Con la regla del repositorio: a domicilio y subtotal menor a 250 se "
         "cobra 30; el aviso muestra la diferencia."),
        ("¿Cómo se traducen tus pantallas?",
         "Con stringResource y claves con prefijo; los textos están en "
         "strings_cliente_compra.xml y strings_cuenta.xml, en español e inglés."),
    ],
    "limites": [
        "La arquitectura, Room y Retrofit los armó Alexander; tú los usas a "
        "través de los repositorios.",
        "La pantalla de acceso y el panel de administración son de Ambar.",
        "Si te preguntan por una consulta SQL concreta, di que la capa de datos "
        "es de Alexander y explica qué dato pediste tú.",
    ],
}

# ===========================================================================
# Ambar
# ===========================================================================
AMBAR = {
    "nombre": "Ambar Paola Fujarte Herrera",
    "nombre_corto": "Ambar",
    "rol": "Acceso, administración de la panadería, layout horizontal y pruebas",
    "archivo": "Guia individual - Ambar Paola Fujarte Herrera.docx",
    "intro": (
        "Tu parte es el acceso y la operación de la panadería: cómo entra la "
        "gente, cómo se protegen las pantallas de administrador y cómo el "
        "encargado busca y mueve pedidos. Además quedó de tu lado el ajuste de "
        "las vistas horizontales y las pruebas que respaldan la entrega."
    ),
    "commits": [
        ("07", "Acceso: botón Mostrar/Ocultar contraseña y botón de entrar "
               "habilitado solo con correo y contraseña"),
        ("08", "Gestión de pedidos: búsqueda por folio o nombre de cliente, "
               "combinada con el filtro de estado"),
        ("09", "Vistas horizontales de dos paneles, documentación y pruebas de "
               "integración (23 comprobaciones)"),
    ],
    "caja": (
        "Tú respondes por: cómo se construye un formulario en Compose, cómo se "
        "habilita un botón según el estado, cómo el panel filtra pedidos, cómo se "
        "protege una ruta por rol y qué se probó contra la API."
    ),
    "conceptos": [
        {
            "titulo": "Compose: formularios y estado local",
            "texto": [
                "Un campo de texto en Compose es un valor más una función que lo "
                "cambia. Si el estado vive en el ViewModel, el campo se vuelve "
                "controlado y no hay dos fuentes de verdad.",
                "El campo de contraseña necesita dos cosas: ocultar el texto y "
                "poder mostrarlo. Eso se logra con VisualTransformation y un "
                "estado local que alterna entre las dos formas.",
            ],
            "codigo": (
                "var contrasenaVisible by remember { mutableStateOf(false) }\n"
                "OutlinedTextField(\n"
                "    value = valor,\n"
                "    onValueChange = onValorCambio,\n"
                "    visualTransformation = if (esContrasena && !contrasenaVisible)\n"
                "        PasswordVisualTransformation() else VisualTransformation.None,\n"
                "    trailingIcon = if (esContrasena) {\n"
                "        { TextButton(onClick = { contrasenaVisible = !contrasenaVisible })\n"
                "            { Text(stringResource(if (contrasenaVisible)\n"
                "                R.string.com_ocultar else R.string.com_mostrar)) } }\n"
                "    } else null\n"
                ")"
            ),
            "pie": "ui/components/Componentes.kt · CampoTexto (resumen)",
        },
        {
            "titulo": "Habilitar un botón según el estado",
            "texto": [
                "El botón de iniciar sesión no se puede tocar si falta un dato. "
                "No hace falta una validación aparte: la condición se calcula con "
                "el mismo estado que llena los campos.",
            ],
            "codigo": (
                "BotonPrincipal(\n"
                "    texto = stringResource(R.string.log_boton_iniciar_sesion),\n"
                "    onClick = { viewModel.entrar() },\n"
                "    habilitado = !estado.cargando &&\n"
                "        estado.correo.isNotBlank() &&\n"
                "        estado.contrasena.isNotBlank()\n"
                ")"
            ),
            "pie": "ui/screens/LoginScreen.kt",
        },
        {
            "titulo": "ViewModel: dos filtros que se combinan",
            "texto": [
                "En la gestión de pedidos el administrador puede filtrar por "
                "estado y además buscar por folio o cliente. En vez de guardar la "
                "lista resultante, visibles aplica los dos criterios cada vez.",
            ],
            "codigo": (
                "val visibles: List<Pedido>\n"
                "    get() {\n"
                "        val texto = busqueda.trim()\n"
                "        return pedidos\n"
                "            .filter { filtro == null || it.estado == filtro }\n"
                "            .filter {\n"
                "                texto.isEmpty() ||\n"
                "                    it.folio.contains(texto, ignoreCase = true) ||\n"
                "                    it.cliente.contains(texto, ignoreCase = true)\n"
                "            }\n"
                "    }"
            ),
            "pie": "PedidosAdminViewModel.kt",
        },
        {
            "titulo": "Cómo se protege el panel por rol",
            "texto": [
                "La sesión guarda el rol. El NavHost decide qué mostrar: si la "
                "sesión no es ADMIN, en lugar del panel aparece la pantalla de "
                "candado. No es solo esconder botones: la ruta está protegida.",
                "Y del lado del servidor, cada operación de administrador exige "
                "token con rol ADMIN; el rol se revalida en cada petición.",
            ],
            "codigo": (
                "composable(route = Rutas.ADMIN_PANEL) {\n"
                "    if (!esAdmin) {\n"
                "        AccesoRestringidoScreen(onIrLogin = { ... }, onVolver = { ... })\n"
                "    } else {\n"
                "        AdminPanelScreen(contenedor = contenedor, ...)\n"
                "    }\n"
                "}"
            ),
            "pie": "navigation/NavegacionAcambapan.kt (resumen)",
        },
        {
            "titulo": "Cambiar el estado de un pedido",
            "texto": [
                "El administrador avanza el pedido; el ViewModel llama al "
                "repositorio, que habla con la API y guarda la respuesta en Room. "
                "El servidor valida la transición: un salto inválido devuelve 409 "
                "y la app lo muestra como mensaje.",
            ],
            "vinetas": [
                "Flujo: pantalla → ViewModel → PedidoRepository → PUT /pedidos/{folio}/estado → Room.",
                "El cliente ve el cambio al refrescar su seguimiento, porque ambos leen la misma base.",
            ],
        },
        {
            "titulo": "Las vistas horizontales",
            "texto": [
                "En pantalla ancha el contenido se reparte en dos paneles con "
                "DosColumnas: en teléfono apila, en horizontal pone lado a lado. "
                "En el catálogo también se pasó a tres columnas.",
                "Y en el catálogo se corrigió que la promoción y el título "
                "estuvieran fijos: ahora viajan dentro de la lista y dejan libre "
                "el alto para los productos.",
            ],
            "codigo": (
                "if (!clase.usaNavegacionLateral) {\n"
                "    Column(modifier.verticalScroll(rememberScrollState())) {\n"
                "        izquierda(); derecha()\n"
                "    }\n"
                "} else {\n"
                "    Row(modifier) {\n"
                "        Column(Modifier.weight(pesoIzquierda)) { izquierda() }\n"
                "        Column(Modifier.weight(pesoDerecha)) { derecha() }\n"
                "    }\n"
                "}"
            ),
            "pie": "ui/adaptativo/Disposiciones.kt (resumen)",
        },
        {
            "titulo": "Las pruebas y la documentación",
            "texto": [
                "El script pruebas_api.py habla con la API real y comprueba 23 "
                "cosas: salud, login, permisos (401 y 403), catálogo, creación de "
                "pedido con total calculado, idempotencia del folio, cambio de "
                "estado válido, salto inválido con 409, direcciones y "
                "notificaciones.",
                "La documentación se genera con scripts: los diagramas con "
                "matplotlib y los documentos con python-docx, para que las cifras "
                "salgan de leer el proyecto y no de escribirlas a mano.",
            ],
            "vinetas": [
                "23 de 23 comprobaciones en verde contra el XAMPP local.",
                "Cinco diagramas: entidad-relación, paquetes, componentes, despliegue y navegación.",
            ],
        },
    ],
    "recorrido_intro": (
        "Elige esta función para demostrar: buscar un pedido y avanzarlo de estado."
    ),
    "recorrido": [
        "Entras con la cuenta de administrador; el login guarda el token y el rol.",
        "El NavHost ve que la sesión es ADMIN y abre el panel; si no lo fuera, "
        "mostraría el candado.",
        "En Pedidos escribes un folio o el nombre de un cliente: visibles filtra "
        "por texto y por el chip de estado a la vez.",
        "Tocas 'Marcar en preparación': el ViewModel llama al repositorio.",
        "El repositorio hace PUT /pedidos/{folio}/estado con el token; el "
        "servidor valida la transición y responde el pedido actualizado.",
        "El repositorio guarda en Room y la lista se actualiza sola porque es un Flow.",
    ],
    "archivos": [
        ("ui/components/Componentes.kt", "CampoTexto con Mostrar/Ocultar la contraseña"),
        ("ui/screens/LoginScreen.kt", "El botón habilitado solo con datos completos"),
        ("ui/viewmodel/PedidosAdminViewModel.kt", "La búsqueda combinada con el filtro"),
        ("ui/screens/GestionPedidosScreen.kt", "El campo de búsqueda y los chips de estado"),
        ("navigation/NavegacionAcambapan.kt", "La guarda de rol del panel"),
        ("ui/adaptativo/Disposiciones.kt", "DosColumnas para el layout horizontal"),
        ("docs/evidencias/pruebas_api.py", "Las 23 comprobaciones contra la API"),
    ],
    "preguntas": [
        ("¿Qué es VisualTransformation?",
         "La pieza de Compose que decide cómo se dibuja el texto de un campo; "
         "PasswordVisualTransformation lo oculta y VisualTransformation.None lo muestra."),
        ("¿Por qué el botón se deshabilita?",
         "Para no mandar una petición que el repositorio va a rechazar; la "
         "condición usa el mismo estado que llena los campos."),
        ("¿Cómo se protege el panel?",
         "Dos veces: la ruta del NavHost revisa el rol y muestra el candado, y "
         "el servidor exige token ADMIN en cada operación."),
        ("¿Qué pasa si intento un cambio de estado inválido?",
         "El servidor responde 409 y la app muestra el mensaje; el estado no cambia."),
        ("¿Qué se probó?",
         "23 comprobaciones contra la API real, incluida la idempotencia del "
         "folio y los rechazos por permisos."),
        ("¿Cómo se generó la documentación?",
         "Con scripts: matplotlib para los diagramas y python-docx para los "
         "documentos; las cifras se calculan leyendo el proyecto."),
    ],
    "limites": [
        "La arquitectura y el layout base los armó Alexander; tú ajustaste las "
        "vistas horizontales de tu alcance.",
        "El catálogo, el carrito y el historial son de Janetzy.",
        "Si te preguntan por la cola offline o las migraciones de Room, di que "
        "son de Alexander y explica cómo tu panel las usa.",
    ],
}


if __name__ == "__main__":
    for persona in (ALEXANDER, JANETZY, AMBAR):
        construir(persona)
