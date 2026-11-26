"""
Pruebas de integración contra la Web API real de Acambapan (XAMPP).

Ejecuta comprobaciones de salud, sesión, permisos, catálogo, pedidos,
inventario, resumen, direcciones y notificaciones, e imprime un resumen.
No modifica datos fuera de lo que crea y deja todo marcado en la salida.
"""
import json
import sys
import time
import urllib.error
import urllib.request

sys.stdout.reconfigure(encoding="utf-8")
BASE = "http://127.0.0.1:88/api"

resultados = []


def llamar(metodo, ruta, cuerpo=None, token=None):
    datos = json.dumps(cuerpo).encode("utf-8") if cuerpo is not None else None
    peticion = urllib.request.Request(BASE + ruta, data=datos, method=metodo)
    peticion.add_header("Content-Type", "application/json")
    if token:
        peticion.add_header("Authorization", "Bearer " + token)
    try:
        with urllib.request.urlopen(peticion, timeout=15) as respuesta:
            texto = respuesta.read().decode("utf-8")
            return respuesta.status, (json.loads(texto) if texto else {})
    except urllib.error.HTTPError as error:
        texto = error.read().decode("utf-8")
        try:
            return error.code, json.loads(texto) if texto else {}
        except json.JSONDecodeError:
            return error.code, {"crudo": texto[:200]}
    except Exception as error:  # noqa: BLE001
        return 0, {"error_red": str(error)}


def comprobar(nombre, condicion, detalle=""):
    resultados.append((nombre, bool(condicion), detalle))
    marca = "PASA" if condicion else "FALLA"
    print(f"[{marca}] {nombre}" + (f"  ->  {detalle}" if detalle else ""))


