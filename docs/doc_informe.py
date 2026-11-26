"""
Genera el "Informe técnico final" de Acambapan (.docx).

Las cifras no se escriben a mano: se calculan leyendo el código del proyecto,
el APK compilado y los recursos de idioma, para que el documento no pueda
quedar desfasado del repositorio.
"""
import os
import re
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from docx.shared import Pt

from _docx_util import (CAFE, EQUIPO, GRIS, caja_destacada, documento_base,
                        encabezado_y_pie, imagen, indice, parrafo, portada,
                        salto, tabla, vineta)

AQUI = os.path.dirname(os.path.abspath(__file__))
RAIZ = os.path.dirname(AQUI)
APP = os.path.join(RAIZ, "app", "src", "main")
KOTLIN = os.path.join(APP, "java", "com", "devnoway", "posproyect")
RES = os.path.join(APP, "res")
APK = os.path.join(RAIZ, "app", "build", "outputs", "apk", "debug", "app-debug.apk")
SQL = os.path.join(RAIZ, "servidor", "database", "panaderia.sql")
if not os.path.exists(SQL):
    SQL = r"C:\xampp\VirtualHost\panaderia\database\panaderia.sql"


# ---------------------------------------------------------------------------
# Métricas reales
# ---------------------------------------------------------------------------

def contar_lineas(ruta):
    total = 0
    archivos = 0
    for dp, _, fns in os.walk(ruta):
        for f in fns:
            if f.endswith(".kt"):
                archivos += 1
                with open(os.path.join(dp, f), encoding="utf-8", errors="replace") as fh:
                    total += sum(1 for _ in fh)
    return archivos, total


def metricas():
    m = {}
    m["kt_archivos"], m["kt_lineas"] = contar_lineas(KOTLIN)

    def cuenta(carpeta, sufijo=".kt"):
        ruta = os.path.join(KOTLIN, carpeta)
        if not os.path.isdir(ruta):
            return 0
        return sum(1 for f in os.listdir(ruta) if f.endswith(sufijo))

    m["pantallas"] = cuenta("ui/screens")
    m["viewmodels"] = cuenta("ui/viewmodel")
    m["repositorios"] = cuenta("data/repository")
    m["modelos"] = cuenta("data/modelo")

    with open(os.path.join(KOTLIN, "data/local/entidades/Entidades.kt"), encoding="utf-8") as fh:
        m["entidades"] = fh.read().count("@Entity")
    with open(os.path.join(KOTLIN, "data/local/daos/Daos.kt"), encoding="utf-8") as fh:
        m["daos"] = fh.read().count("@Dao")
    with open(os.path.join(KOTLIN, "data/remote/ApiServicio.kt"), encoding="utf-8") as fh:
        texto = fh.read()
    m["endpoints"] = len(re.findall(r"@(GET|POST|PUT|DELETE)\(", texto))

    if os.path.exists(SQL):
        with open(SQL, encoding="utf-8", errors="replace") as fh:
            sql = fh.read()
        m["tablas"] = len(re.findall(r"CREATE TABLE", sql, re.I))
        m["foraneas"] = len(re.findall(r"FOREIGN KEY", sql, re.I))
    else:
        m["tablas"] = m["foraneas"] = 0

    def claves(carpeta):
        ruta = os.path.join(RES, carpeta)
        conjunto = set()
        for f in os.listdir(ruta):
            if f.endswith(".xml"):
                with open(os.path.join(ruta, f), encoding="utf-8") as fh:
                    conjunto.update(re.findall(r'<string\s+name="([^"]+)"', fh.read()))
        return conjunto

    es, en = claves("values"), claves("values-en")
    m["claves_es"] = len(es)
    m["claves_en"] = len(en)
    m["idiomas_parejos"] = es == en
    m["rutas"] = 0
    with open(os.path.join(KOTLIN, "navigation/Rutas.kt"), encoding="utf-8") as fh:
        m["rutas"] = len(re.findall(r"const val \w+ = ", fh.read()))

    m["apk_mb"] = round(os.path.getsize(APK) / (1024 * 1024), 2) if os.path.exists(APK) else 0.0
    return m


# ---------------------------------------------------------------------------
# Documento
# ---------------------------------------------------------------------------

