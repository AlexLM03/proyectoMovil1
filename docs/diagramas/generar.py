"""
Generador de diagramas de Acambapan.

Dibuja con matplotlib los diagramas que pide la entrega final:
  1. Modelo entidad-relacion de la base de datos (11 tablas)
  2. Diagrama de paquetes (UML)
  3. Diagrama de componentes (UML)
  4. Diagrama de despliegue (UML)
  5. Mapa de navegacion (NavHost)

Uso:  python generar.py
Salida: PNG a 200 dpi en la misma carpeta.
"""
import os
import matplotlib

matplotlib.use("Agg")
import matplotlib.pyplot as plt
from matplotlib.patches import FancyBboxPatch, FancyArrowPatch, Rectangle

AQUI = os.path.dirname(os.path.abspath(__file__))

TINTA = "#1f2937"
GRIS = "#6b7280"
CREMA = "#FDF6EC"
CAFE = "#8B5E3C"
CANELA = "#C98B5E"
VERDE = "#3F7D5A"
AZUL = "#3B5B8C"
ROJO = "#A63D3D"


def lienzo(ancho, alto, xmax, ymax):
    fig, ax = plt.subplots(figsize=(ancho, alto))
    ax.set_xlim(0, xmax)
    ax.set_ylim(0, ymax)
    ax.axis("off")
    return fig, ax


def caja(ax, x, y, w, h, titulo, campos=(), color=CAFE, fondo="white", fuente=7.2):
    """Caja UML: banda de titulo + lista de campos."""
    ax.add_patch(
        FancyBboxPatch(
            (x, y), w, h,
            boxstyle="round,pad=0.15,rounding_size=0.8",
            linewidth=1.4, edgecolor=color, facecolor=fondo, zorder=2,
        )
    )
    alto_titulo = 2.6
    ax.add_patch(
        Rectangle(
            (x, y + h - alto_titulo), w, alto_titulo,
            facecolor=color, edgecolor=color, zorder=3,
        )
    )
    ax.text(
        x + w / 2, y + h - alto_titulo / 2, titulo,
        ha="center", va="center", fontsize=fuente + 1.1,
        fontweight="bold", color="white", zorder=4,
    )
    for i, campo in enumerate(campos):
        ax.text(
            x + 0.7, y + h - alto_titulo - 1.5 - i * 1.45, campo,
            ha="left", va="center", fontsize=fuente, color=TINTA, zorder=4,
            family="DejaVu Sans",
        )


def flecha(ax, p1, p2, etiqueta="", color=GRIS, estilo="-|>", rad=0.0,
           tam=7.5, desplazamiento=0.0, negrita=False):
    ax.add_patch(
        FancyArrowPatch(
            p1, p2, arrowstyle=estilo, mutation_scale=tam,
            linewidth=1.25, color=color, zorder=1,
            connectionstyle=f"arc3,rad={rad}",
            shrinkA=2, shrinkB=2,
        )
    )
    if etiqueta:
        mx = (p1[0] + p2[0]) / 2 + desplazamiento
        my = (p1[1] + p2[1]) / 2 + desplazamiento * 0.4
        ax.text(
            mx, my, etiqueta, fontsize=6.6, color=color, ha="center",
            va="center", style="italic",
            fontweight="bold" if negrita else "normal",
            bbox=dict(boxstyle="round,pad=0.18", facecolor="white",
                      edgecolor="none", alpha=0.92),
            zorder=5,
        )


# ---------------------------------------------------------------------------
# 1. Modelo entidad-relacion
# ---------------------------------------------------------------------------