def main():
    print("=" * 78)
    print("PRUEBAS DE INTEGRACIÓN · API Acambapan · " + time.strftime("%Y-%m-%d %H:%M"))
    print("=" * 78)

    # 1. Salud
    estado, cuerpo = llamar("GET", "/ping")
    comprobar("GET /ping responde 200", estado == 200, f"HTTP {estado}")
    comprobar("MySQL responde ok", cuerpo.get("db") == "ok", str(cuerpo.get("db")))

    # 2. Catálogo público
    estado, cuerpo = llamar("GET", "/categorias")
    comprobar("GET /categorias", estado == 200 and len(cuerpo.get("categorias") or []) >= 4,
              f"{len(cuerpo.get('categorias') or [])} categorías")

    estado, cuerpo = llamar("GET", "/productos")
    productos = cuerpo.get("productos") or []
    comprobar("GET /productos trae el catálogo", estado == 200 and len(productos) >= 12,
              f"{len(productos)} productos")

    estado, cuerpo = llamar("GET", "/productos/1")
    comprobar("Ficha de producto con multimedia y tandas",
              estado == 200 and cuerpo.get("producto") is not None
              and len(cuerpo.get("multimedia") or []) >= 1
              and len(cuerpo.get("tandas") or []) >= 1,
              f"multimedia={len(cuerpo.get('multimedia') or [])} tandas={len(cuerpo.get('tandas') or [])}")

    estado, _ = llamar("GET", "/productos/99999")
    comprobar("Producto inexistente devuelve 404", estado == 404, f"HTTP {estado}")

    # 3. Sesión
    estado, cuerpo = llamar("POST", "/auth/login",
                            {"correo": "admin@pantradicion.com", "password": "123456"})
    token_admin = cuerpo.get("token")
    comprobar("Login ADMIN entrega token", estado == 200 and bool(token_admin), f"HTTP {estado}")

    estado, _ = llamar("POST", "/auth/login",
                       {"correo": "admin@pantradicion.com", "password": "incorrecta"})
    comprobar("Login con contraseña incorrecta devuelve 401", estado == 401, f"HTTP {estado}")

    estado, cuerpo = llamar("POST", "/auth/login",
                            {"correo": "s23120227@alumnos.itsur.edu.mx", "password": "123456"})
    token_cliente = cuerpo.get("token")
    id_cliente = (cuerpo.get("usuario") or {}).get("id")
    comprobar("Login CLIENTE entrega token", estado == 200 and bool(token_cliente), f"HTTP {estado}")

    # 4. Permisos
    estado, _ = llamar("GET", "/resumen")
    comprobar("GET /resumen sin token devuelve 401", estado == 401, f"HTTP {estado}")

    estado, _ = llamar("PUT", "/insumos/1", {"nombre": "Harina", "stock": 1, "minimo": 1, "unidad": "kg"})
    comprobar("PUT /insumos sin token devuelve 401", estado == 401, f"HTTP {estado}")

    estado, _ = llamar("GET", "/resumen", token=token_cliente)
    comprobar("GET /resumen con token CLIENTE devuelve 403", estado == 403, f"HTTP {estado}")

    estado, cuerpo = llamar("GET", "/resumen", token=token_admin)
    resumen = cuerpo.get("resumen") or {}
    comprobar("GET /resumen con token ADMIN", estado == 200 and "pedidosHoy" in resumen,
              f"pedidosHoy={resumen.get('pedidosHoy')} ventasHoy={resumen.get('ventasHoy')}")

    # 5. Inventario público de lectura
    estado, cuerpo = llamar("GET", "/insumos")
    comprobar("GET /insumos es público", estado == 200 and len(cuerpo.get("insumos") or []) > 0,
              f"{len(cuerpo.get('insumos') or [])} insumos")

    # 6. Pedidos
    estado, cuerpo = llamar("GET", "/productos")
    catalogo = {p["id"]: p for p in (cuerpo.get("productos") or [])}
    pid = sorted(catalogo)[0]
    folio = "PRUEBA-" + time.strftime("%H%M%S")

    pedido = {
        "folio": folio,
        "clienteId": id_cliente,
        "clienteNombre": "Prueba Integración",
        "clienteTelefono": "4451000000",
        "tipoEntrega": "A domicilio",
        "direccionEntrega": "Calle de prueba 1",
        "tipoEmpaque": "Bolsa ecológica de papel",
        "requiereRebanado": False,
        "costoEnvio": 30,
        "items": [{"productoId": pid, "cantidad": 2}],
    }
    estado, cuerpo = llamar("POST", "/pedidos", pedido)
    creado = cuerpo.get("pedido") or {}
    comprobar("POST /pedidos crea el pedido", estado in (200, 201) and creado.get("folio") == folio,
              f"HTTP {estado}, folio={creado.get('folio')}, total={creado.get('total')}")

    total_esperado = round(float(catalogo[pid]["precio"]) * 2 + 30, 2)
    comprobar("El servidor calcula subtotal y total",
              abs(float(creado.get("total") or 0) - total_esperado) < 0.01,
              f"total={creado.get('total')} esperado={total_esperado}")

    estado, cuerpo = llamar("POST", "/pedidos", pedido)
    comprobar("Reenviar el mismo folio no duplica (idempotencia)",
              estado == 200 and (cuerpo.get("pedido") or {}).get("folio") == folio,
              f"HTTP {estado}")

    estado, cuerpo = llamar("GET", "/pedidos?clienteId=" + str(id_cliente))
    repetidos = [p for p in (cuerpo.get("pedidos") or []) if p.get("folio") == folio]
    comprobar("El folio aparece una sola vez en la lista", len(repetidos) == 1,
              f"{len(repetidos)} coincidencias")

    estado, cuerpo = llamar("PUT", f"/pedidos/{folio}/estado", {"estado": "EN_PREPARACION"}, token_admin)
    comprobar("Cambio de estado válido RECIBIDO -> EN_PREPARACION", estado == 200, f"HTTP {estado}")

    estado, cuerpo = llamar("PUT", f"/pedidos/{folio}/estado", {"estado": "ENTREGADO"}, token_admin)
    comprobar("Salto inválido devuelve 409", estado == 409, f"HTTP {estado}")

    # 7. Direcciones y notificaciones del cliente
    estado, cuerpo = llamar("GET", "/direcciones", token=token_cliente)
    comprobar("GET /direcciones con token", estado == 200,
              f"{len(cuerpo.get('direcciones') or [])} direcciones")

    estado, cuerpo = llamar("GET", "/notificaciones?clienteId=" + str(id_cliente))
    comprobar("GET /notificaciones del cliente", estado == 200,
              f"{len(cuerpo.get('notificaciones') or [])} avisos")

    # 8. Registro duplicado
    estado, _ = llamar("POST", "/auth/registro", {
        "nombre": "Duplicado", "correo": "admin@pantradicion.com",
        "password": "123456", "telefono": "4451111111", "direccion": "x",
    })
    comprobar("Registro con correo existente devuelve 409", estado in (409, 400), f"HTTP {estado}")

    # Resumen
    pasan = sum(1 for _, ok, _ in resultados if ok)
    print("-" * 78)
    print(f"RESULTADO: {pasan}/{len(resultados)} comprobaciones en verde")
    fallidas = [n for n, ok, _ in resultados if not ok]
    if fallidas:
        print("Fallaron: " + ", ".join(fallidas))
    print("-" * 78)
    return 0 if not fallidas else 1


if __name__ == "__main__":
    sys.exit(main())
