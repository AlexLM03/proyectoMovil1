"""
Genera la guia de estudio "Como se implementaron los idiomas y la
responsividad" para el equipo de Acambapan (.docx).

Va dirigida a los tres integrantes: explica el porque de cada decision y trae
los fragmentos de codigo reales del proyecto, para que puedan defenderlo en la
revision individual.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.shared import Cm, Pt, RGBColor

from _docx_util import (CAFE, EQUIPO, GRIS, TINTA, caja_destacada,
                        documento_base, encabezado_y_pie, imagen, indice,
                        parrafo, portada, salto, tabla, vineta)

AQUI = os.path.dirname(os.path.abspath(__file__))
DIAG = os.path.join(AQUI, "diagramas")


def codigo(doc, texto, titulo=None):
    """Bloque de codigo monoespaciado con una etiqueta arriba."""
    if titulo:
        p = doc.add_paragraph()
        r = p.add_run(titulo)
        r.font.size = Pt(8.5)
        r.font.bold = True
        r.font.color.rgb = CAFE
        p.paragraph_format.space_after = Pt(2)

    from docx.oxml import OxmlElement
    from docx.oxml.ns import qn

    tabla_codigo = doc.add_table(rows=1, cols=1)
    tabla_codigo.style = "Table Grid"
    celda = tabla_codigo.rows[0].cells[0]
    sombra = OxmlElement("w:shd")
    sombra.set(qn("w:val"), "clear")
    sombra.set(qn("w:color"), "auto")
    sombra.set(qn("w:fill"), "F7F4EF")
    celda._tc.get_or_add_tcPr().append(sombra)
    celda.text = ""

    primera = True
    for linea in texto.splitlines():
        p = celda.paragraphs[0] if primera else celda.add_paragraph()
        primera = False
        r = p.add_run(linea if linea else " ")
        r.font.name = "Consolas"
        r.font.size = Pt(8)
        r.font.color.rgb = TINTA
        p.paragraph_format.space_after = Pt(0)
        p.paragraph_format.space_before = Pt(0)

    doc.add_paragraph().paragraph_format.space_after = Pt(4)


def construir():
    doc = documento_base(
        "Guía de estudio",
        "Idiomas y responsividad · Entrega final",
    )
    encabezado_y_pie(doc, "Acambapan · Guía de idiomas y responsividad")

    portada(
        doc,
        "Cómo se implementaron\nlos idiomas y la responsividad",
        "Guía de estudio para el equipo",
        "Programación Móvil I · Proyecto final · Entrega 3",
        EQUIPO,
        "Acámbaro, Guanajuato · Noviembre de 2026",
    )

    indice(doc)

    # ------------------------------------------------------------------
    doc.add_heading("Antes de empezar", level=1)
    parrafo(doc,
            "Este documento no es el de arquitectura ni el informe: es una guía "
            "para ustedes. Explica, con el código que está en el proyecto, cómo se "
            "resolvieron los dos criterios que más se preguntan en la revisión "
            "individual: el soporte de idiomas y la adaptación a distintos tamaños "
            "de pantalla.")
    parrafo(doc,
            "La idea es que cada quien pueda señalar un archivo y explicar qué hace "
            "y por qué se hizo así, sin memorizar. Al final hay una lista de "
            "preguntas probables con su respuesta corta.")

    caja_destacada(
        doc,
        "Las dos ideas de fondo",
        "Idiomas: ningún texto visible se escribe en Kotlin; todo vive en archivos "
        "de recursos y la app elige el que corresponde al idioma del usuario. "
        "Responsividad: no se diseña 'para teléfono' y 'para tablet' por separado, "
        "sino que se mide el ancho disponible y se decide la experiencia con tres "
        "clases de tamaño.",
    )

    salto(doc)

    # ==================================================================
    doc.add_heading("Parte 1 · Soporte de idiomas", level=1)

    doc.add_heading("1.1 El principio: el texto no vive en el código", level=2)
    parrafo(doc,
            "En Android, todo texto que ve el usuario debería estar en "
            "res/values/strings.xml. Si está escrito dentro de Kotlin, la app solo "
            "puede mostrar ese idioma y no hay forma de traducirla sin tocar la "
            "lógica.")
    parrafo(doc, "Comparación con el código real:", negrita=True)

    codigo(doc, 'Text(text = "Nuestro catálogo")          // MAL: texto incrustado', "Antes")
    codigo(doc, 'Text(text = stringResource(R.string.cat_titulo_catalogo))   // BIEN', "Después")

    parrafo(doc,
            "stringResource es una función de Compose que lee el recurso con el "
            "idioma activo en ese momento. Si el usuario cambia el idioma del "
            "teléfono, la misma línea devuelve el texto en inglés sin reiniciar la "
            "app.")

    doc.add_heading("1.2 Dónde está cada cosa", level=2)
    parrafo(doc,
            "El español es el idioma predeterminado: vive en res/values/. El inglés "
            "vive en res/values-en/. Android elige la carpeta según el idioma del "
            "sistema; si no encuentra una traducción, cae al idioma por defecto.")
    tabla(doc, ["Carpeta", "Idioma", "Cuándo se usa"], [
        ("res/values/", "Español (predeterminado)", "Idioma del sistema en español, "
                                                    "o cualquier idioma sin traducción"),
        ("res/values-en/", "Inglés", "Sistema en inglés"),
    ], anchos=[4.4, 5.0, 6.6], tam=9)

    parrafo(doc,
            "Para que el proyecto no se vuelva un archivo gigantesco, las claves se "
            "repartieron en siete archivos por área, con un prefijo por pantalla. "
            "Los dos idiomas declaran exactamente las mismas claves:")
    tabla(doc, ["Archivo", "Prefijos", "Qué contiene"], [
        ("strings.xml", "nav_, conn_, accion_, estado_, metodo_", "Base: navegación, "
                                                                  "conexión, acciones, estados y pagos"),
        ("strings_acceso.xml", "log_, reg_, ayu_", "Login, registro y ayuda"),
        ("strings_cliente_compra.xml", "cat_, det_, car_, seg_", "Catálogo, detalle, "
                                                                 "carrito y seguimiento"),
        ("strings_cuenta.xml", "per_, dir_, pago_, not_, acc_", "Perfil, direcciones, "
                                                                 "pagos, avisos y candado"),
        ("strings_admin.xml", "adp_, adc_, gpe_, inv_", "Panel, dash, pedidos e inventario"),
        ("strings_errores.xml", "err_", "Errores de la capa de datos"),
        ("strings_mensajes.xml", "vm_", "Confirmaciones de los ViewModel"),
    ], anchos=[4.8, 4.6, 6.6], tam=8.4)

    caja_destacada(
        doc, "Dato verificable",
        "323 claves en español y 323 en inglés. Un script de conteo confirma que "
        "ambos idiomas declaran los mismos nombres, así que no hay una pantalla "
        "traducida a medias.",
    )

    doc.add_heading("1.3 Textos con valores variables", level=2)
    parrafo(doc,
            "Cuando el texto lleva un número o un nombre, no se arma concatenando en "
            "Kotlin: se declara el hueco en el recurso y se pasa como argumento. Así "
            "el traductor puede mover el orden de las palabras.")
    codigo(doc,
           '<string name="cat_contador_filtrado">%1$d de %2$d artículos</string>\n'
           '<string name="cat_contador_filtrado">%1$d of %2$d items</string>',
           "En strings.xml (es) y strings-en.xml (en)")
    codigo(doc,
           'stringResource(R.string.cat_contador_filtrado, visibles, total)',
           "En la pantalla")

    doc.add_heading("1.4 Elegir el idioma solo para esta app", level=2)
    parrafo(doc,
            "Desde Android 13 el usuario puede poner un idioma distinto solo a "
            "Acambapan, sin cambiar todo el teléfono. Para eso se declara qué idiomas "
            "soporta la app.")
    codigo(doc, '<!-- app/src/main/res/xml/locales_config.xml -->\n'
                '<locale-config xmlns:android="http://schemas.android.com/apk/res/android">\n'
                '    <locale android:name="es" />\n'
                '    <locale android:name="en" />\n'
                '</locale-config>', "locales_config.xml")
    codigo(doc,
           '<application\n'
           '    android:localeConfig="@xml/locales_config"\n'
           '    ... >', "AndroidManifest.xml")
    parrafo(doc,
            "Con eso aparece la opción en Ajustes → Sistema → Idiomas → Idiomas de "
            "la app → Acambapan. En versiones anteriores funciona cambiando el "
            "idioma del teléfono.")

    salto(doc)

    doc.add_heading("1.5 El caso difícil: los enums del dominio", level=2)
    parrafo(doc,
            "Los estados del pedido y los métodos de pago son enums que también "
            "viajan a la base de datos. No podemos ponerles texto traducido dentro, "
            "porque su valor debe seguir siendo RECIBIDO o EFECTIVO para hablar con "
            "la API y con Room.")
    parrafo(doc,
            "La solución: el enum conserva su código, y una extensión aparte dice "
            "cuál es su recurso de texto.")
    codigo(doc,
           '@get:StringRes\n'
           'val EstadoPedido.etiquetaRes: Int\n'
           '    get() = when (this) {\n'
           '        EstadoPedido.RECIBIDO -> R.string.estado_recibido\n'
           '        EstadoPedido.EN_PREPARACION -> R.string.estado_en_preparacion\n'
           '        EstadoPedido.EN_CAMINO -> R.string.estado_en_camino\n'
           '        EstadoPedido.ENTREGADO -> R.string.estado_entregado\n'
           '        EstadoPedido.CANCELADO -> R.string.estado_cancelado\n'
           '    }', "ui/EtiquetasRes.kt")
    codigo(doc, 'Text(text = stringResource(pedido.estado.etiquetaRes))', "Uso en pantalla")
    parrafo(doc,
            "La ventaja: el dominio (data/modelo) sigue sin depender de Android, y el "
            "texto que ve el usuario sí está traducido.")

    doc.add_heading("1.6 El otro caso difícil: los mensajes de error", level=2)
    parrafo(doc,
            "Los repositorios y los ViewModel también escriben mensajes "
            "('Necesitas conexión para guardar una dirección'). Si se quedan como "
            "texto en español, al cambiar a inglés la app mostraría errores en "
            "español. Se resolvió así:")
    vineta(doc, "el fallo puede llevar un recurso (recurso) o un texto del servidor "
                "(mensaje), porque los mensajes de la API llegan ya redactados.",
           negrita_hasta="Resultado.Fallo ")
    vineta(doc, "devuelve el texto en el idioma activo.",
           negrita_hasta="texto(contexto) ")
    vineta(doc, "se lanza dentro de ejecutarRed cuando la regla de negocio la "
                "escribe la app, para no perder el recurso al pasar por el manejo "
                "de excepciones.",
           negrita_hasta="ErrorLocalizado ")
    codigo(doc,
           'data class Fallo(\n'
           '    val mensaje: String = "",\n'
           '    @param:StringRes val recurso: Int = 0,\n'
           '    val argumentos: List<Any> = emptyList()\n'
           ') : Resultado<Nothing> {\n'
           '    constructor(@StringRes recurso: Int, vararg argumentos: Any) :\n'
           '        this("", recurso, argumentos.toList())\n'
           '\n'
           '    fun texto(contexto: Context): String =\n'
           '        if (recurso != 0) contexto.getString(recurso, *argumentos.toTypedArray())\n'
           '        else mensaje\n'
           '}', "data/remote/Resultado.kt (resumen)")
    parrafo(doc,
            "Para que los ViewModel puedan resolver el texto, ContenedorApp expone el "
            "contexto de aplicación y cada ViewModel lo recibe en su fábrica. Las "
            "pantallas no cambiaron: siguen mostrando un String.")

    doc.add_heading("1.7 Errores comunes (y cómo se evitan aquí)", level=2)
    tabla(doc, ["Error", "Por qué falla", "Cómo se evita"], [
        ("Usar stringResource dentro de remember { } o de un onClick",
         "No es un contexto composable; no compila",
         "Se saca a una variable antes: val texto = stringResource(...)"),
        ("Poner apóstrofos o & sin escapar en el XML",
         "El XML se rompe o el recurso no compila",
         "Se escribe \\' y &amp;"),
        ("Usar % literal en un texto con argumentos",
         "El formateador lo interpreta como marcador",
         "Se escribe %%"),
        ("Concatenar en Kotlin: \"Total: \" + total",
         "El orden de las palabras queda fijo en un idioma",
         "Se usa un recurso con %1$s y se pasan argumentos"),
        ("Traducir solo las pantallas",
         "Los errores y confirmaciones siguen en español",
         "Repositorios y ViewModel piden su texto a los recursos"),
    ], anchos=[5.4, 5.4, 5.2], tam=8.4)

    doc.add_heading("1.8 Si agregan una pantalla nueva", level=2)
    for paso in [
        "Elegir el prefijo del área (por ejemplo inv_ si es inventario).",
        "Declarar cada texto en res/values/strings_<área>.xml y su traducción en "
        "res/values-en/strings_<área>.xml, con los mismos nombres.",
        "En la pantalla, usar stringResource(R.string.prefijo_clave) e importar "
        "androidx.compose.ui.res.stringResource y com.devnoway.posproyect.R.",
        "Si el texto lleva un valor, usar %1$s / %1$d y pasar argumentos.",
        "Compilar y contar las claves de ambos idiomas para confirmar que coinciden.",
    ]:
        vineta(doc, paso)

    salto(doc)

    # ==================================================================
    doc.add_heading("Parte 2 · Responsividad", level=1)

    doc.add_heading("2.1 La idea: medir la ventana, no adivinar el dispositivo", level=2)
    parrafo(doc,
            "Un teléfono en horizontal tiene casi el mismo ancho que una tableta "
            "pequeña, y una tableta puede mostrar la app en ventana dividida. Por eso "
            "no se pregunta '¿es tableta?', sino '¿cuánto ancho hay ahora mismo?'.")
    parrafo(doc,
            "Se usan los puntos de corte de Material 3: menos de 600 dp es compacta, "
            "de 600 a 839 es mediana y desde 840 es expandida. dp es una unidad que "
            "no depende de la densidad de píxeles de la pantalla.")
    tabla(doc, ["Clase", "Ancho", "Qué cambia"], [
        ("COMPACTA", "menos de 600 dp", "Barra inferior, una columna, pantallas apiladas"),
        ("MEDIANA", "600 a 839 dp", "Riel lateral, dos columnas, dos paneles en formularios"),
        ("EXPANDIDA", "840 dp o más", "Riel lateral, tres columnas, contenido centrado a 1080 dp"),
    ], anchos=[3.4, 4.2, 8.4], tam=9)

    doc.add_heading("2.2 Dónde se calcula", level=2)
    parrafo(doc,
            "BoxWithConstraints entrega el ancho real disponible en dp. Con ese valor "
            "se decide la clase y se publica para todo el árbol de composición con un "
            "CompositionLocal, de modo que cualquier pantalla la lea sin recibir "
            "parámetros extra.")
    codigo(doc,
           'enum class ClaseVentana {\n'
           '    COMPACTA, MEDIANA, EXPANDIDA;\n'
           '    val usaNavegacionLateral: Boolean get() = this != COMPACTA\n'
           '    val anchoMaximoContenido: Dp get() = when (this) {\n'
           '        COMPACTA -> Dp.Unspecified\n'
           '        MEDIANA -> 820.dp\n'
           '        EXPANDIDA -> 1080.dp\n'
           '    }\n'
           '    val columnasCatalogo: Int get() = when (this) {\n'
           '        COMPACTA -> 1\n'
           '        MEDIANA -> 2\n'
           '        EXPANDIDA -> 3\n'
           '    }\n'
           '}\n'
           '\n'
           'val LocalClaseVentana = staticCompositionLocalOf { ClaseVentana.COMPACTA }\n'
           '\n'
           '@Composable\n'
           'fun ProveedorClaseVentana(modifier: Modifier = Modifier,\n'
           '                          contenido: @Composable () -> Unit) {\n'
           '    BoxWithConstraints(modifier = modifier) {\n'
           '        val clase = claseParaAncho(maxWidth.value)\n'
           '        CompositionLocalProvider(LocalClaseVentana provides clase) {\n'
           '            contenido()\n'
           '        }\n'
           '    }\n'
           '}', "ui/adaptativo/ClaseVentana.kt")
    parrafo(doc,
            "Se envuelve la app una sola vez en MainActivity, entonces todas las "
            "pantallas ya conocen la clase activa.")

    doc.add_heading("2.3 Cambio 1: la navegación", level=2)
    parrafo(doc,
            "En teléfono, la barra inferior. En pantalla ancha, un riel a la "
            "izquierda: gasta ancho en lugar de alto, que es lo escaso en horizontal.")
    codigo(doc,
           'Scaffold(\n'
           '    topBar = { BarraSuperior(...) },\n'
           '    bottomBar = {\n'
           '        if (!clase.usaNavegacionLateral) BarraInferior(destinos, ruta, onNavegar)\n'
           '    }\n'
           ') { padding ->\n'
           '    Row(Modifier.fillMaxSize().padding(padding)) {\n'
           '        if (clase.usaNavegacionLateral) BarraLateral(destinos, ruta, onNavegar)\n'
           '        Box(Modifier.weight(1f), contentAlignment = Alignment.TopCenter) {\n'
           '            Column(Modifier.widthIn(max = anchoMaximo), content = contenido)\n'
           '        }\n'
           '    }\n'
           '}', "ui/components/Componentes.kt · MarcoAdaptable (resumen)")
    parrafo(doc,
            "El anchoIn(max = ...) evita que el contenido se estire de borde a borde "
            "en una ventana enorme, que es lo que hace ilegible un formulario.")

    doc.add_heading("2.4 Cambio 2: dos paneles en lugar de uno", level=2)
    parrafo(doc,
            "Para formularios y detalles se creó DosColumnas: en compacta apila, en "
            "ancha reparte en dos paneles con scroll independiente.")
    codigo(doc,
           '@Composable\n'
           'fun DosColumnas(modifier: Modifier = Modifier, ...,\n'
           '                izquierda: @Composable ColumnScope.() -> Unit,\n'
           '                derecha: @Composable ColumnScope.() -> Unit) {\n'
           '    if (!LocalClaseVentana.current.usaNavegacionLateral) {\n'
           '        Column(modifier.verticalScroll(rememberScrollState())) {\n'
           '            izquierda(); derecha()\n'
           '        }\n'
           '    } else {\n'
           '        Row(modifier) {\n'
           '            Column(Modifier.weight(pesoIzquierda).verticalScroll(...)) { izquierda() }\n'
           '            Column(Modifier.weight(pesoDerecha).verticalScroll(...)) { derecha() }\n'
           '        }\n'
           '    }\n'
           '}', "ui/adaptativo/Disposiciones.kt (resumen)")
    tabla(doc, ["Pantalla", "Izquierda", "Derecha"], [
        ("Login", "Marca y eslogan", "Correo, contraseña y botones"),
        ("Registro", "Marca", "Formulario de alta"),
        ("Detalle de producto", "Imagen, nombre, precio, horneada", "Cantidad, total y botones"),
        ("Carrito", "Líneas del pedido", "Entrega, pago, resumen y confirmar"),
        ("Perfil", "Datos de la cuenta", "Ajustes, historial y sesión"),
    ], anchos=[3.8, 6.0, 6.2], tam=8.6)

    doc.add_heading("2.5 Cambio 3: la cuadrícula del catálogo", level=2)
    parrafo(doc,
            "El catálogo usa LazyColumn cuando hay una sola columna y LazyVerticalGrid "
            "cuando hay dos o tres. Las dos variantes son listas perezosas: solo "
            "dibujan lo que se ve, así que el rendimiento no depende de cuántos "
            "productos haya.")
    parrafo(doc,
            "Detalle importante que se corrigió: la promoción y el título estaban "
            "fijos y robaban alto a la lista. Ahora viajan como primer elemento de la "
            "lista (CabeceraCatalogo), de modo que al hacer scroll se van y los "
            "productos usan toda la pantalla. Solo el buscador y los chips quedan "
            "fijos, porque son los controles.")

    doc.add_heading("2.6 Errores comunes (y cómo se evitan aquí)", level=2)
    tabla(doc, ["Error", "Por qué falla", "Cómo se evita"], [
        ("Meter un LazyColumn dentro de un Column con verticalScroll",
         "Dos scrolls en la misma dirección: excepción en tiempo de ejecución",
         "La lista lleva weight(1f) y el contenedor no scrollea"),
        ("Usar el tamaño físico del dispositivo",
         "Falla en pantalla dividida y en ventanas",
         "Se mide con BoxWithConstraints"),
        ("Dejar el contenido a todo lo ancho en una ventana grande",
         "Líneas larguísimas, difícil de leer",
         "widthIn(max = 820 / 1080 dp) y centrado"),
        ("Repetir el mismo layout con tamaños distintos",
         "No es responsividad, es escalar",
         "Cambia la estructura: barra vs riel, una vs dos columnas"),
    ], anchos=[5.6, 5.2, 5.2], tam=8.4)

    doc.add_heading("2.7 Cómo demostrarlo en la revisión", level=2)
    for paso in [
        "Abrir la app en el emulador vertical: barra inferior y catálogo de una columna.",
        "Girar con Ctrl + flecha izquierda: aparece el riel lateral y el catálogo se "
        "ve en dos o tres columnas.",
        "Abrir el detalle de un producto: ficha a la izquierda, compra a la derecha.",
        "Ir al carrito: líneas a la izquierda, resumen de pago a la derecha.",
        "Volver al catálogo y hacer scroll: la promoción sube y los productos llenan "
        "la pantalla.",
    ]:
        vineta(doc, paso)

    salto(doc)

    # ==================================================================
    doc.add_heading("Preguntas probables en la revisión", level=1)
    parrafo(doc,
            "Respuestas cortas, para tenerlas claras. No hace falta recitarlas: sirven "
            "para ubicar el archivo correcto.")
    tabla(doc, ["Pregunta", "Respuesta corta"], [
        ("¿Dónde está el texto de la app?",
         "En res/values (español) y res/values-en (inglés); 323 claves por idioma en "
         "siete archivos."),
        ("¿Cómo se cambia el idioma?",
         "Android elige la carpeta según el idioma del sistema; en Android 13+ "
         "también por app, gracias a locales_config.xml."),
        ("¿Qué pasa con un texto que lleva un número?",
         "Se declara con %1$d o %1$s y se pasa como argumento a stringResource."),
        ("¿Cómo se traducen los estados del pedido?",
         "El enum guarda el código para la API y EtiquetasRes.kt dice qué recurso le "
         "corresponde."),
        ("¿Y los mensajes de error?",
         "Resultado.Fallo puede llevar un recurso; texto(contexto) lo resuelve en el "
         "idioma activo."),
        ("¿La app funciona sin internet?",
         "Sí: Room es la copia local; los pedidos sin red quedan en cola y se envían "
         "con folio idempotente."),
        ("¿Cómo se adapta a pantallas grandes?",
         "Se mide la ventana con BoxWithConstraints y se elige COMPACTA, MEDIANA o "
         "EXPANDIDA."),
        ("¿Qué cambia entre clases?",
         "Navegación (barra o riel), número de columnas del catálogo y paneles de "
         "formulario."),
        ("¿Por qué no usaron dos layouts separados?",
         "Porque un mismo layout con estructura adaptativa evita duplicar pantallas y "
         "funciona también en ventana dividida."),
        ("¿Dónde se ve que no quedó texto sin traducir?",
         "Un conteo automático compara las claves de ambos idiomas y un buscador "
         "recorre el código en busca de literales."),
    ], anchos=[6.4, 9.6], tam=8.6)

    doc.add_heading("Mapa rápido de archivos", level=1)
    tabla(doc, ["Archivo", "Para qué sirve"], [
        ("res/values/strings*.xml", "Todos los textos en español"),
        ("res/values-en/strings*.xml", "Las mismas claves en inglés"),
        ("res/xml/locales_config.xml", "Declara es y en para el selector por app"),
        ("ui/EtiquetasRes.kt", "Traduce los enums del dominio"),
        ("data/remote/Resultado.kt", "Fallo con recurso y texto(contexto)"),
        ("ui/adaptativo/ClaseVentana.kt", "Clases de tamaño y CompositionLocal"),
        ("ui/adaptativo/Disposiciones.kt", "DosColumnas"),
        ("ui/components/Componentes.kt", "MarcoAdaptable: barra inferior o riel"),
        ("ui/screens/CatalogoScreen.kt", "Lista o cuadrícula, cabecera que scrollea"),
        ("MainActivity.kt", "Envuelve la app en ProveedorClaseVentana"),
    ], anchos=[6.4, 9.6], tam=8.6)

    parrafo(doc,
            "Si algo de esto no cuadra con el código, gana el código: abran el archivo "
            "y compárenlo. Esta guía describe el proyecto tal como quedó en la "
            "entrega 09.")

    ruta = os.path.join(AQUI, "Guia - idiomas y responsividad - Acambapan.docx")
    doc.save(ruta)
    print("OK", ruta)
    return ruta


if __name__ == "__main__":
    construir()