def modelo_er():
    fig, ax = lienzo(17, 11, 170, 110)
    ax.text(85, 106.5, "Acambapan · Modelo entidad-relación (pantradicion_db)",
            ha="center", fontsize=15, fontweight="bold", color=CAFE)
    ax.text(85, 103.2,
            "11 tablas · InnoDB · utf8mb4 · copia espejo en Room (SQLite) dentro del teléfono",
            ha="center", fontsize=8.6, color=GRIS)

    W, H = 30, 15.5
    # (x, y, titulo, campos, color)
    tablas = {
        "usuarios": (12, 52, "usuarios", [
            "PK  id",
            "     nombre, correo, telefono",
            "     password_hash",
            "     rol ENUM(CLIENTE,ADMIN,REPARTIDOR)",
            "     metodo_pago_preferido",
            "     activo, creado_en",
        ], CAFE),
        "direcciones": (12, 82, "direcciones", [
            "PK  id",
            "FK  usuario_id",
            "     alias, calle, referencias",
            "     predeterminada",
            "     creado_en",
        ], CAFE),
        "notificaciones": (12, 22, "notificaciones", [
            "PK  id",
            "FK  usuario_id",
            "FK  pedido_folio",
            "     titulo, mensaje",
            "     leida, creado_en",
        ], CAFE),
        "pedidos": (62, 50, "pedidos", [
            "PK  folio",
            "FK  cliente_id",
            "FK  repartidor_id",
            "     cliente_nombre, cliente_telefono",
            "     tipo_entrega, direccion_entrega",
            "     tipo_empaque, metodo_pago",
            "     requiere_rebanado, notas_panadero",
            "     subtotal, costo_envio, total",
            "     estado, tiempo_estimado, fecha_pedido",
        ], ROJO),
        "detalles_pedido": (62, 18, "detalles_pedido", [
            "PK  id",
            "FK  pedido_folio",
            "FK  producto_id",
            "     cantidad, precio_unitario",
            "     subtotal (columna generada)",
        ], ROJO),
        "productos": (112, 50, "productos", [
            "PK  id",
            "FK  categoria_id",
            "     nombre, descripcion",
            "     precio, emoji",
            "     disponible, creado_en",
        ], VERDE),
        "categorias": (112, 82, "categorias", [
            "PK  id",
            "     nombre (UNIQUE)",
            "     icono_emoji, activo",
        ], VERDE),
        "tandas_horneado": (140, 82, "tandas_horneado", [
            "PK  id",
            "FK  producto_id",
            "     hora_salida",
            "     piezas_estimadas",
            "     piezas_disponibles",
            "     estado, dias_semana",
        ], VERDE),
        "producto_multimedia": (140, 50, "producto_multimedia", [
            "PK  id",
            "FK  producto_id",
            "     tipo ENUM(imagen,video)",
            "     url, orden",
        ], VERDE),
        "producto_insumos": (112, 18, "producto_insumos", [
            "PK  (producto_id, insumo_id)",
            "FK  producto_id",
            "FK  insumo_id",
            "     cantidad_requerida",
        ], AZUL),
        "insumos": (140, 18, "insumos", [
            "PK  id",
            "     nombre (UNIQUE)",
            "     stock, minimo, unidad",
            "     actualizado_en",
        ], AZUL),
    }

    for titulo, (x, y, t, campos, color) in tablas.items():
        caja(ax, x, y, W, H, t, campos, color=color)

    def borde(nombre, lado):
        x, y, *_ = tablas[nombre]
        if lado == "arriba":
            return (x + W / 2, y + H)
        if lado == "abajo":
            return (x + W / 2, y)
        if lado == "izq":
            return (x, y + H / 2)
        return (x + W, y + H / 2)

    # usuarios -> direcciones / notificaciones / pedidos
    flecha(ax, borde("usuarios", "arriba"), borde("direcciones", "abajo"),
           "1 ── N", rad=-0.05)
    flecha(ax, borde("usuarios", "abajo"), borde("notificaciones", "arriba"),
           "1 ── N", rad=0.05)
    flecha(ax, borde("usuarios", "der"), borde("pedidos", "izq"),
           "1 ── N  (cliente / repartidor)", rad=0)
    # pedidos -> detalles / notificaciones
    flecha(ax, borde("pedidos", "abajo"), borde("detalles_pedido", "arriba"),
           "1 ── N", rad=0)
    flecha(ax, borde("pedidos", "izq"), (12 + W, 22 + H),
           "1 ── N", rad=0.12, desplazamiento=-4)
    # productos -> resto
    flecha(ax, borde("productos", "arriba"), borde("categorias", "abajo"),
           "N ── 1", rad=0)
    flecha(ax, borde("productos", "der"), borde("producto_multimedia", "izq"),
           "1 ── N", rad=0)
    flecha(ax, borde("productos", "der"), borde("tandas_horneado", "izq"),
           "1 ── N", rad=0.18, desplazamiento=3)
    flecha(ax, borde("productos", "abajo"), borde("producto_insumos", "arriba"),
           "1 ── N", rad=0)
    flecha(ax, borde("productos", "abajo"), borde("detalles_pedido", "der"),
           "1 ── N", rad=-0.12)
    # insumos -> producto_insumos
    flecha(ax, borde("insumos", "izq"), borde("producto_insumos", "der"),
           "1 ── N", rad=0)

    ax.text(85, 4.5,
            "PK = llave primaria · FK = llave foránea · ENUM = valores cerrados definidos en el esquema\n"
            "Las columnas calculadas (detalles_pedido.subtotal) y los índices viven también en la copia Room",
            ha="center", fontsize=7.4, color=GRIS)
    fig.savefig(os.path.join(AQUI, "01_modelo_er.png"), dpi=200,
                bbox_inches="tight", facecolor="white")
    plt.close(fig)
    print("01_modelo_er.png")