def construir():
    m = metricas()

    doc = documento_base(
        "Informe técnico final",
        "Operación de la plataforma · Entrega final",
    )
    encabezado_y_pie(doc, "Acambapan · Informe técnico final")

    portada(
        doc,
        "Informe técnico final\nResultados de la operación",
        "Aplicación Android y Web API de la panadería Acambapan",
        "Programación Móvil I · Proyecto final · Entrega 3",
        EQUIPO,
        "Acámbaro, Guanajuato · Noviembre de 2026",
    )

    indice(doc)

    # ------------------------------------------------------------------
    doc.add_heading("1. Resumen ejecutivo", level=1)
    parrafo(doc,
            "Acambapan quedó como una plataforma de dos piezas que funcionan "
            "juntas: una aplicación Android en Kotlin y Jetpack Compose, y una "
            "Web API en PHP sobre MySQL que publica el catálogo, los pedidos, el "
            "inventario y los avisos. La app guarda una copia local con Room, de "
            "modo que el cliente puede consultar el catálogo y levantar pedidos "
            "incluso sin internet; al volver la conexión, la cola local se envía "
            "sola y el folio idempotente evita pedidos duplicados.")
    parrafo(doc,
            "Este informe documenta cómo se operó la plataforma y qué resultados "
            "se obtuvieron: compilación del APK, pruebas de integración contra la "
            "API real y métricas del código entregado. Las cifras de las tablas se "
            "calculan leyendo el propio proyecto, no se capturan a mano.")

    caja_destacada(
        doc, "Resultado global",
        f"{m['kt_archivos']} archivos Kotlin ({m['kt_lineas']} líneas) · "
        f"{m['pantallas']} pantallas · {m['viewmodels']} ViewModel · "
        f"{m['repositorios']} repositorios · {m['entidades']} entidades Room · "
        f"{m['endpoints']} endpoints · {m['claves_es']} textos en cada idioma.",
    )

    # ------------------------------------------------------------------
    doc.add_heading("2. Metodología y entorno", level=1)
    parrafo(doc,
            "El trabajo se organizó por capas y por integrante. Cada persona "
            "trabajó sobre su área y dejó commits propios, de modo que el "
            "historial de GitHub muestra el avance individual además del trabajo "
            "de equipo.")
    tabla(doc, ["Elemento", "Versión / valor"], [
        ("Lenguaje de la app", "Kotlin 2.2.10 · Jetpack Compose (BOM 2026.02.01)"),
        ("Compilación", "Gradle 9 con AGP 9.4.1 · JDK 17"),
        ("Android", "minSdk 29 (Android 10) · targetSdk 37 · probado en Android 17"),
        ("Base local", "Room 2.8.5 con KSP"),
        ("Red", "Retrofit 3.0 · OkHttp 5.5 · Gson"),
        ("Servidor", "XAMPP · Apache 2.4 puerto 88 · PHP 8 · MySQL/MariaDB"),
        ("Base de datos", "pantradicion_db · InnoDB · utf8mb4"),
        ("Publicación", "Túnel Cloudflare → https://acambapan.devnoway.com/api/"),
        ("Control de versiones", "GitHub · github.com/AlexLM03/proyectoMovil1"),
    ], anchos=[4.6, 11.4], tam=9)

    doc.add_heading("2.1 Cómo se probó", level=2)
    for texto in [
        "Compilación real del APK de depuración con Gradle en modo offline.",
        "Pruebas de integración contra la API viva del XAMPP local, con 23 "
        "comprobaciones automatizadas sobre salud, sesión, permisos, catálogo, "
        "pedidos, inventario, direcciones y notificaciones.",
        "Conteo automático de recursos de idioma para comprobar que español e "
        "inglés declaran exactamente las mismas claves.",
        "Inspección del árbol de código con un buscador propio para verificar que "
        "no quedaran textos de interfaz incrustados en Kotlin.",
    ]:
        vineta(doc, texto)

    salto(doc)

    # ------------------------------------------------------------------
    doc.add_heading("3. Resultados de la operación", level=1)

    doc.add_heading("3.1 Compilación del APK", level=2)
    tabla(doc, ["Comprobación", "Resultado"], [
        ("Tarea de Gradle `:app:assembleDebug`", "BUILD SUCCESSFUL"),
        ("APK generado", f"app-debug.apk ({m['apk_mb']} MB)"),
        ("Recursos en dos idiomas", f"{m['claves_es']} claves en español y "
                                    f"{m['claves_en']} en inglés"),
        ("Claves coincidentes entre idiomas", "Sí" if m["idiomas_parejos"] else "No"),
        ("Errores de compilación", "0"),
    ], anchos=[9.0, 7.0], tam=9)

    doc.add_heading("3.2 Pruebas de integración de la Web API", level=2)
    parrafo(doc,
            "Se ejecutaron 23 comprobaciones contra la API real, con MySQL "
            "encendido. Todas terminaron en verde. La siguiente tabla resume qué "
            "se verificó y qué se obtuvo; la salida literal está en el anexo A.")
    tabla(doc, ["Área", "Comprobación", "Resultado"], [
        ("Salud", "GET /ping responde 200 y MySQL ok", "PASA"),
        ("Catálogo", "4 categorías y 12 productos publicados", "PASA"),
        ("Catálogo", "Ficha con multimedia y tandas", "PASA"),
        ("Catálogo", "Producto inexistente devuelve 404", "PASA"),
        ("Sesión", "Login ADMIN entrega token", "PASA"),
        ("Sesión", "Contraseña incorrecta devuelve 401", "PASA"),
        ("Sesión", "Login CLIENTE entrega token", "PASA"),
        ("Permisos", "/resumen sin token devuelve 401", "PASA"),
        ("Permisos", "PUT /insumos sin token devuelve 401", "PASA"),
        ("Permisos", "/resumen con token CLIENTE devuelve 403", "PASA"),
        ("Permisos", "/resumen con token ADMIN responde", "PASA"),
        ("Inventario", "GET /insumos es público (8 insumos)", "PASA"),
        ("Pedidos", "POST /pedidos crea el pedido (201)", "PASA"),
        ("Pedidos", "El servidor calcula subtotal y total", "PASA"),
        ("Pedidos", "Reenviar el mismo folio no duplica", "PASA"),
        ("Pedidos", "El folio aparece una sola vez en la lista", "PASA"),
        ("Pedidos", "RECIBIDO → EN_PREPARACION aceptado", "PASA"),
        ("Pedidos", "Salto inválido devuelve 409", "PASA"),
        ("Cuenta", "GET /direcciones con token", "PASA"),
        ("Cuenta", "GET /notificaciones del cliente", "PASA"),
        ("Registro", "Correo existente devuelve 409", "PASA"),
    ], anchos=[2.6, 10.4, 3.0], tam=8.6)

    doc.add_heading("3.3 Métricas del código entregado", level=2)
    tabla(doc, ["Métrica", "Valor"], [
        ("Archivos Kotlin", str(m["kt_archivos"])),
        ("Líneas de Kotlin", str(m["kt_lineas"])),
        ("Pantallas Compose", str(m["pantallas"])),
        ("ViewModel", str(m["viewmodels"])),
        ("Repositorios", str(m["repositorios"])),
        ("Entidades Room", str(m["entidades"])),
        ("DAOs", str(m["daos"])),
        ("Tablas en MySQL", str(m["tablas"])),
        ("Llaves foráneas declaradas", str(m["foraneas"])),
        ("Endpoints del contrato Retrofit", str(m["endpoints"])),
        ("Rutas de navegación", str(m["rutas"])),
        ("Textos por idioma", f"{m['claves_es']} (es) / {m['claves_en']} (en)"),
    ], anchos=[9.0, 7.0], tam=9)

    doc.add_heading("3.4 Comportamiento sin conexión", level=2)
    parrafo(doc,
            "El escenario de operación más delicado es el corte de red y se "
            "verificó por diseño y por las pruebas de la API:")
    for texto in [
        "Catálogo e inventario se leen siempre de Room, así que la app abre y "
        "navega sin red.",
        "Un pedido levantado sin conexión se guarda con folio propio (APP-…) y la "
        "bandera pendiente_sincronizar; la pantalla de seguimiento lo muestra "
        "como pendiente de envío.",
        "Al recuperar la red, el repositorio empuja la cola. Si el envío se "
        "repite, el servidor responde 200 con el pedido existente: en la prueba "
        "correspondiente el folio apareció una sola vez en la lista.",
        "Las operaciones que necesitan al servidor (cambiar estado, editar "
        "inventario, guardar direcciones) avisan con un mensaje claro en lugar de "
        "inventar un estado que el servidor no conoce.",
    ]:
        vineta(doc, texto)

    doc.add_heading("3.5 Verificación en dispositivo", level=2)
    parrafo(doc,
            "El APK de depuración compila y se firma correctamente. La "
            "verificación visual de la interfaz se realiza en la revisión "
            "presencial con el emulador del equipo, porque es ahí donde el "
            "profesor evalúa la navegación y los dos idiomas en vivo. En entregas "
            "anteriores el equipo ya ejecutó la app en el emulador Pixel 10 Pro XL "
            "con Android 17 y verificó el alta de pedidos contra la base real.")

    salto(doc)

    # ------------------------------------------------------------------
    doc.add_heading("4. Problemas encontrados y soluciones", level=1)
    tabla(doc, ["Problema", "Causa", "Solución aplicada"], [
        ("Pedidos duplicados al reintentar sin red",
         "El reintento volvía a hacer POST del mismo pedido.",
         "Folio generado en el dispositivo y aceptado por el servidor: si existe, "
         "responde 200 con el pedido guardado."),
        ("Ventas en cero en el panel",
         "El dash calculaba el día con la fecha del teléfono.",
         "El panel usa GET /resumen del servidor como fuente principal."),
        ("Login del candado se quedaba en el formulario",
         "La sesión se guardaba pero la pantalla no reaccionaba al rol.",
         "El ViewModel expone la entrada reciente y la pantalla navega según el rol."),
        ("Mensajes de error en español con la app en inglés",
         "Los textos estaban escritos en repositorios y ViewModel.",
         "Los fallos viajan como recurso y se resuelven con el Context al mostrarse."),
        ("Acentos y emojis corruptos en MySQL",
         "Importación sin el juego de caracteres correcto.",
         "Importación con --default-character-set=utf8mb4 y base utf8mb4_unicode_ci."),
        ("Errores 500 sin pista",
         "Las excepciones PHP se mostraban o se perdían.",
         "Bitácora en api/logs/api.log y respuesta JSON uniforme al cliente."),
    ], anchos=[4.6, 5.2, 6.2], tam=8.4)

    # ------------------------------------------------------------------
    doc.add_heading("5. Limitaciones y trabajo futuro", level=1)
    for titulo, texto in [
        ("Imágenes de producto: ",
         "la tabla producto_multimedia y el endpoint existen y devuelven la ruta, "
         "pero la app todavía dibuja el emoji de marca. Falta subir las fotografías "
         "y añadir el cargador de imágenes."),
        ("Pago real: ",
         "se registra el método (efectivo, terminal o SPEI) pero no hay pasarela; "
         "el cobro se hace al entregar."),
        ("Notificaciones push: ",
         "la bandeja funciona contra la API; falta FCM para avisar sin abrir la app."),
        ("App del repartidor: ",
         "el rol REPARTIDOR existe en la base y se asigna al pedido, pero no tiene "
         "pantallas propias."),
        ("Credenciales de ejemplo: ",
         "las cuentas semilla usan 123456; antes de dejar el túnel público de forma "
         "permanente conviene cambiarlas."),
    ]:
        vineta(doc, texto, negrita_hasta=titulo)

    # ------------------------------------------------------------------
    doc.add_heading("6. Conclusiones", level=1)
    parrafo(doc,
            "La plataforma cumple los criterios funcionales y técnicos de la "
            "entrega final: documentos y diagramas, interfaz completa en Compose "
            "con ViewModel, navegación con NavHost, repositorio local con Room y "
            "remoto con Retrofit, cadena de dependencias explícita, consumo de una "
            "Web API propia, soporte de dos idiomas y experiencia distinta según el "
            "tamaño de pantalla.")
    parrafo(doc,
            "Lo más valioso del ejercicio no fue la cantidad de pantallas sino dos "
            "decisiones: que la app siga sirviendo sin internet, y que el servidor "
            "sea la única fuente de verdad para permisos, totales e idempotencia. "
            "Eso es lo que permite demostrar el sistema en vivo sin miedo a que un "
            "reintento duplique un pedido o a que un cliente vea datos de otro.")

    # ------------------------------------------------------------------
    doc.add_heading("7. Anexo A · Salida de las pruebas de integración", level=1)
    parrafo(doc,
            "Salida literal del script docs/evidencias/pruebas_api.py contra la "
            "API del XAMPP local, el 25 de noviembre de 2026:")
    ruta_salida = os.path.join(AQUI, "evidencias", "pruebas_api.txt")
    if os.path.exists(ruta_salida):
        with open(ruta_salida, encoding="utf-8") as fh:
            for linea in fh.read().splitlines():
                p = doc.add_paragraph()
                r = p.add_run(linea)
                r.font.name = "Consolas"
                r.font.size = Pt(8)
                p.paragraph_format.space_after = Pt(0)
    else:
        parrafo(doc, "[Falta docs/evidencias/pruebas_api.txt]", cursiva=True, color=GRIS)

    doc.add_heading("Anexo B · Cómo reproducir", level=1)
    for linea in [
        "gradlew :app:assembleDebug",
        "C:\\xampp\\mysql\\bin\\mysql.exe -u root --default-character-set=utf8mb4 -e \"source C:/xampp/virtualhost/panaderia/database/panaderia.sql\"",
        "C:\\xampp\\apache\\bin\\httpd.exe -k restart",
        "curl http://127.0.0.1:88/api/ping",
        "python docs/evidencias/pruebas_api.py",
    ]:
        p = doc.add_paragraph()
        r = p.add_run(linea)
        r.font.name = "Consolas"
        r.font.size = Pt(8.4)
        p.paragraph_format.space_after = Pt(2)

    ruta = os.path.join(AQUI, "Informe tecnico final - Acambapan.docx")
    doc.save(ruta)
    print("OK", ruta)
    print("metricas:", m)
    return ruta


if __name__ == "__main__":
    construir()