# ---------------------------------------------------------------------------
# 2. Paquetes
# ---------------------------------------------------------------------------

def paquetes():
    fig, ax = lienzo(13, 10, 130, 100)
    ax.text(65, 97, "Acambapan · Diagrama de paquetes", ha="center",
            fontsize=15, fontweight="bold", color=CAFE)

    # Marco raiz
    ax.add_patch(FancyBboxPatch((3, 3), 124, 88,
                                boxstyle="round,pad=0.3,rounding_size=1.2",
                                linewidth=1.6, edgecolor=CAFE, facecolor=CREMA))
    ax.text(6, 87.5, "com.devnoway.posproyect", fontsize=10.5,
            fontweight="bold", color=CAFE)

    def grupo(x, y, w, h, titulo, color, items, fuente=7.6):
        ax.add_patch(FancyBboxPatch((x, y), w, h,
                                    boxstyle="round,pad=0.25,rounding_size=0.9",
                                    linewidth=1.3, edgecolor=color,
                                    facecolor="white", zorder=2))
        ax.text(x + 1.2, y + h - 2.2, titulo, fontsize=8.8,
                fontweight="bold", color=color, zorder=3)
        for i, item in enumerate(items):
            ax.text(x + 2.0, y + h - 5.4 - i * 3.1, item, fontsize=fuente,
                    color=TINTA, zorder=3)

    grupo(7, 72, 30, 13, "ui.screens", CAFE, ["15 pantallas Compose"], 7.4)
    grupo(40, 72, 30, 13, "ui.components", CAFE, ["Botones, tarjetas, marcos"], 7.4)
    grupo(73, 72, 24, 13, "ui.viewmodel", CAFE, ["13 ViewModel · StateFlow"], 7.4)
    grupo(100, 72, 24, 13, "ui.theme", CAFE, ["Color, Type, Theme"], 7.4)

    grupo(7, 47, 30, 21, "navigation", AZUL,
          ["Rutas (constantes)", "NavegacionAcambapan", "  NavHost + guardas de rol"])
    grupo(40, 47, 27, 21, "di", AZUL,
          ["ContenedorApp", "  inyección manual", "  (Application → Activity)"])
    grupo(70, 47, 27, 21, "data.repository", VERDE,
          ["Catalogo · Pedido · Carrito", "Insumo · Direccion", "Sesion · Notificacion"])
    grupo(100, 47, 24, 21, "data.sesion", VERDE,
          ["AlmacenSesion", "  token + perfil", "data.conectividad"])

    grupo(7, 22, 30, 21, "data.local", VERDE,
          ["entidades (11 @Entity)", "daos (8 interfaces)", "AcambapanDatabase", "  Room + migraciones"])
    grupo(40, 22, 30, 21, "data.remote", VERDE,
          ["ApiCliente (Retrofit)", "ApiServicio (contrato)", "dto (Gson)", "Resultado / ejecutarRed"])
    grupo(73, 22, 24, 21, "data.modelo", VERDE,
          ["Producto, Pedido", "Usuario, Direccion", "Insumo, Notificacion"])
    grupo(100, 22, 24, 21, "data.mapeadores", VERDE,
          ["DTO ↔ Entity ↔ Modelo"])

    grupo(7, 6, 60, 12, "Application / Activity", ROJO,
          ["AcambapanApplication → crea ContenedorApp", "MainActivity → setContent { AcambapanApp }"], 7.4)
    grupo(73, 6, 51, 12, "Entradas del sistema", GRIS,
          ["MainActivity (única Activity)", "UI 100% Jetpack Compose"], 7.4)

    # Dependencias entre capas
    flecha(ax, (65, 72), (65, 68.4), "usa", AZUL, rad=0, tam=8)
    flecha(ax, (84, 72), (84, 68.4), "crea VMs con", AZUL, rad=0, tam=8)
    flecha(ax, (53, 47), (53, 43.4), "inyecta repos en", VERDE, rad=0, tam=8)
    flecha(ax, (84, 47), (84, 43.4), "delega", VERDE, rad=0, tam=8)
    flecha(ax, (22, 47), (22, 43.4), "navega", AZUL, rad=0, tam=8)
    flecha(ax, (37, 32), (40, 32), "espejo", GRIS, rad=0, tam=8)
    flecha(ax, (70, 32), (73, 32), "traduce", GRIS, rad=0, tam=8)
    flecha(ax, (61, 18), (61, 22), "detecta red", GRIS, rad=0, tam=8)
    flecha(ax, (40, 12), (40, 18), "arranca", ROJO, rad=0, tam=8)

    fig.savefig(os.path.join(AQUI, "02_paquetes.png"), dpi=200,
                bbox_inches="tight", facecolor="white")
    plt.close(fig)
    print("02_paquetes.png")


# ---------------------------------------------------------------------------
# 3. Componentes
# ---------------------------------------------------------------------------

def componentes():
    fig, ax = lienzo(14, 9, 140, 90)
    ax.text(70, 87, "Acambapan · Diagrama de componentes", ha="center",
            fontsize=15, fontweight="bold", color=CAFE)

    # Frontera del telefono
    ax.add_patch(FancyBboxPatch((3, 30), 78, 50,
                                boxstyle="round,pad=0.4,rounding_size=1.5",
                                linewidth=1.8, edgecolor=CAFE, facecolor=CREMA,
                                linestyle="--"))
    ax.text(6, 77.5, "«device» Teléfono / emulador Android", fontsize=9.5,
            fontweight="bold", color=CAFE)

    caja(ax, 7, 62, 32, 12, "UI · Compose", [
        "15 pantallas (ui.screens)",
        "Componentes reutilizables",
        "MarcoCliente / MarcoAdmin",
    ], CAFE, fuente=7.0)
    caja(ax, 45, 62, 32, 12, "ViewModel", [
        "13 ViewModel con StateFlow",
        "Ciclo de vida del Activity",
    ], CAFE, fuente=7.0)
    caja(ax, 7, 44, 32, 13, "Repository", [
        "CatalogoRepository",
        "PedidoRepository (cola offline)",
        "Sesion · Carrito · Insumo ...",
    ], VERDE, fuente=7.0)
    caja(ax, 45, 42, 32, 10, "Room (SQLite)", [
        "11 entidades · 8 DAOs",
        "AcambapanDatabase",
    ], VERDE, fuente=7.0)
    caja(ax, 45, 30.5, 32, 8.5, "Retrofit + OkHttp", [
        "ApiServicio (JSON/Gson)",
        "Resultado<T> / ejecutarRed",
    ], AZUL, fuente=7.0)
    caja(ax, 7, 31, 32, 9, "Sesión local", [
        "AlmacenSesion (token, rol)",
        "ObservadorConectividad",
    ], GRIS, fuente=7.0)

    # Servidor
    ax.add_patch(FancyBboxPatch((90, 30), 46, 50,
                                boxstyle="round,pad=0.4,rounding_size=1.5",
                                linewidth=1.8, edgecolor=AZUL, facecolor="#EEF3FA",
                                linestyle="--"))
    ax.text(93, 77.5, "«server» XAMPP + Cloudflare", fontsize=9.5,
            fontweight="bold", color=AZUL)
    caja(ax, 94, 62, 38, 12, "Web API · PHP 8 + PDO", [
        "index.php (router REST)",
        "Auth HMAC-SHA256 · roles",
        "JSON uniforme { ok, ... }",
    ], AZUL, fuente=7.0)
    caja(ax, 94, 44, 38, 13, "MySQL · pantradicion_db", [
        "11 tablas InnoDB utf8mb4",
        "Llaves foráneas y ENUM",
        "Índices por estado y cliente",
    ], ROJO, fuente=7.0)
    caja(ax, 94, 31, 38, 10, "Túnel HTTPS", [
        "acambapan.devnoway.com",
        "Cloudflare → localhost:88",
    ], GRIS, fuente=7.0)

    flecha(ax, (39, 68), (45, 68), "observa estado", CAFE, rad=0, tam=9)
    flecha(ax, (61, 62), (61, 55), "invoca", VERDE, rad=0, tam=9)
    flecha(ax, (39, 50), (45, 48), "lee/escribe", VERDE, rad=0, tam=9)
    flecha(ax, (39, 47), (45, 36), "consulta", AZUL, rad=0, tam=9)
    flecha(ax, (77, 35), (94, 35), "HTTPS / JSON", AZUL, rad=0, tam=9, negrita=True)
    flecha(ax, (113, 44), (113, 44.2), "", ROJO, rad=0, tam=9)
    flecha(ax, (94, 68), (94, 57), "SQL", ROJO, rad=0, tam=9)

    ax.text(70, 14,
            "Cadena de dependencias exigida:  Application → Activity → Compose → ViewModel → Repository → (Room | Retrofit)",
            ha="center", fontsize=8.4, color=TINTA)
    ax.text(70, 9.5,
            "La UI nunca toca la base ni la red: solo conoce modelos de dominio y ViewModels.",
            ha="center", fontsize=8.0, color=GRIS)

    fig.savefig(os.path.join(AQUI, "03_componentes.png"), dpi=200,
                bbox_inches="tight", facecolor="white")
    plt.close(fig)
    print("03_componentes.png")


# ---------------------------------------------------------------------------
# 4. Despliegue
# ---------------------------------------------------------------------------

def despliegue():
    fig, ax = lienzo(14, 8, 140, 80)
    ax.text(70, 76.5, "Acambapan · Diagrama de despliegue", ha="center",
            fontsize=15, fontweight="bold", color=CAFE)

    def nodo(x, y, w, h, estereotipo, titulo, lineas, color):
        ax.add_patch(FancyBboxPatch((x, y), w, h,
                                    boxstyle="round,pad=0.4,rounding_size=1.2",
                                    linewidth=1.7, edgecolor=color,
                                    facecolor="white"))
        ax.add_patch(Rectangle((x, y + h - 4.2), w, 4.2,
                               facecolor=color, edgecolor=color))
        ax.text(x + w / 2, y + h - 2.1, titulo, ha="center", va="center",
                fontsize=9.2, fontweight="bold", color="white")
        ax.text(x + w / 2, y + h - 6.0, estereotipo, ha="center",
                fontsize=7.6, color=color, style="italic")
        for i, l in enumerate(lineas):
            ax.text(x + 3.0, y + h - 9.6 - i * 3.4, l, fontsize=7.4,
                    color=TINTA, va="center")

    nodo(6, 44, 34, 26, "«device»", "Teléfono Android", [
        "App Acambapan (APK)",
        "Kotlin · Compose · Room",
        "minSdk 29 · target 37",
        "Almacenamiento local SQLite",
    ], CAFE)

    nodo(52, 44, 34, 26, "«node»", "Túnel Cloudflare", [
        "ch1984 → localhost:88",
        "acambapan.devnoway.com",
        "HTTPS público",
    ], GRIS)

    nodo(98, 44, 36, 26, "«node»", "Servidor XAMPP", [
        "Apache 2.4 (puerto 88)",
        "PHP 8 · API REST",
        "Windows 11 · C:\\xampp",
    ], AZUL)

    nodo(52, 6, 34, 24, "«node»", "MySQL / MariaDB", [
        "pantradicion_db",
        "11 tablas InnoDB",
        "usuario root (local)",
    ], ROJO)

    nodo(6, 6, 34, 24, "«artifact»", "Repositorio", [
        "github.com/AlexLM03/proyectoMovil1",
        "Gradle 9 · AGP 9.4",
        "APK debug / release",
    ], VERDE)

    flecha(ax, (40, 60), (52, 60), "HTTPS 443", AZUL, rad=0, tam=10, negrita=True)
    flecha(ax, (86, 60), (98, 60), "HTTP :88", AZUL, rad=0, tam=10, negrita=True)
    flecha(ax, (116, 44), (86, 30), "PDO / MySQL 3306", ROJO, rad=0.12, tam=10, negrita=True)
    flecha(ax, (23, 30), (23, 44), "instala APK", VERDE, rad=0, tam=10)

    ax.text(70, 2.0,
            "El teléfono no abre conexiones a MySQL: todo pasa por la Web API, que es la única que conoce las credenciales.",
            ha="center", fontsize=7.6, color=GRIS)

    fig.savefig(os.path.join(AQUI, "04_despliegue.png"), dpi=200,
                bbox_inches="tight", facecolor="white")
    plt.close(fig)
    print("04_despliegue.png")


# ---------------------------------------------------------------------------
# 5. Navegacion
# ---------------------------------------------------------------------------

def navegacion():
    fig, ax = lienzo(13, 9, 130, 90)
    ax.text(65, 87, "Acambapan · Grafo de navegación (NavHost)",
            ha="center", fontsize=15, fontweight="bold", color=CAFE)

    def pantalla(x, y, w, h, nombre, ruta, color):
        ax.add_patch(FancyBboxPatch((x, y), w, h,
                                    boxstyle="round,pad=0.25,rounding_size=1.0",
                                    linewidth=1.3, edgecolor=color,
                                    facecolor="white"))
        ax.text(x + w / 2, y + h - 2.6, nombre, ha="center", fontsize=8.6,
                fontweight="bold", color=color)
        ax.text(x + w / 2, y + 2.4, ruta, ha="center", fontsize=6.6,
                color=GRIS, style="italic")

    # Acceso
    pantalla(6, 62, 29, 11, "Login", "login · login_admin", ROJO)
    pantalla(6, 46, 29, 11, "Registro", "registro", ROJO)
    pantalla(6, 30, 29, 11, "AccesoRestringido", "(candado de rol)", ROJO)

    # Cliente
    pantalla(44, 68, 32, 11, "Catálogo", "catalogo", CAFE)
    pantalla(44, 51, 32, 11, "Detalle", "detalle_producto/{id}", CAFE)
    pantalla(44, 34, 32, 11, "Carrito y pago", "carrito", CAFE)
    pantalla(44, 17, 32, 11, "Seguimiento", "seguimiento", CAFE)
    pantalla(44, 2, 32, 11, "Perfil", "perfil", CAFE)

    # Cuenta
    pantalla(88, 62, 36, 11, "Direcciones", "direcciones", VERDE)
    pantalla(88, 46, 36, 11, "Métodos de pago", "metodos_pago", VERDE)
    pantalla(88, 30, 36, 11, "Notificaciones", "notificaciones", VERDE)
    pantalla(88, 14, 36, 11, "Ayuda", "ayuda", VERDE)

    # Admin
    pantalla(44, 84, 0.1, 0.1, "", "", "white")
    pantalla(88, 84, 36, 11, "Panel admin", "admin_panel · guarda", AZUL)
    pantalla(44, 84, 32, 11, "Dash · Pedidos · Inventario", "admin_dash / _pedidos / _inventario", AZUL)

    flecha(ax, (35, 67), (44, 72), "", ROJO, rad=0.1)
    flecha(ax, (20, 62), (20, 57), "", ROJO, rad=0)
    flecha(ax, (20, 46), (20, 41), "", ROJO, rad=0)
    flecha(ax, (35, 36), (44, 24), "login admin", ROJO, rad=0.15)
    flecha(ax, (60, 68), (60, 62), "", CAFE, rad=0)
    flecha(ax, (60, 51), (60, 45), "", CAFE, rad=0)
    flecha(ax, (60, 34), (60, 28), "", CAFE, rad=0)
    flecha(ax, (76, 22), (88, 67), "perfil → cuenta", VERDE, rad=-0.25)
    flecha(ax, (88, 67), (76, 7), "", VERDE, rad=0.2)
    flecha(ax, (76, 7), (88, 35), "", VERDE, rad=-0.2)
    flecha(ax, (76, 22), (88, 84), "modo admin", AZUL, rad=-0.3)
    flecha(ax, (106, 84), (106, 73), "", AZUL, rad=0)

    fig.savefig(os.path.join(AQUI, "05_navegacion.png"), dpi=200,
                bbox_inches="tight", facecolor="white")
    plt.close(fig)
    print("05_navegacion.png")


if __name__ == "__main__":
    modelo_er()
    paquetes()
    componentes()
    despliegue()
    navegacion()
    print("Listo: diagramas en", AQUI)
