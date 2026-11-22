<?php
declare(strict_types=1);

/**
 * API de la panadería Acambapan.
 *
 * Punto de entrada único: todas las rutas se resuelven aquí y siempre
 * responden JSON. La app Android consume esta API a través de Retrofit y
 * guarda una copia local en Room para funcionar sin conexión.
 *
 * URL base del emulador: http://10.0.2.2:88/api/
 */

require_once __DIR__ . '/src/Config.php';
require_once __DIR__ . '/src/Nucleo.php';
require_once __DIR__ . '/src/Auth.php';

cors();

$metodo = $_SERVER['REQUEST_METHOD'] ?? 'GET';
$ruta = rutaSolicitada();

try {
    $pdo = bd();
    despachar($metodo, $ruta, $pdo);
} catch (Throwable $error) {
    registrar_error($error);
    fallo(500, 'error_interno', 'Ocurrió un error en el servidor. Intenta de nuevo.');
}

// ---------------------------------------------------------------------------
// Router
// ---------------------------------------------------------------------------

function despachar(string $metodo, string $ruta, PDO $pdo): void
{
    // Salud
    if ($metodo === 'GET' && $ruta === '/ping') {
        $pdo->query('SELECT 1');
        responder([
            'ok' => true,
            'version' => VERSION_API,
            'hora' => date('c'),
            'db' => 'ok',
        ]);
    }

    // Sesión
    if ($metodo === 'POST' && $ruta === '/auth/login') {
        iniciarSesion($pdo);
    }
    if ($metodo === 'POST' && $ruta === '/auth/registro') {
        registrarCliente($pdo);
    }
    if ($metodo === 'GET' && $ruta === '/auth/me') {
        responder(['ok' => true, 'usuario' => usuarioDto(requerirUsuario())]);
    }

    // Catálogo
    if ($metodo === 'GET' && $ruta === '/categorias') {
        listarCategorias($pdo);
    }
    if ($metodo === 'GET' && $ruta === '/productos') {
        listarProductos($pdo);
    }
    if ($metodo === 'GET' && preg_match('#^/productos/(\d+)$#', $ruta, $coincidencias)) {
        verProducto($pdo, (int) $coincidencias[1]);
    }
    if ($metodo === 'GET' && $ruta === '/tandas') {
        listarTandas($pdo);
    }

    // Pedidos
    if ($metodo === 'GET' && $ruta === '/pedidos') {
        listarPedidos($pdo);
    }
    if ($metodo === 'POST' && $ruta === '/pedidos') {
        crearPedido($pdo);
    }
    if ($metodo === 'GET' && preg_match('#^/pedidos/([A-Za-z0-9\-]+)$#', $ruta, $coincidencias)) {
        $pedido = pedidoDto($pdo, $coincidencias[1]);
        if ($pedido === null) {
            fallo(404, 'no_encontrado', 'El pedido no existe.');
        }
        responder(['ok' => true, 'pedido' => $pedido]);
    }
    if ($metodo === 'PUT' && preg_match('#^/pedidos/([A-Za-z0-9\-]+)/estado$#', $ruta, $coincidencias)) {
        cambiarEstadoPedido($pdo, $coincidencias[1]);
    }
    if ($metodo === 'DELETE' && preg_match('#^/pedidos/([A-Za-z0-9\-]+)$#', $ruta, $coincidencias)) {
        cancelarPedido($pdo, $coincidencias[1]);
    }

    // Inventario
    if ($metodo === 'GET' && $ruta === '/insumos') {
        listarInsumos($pdo);
    }
    if ($metodo === 'POST' && $ruta === '/insumos') {
        crearInsumo($pdo);
    }
    if ($metodo === 'PUT' && preg_match('#^/insumos/(\d+)$#', $ruta, $coincidencias)) {
        actualizarInsumo($pdo, (int) $coincidencias[1]);
    }
    if ($metodo === 'DELETE' && preg_match('#^/insumos/(\d+)$#', $ruta, $coincidencias)) {
        eliminarInsumo($pdo, (int) $coincidencias[1]);
    }

    // Direcciones de entrega del cliente
    if ($metodo === 'GET' && $ruta === '/direcciones') {
        listarDirecciones($pdo);
    }
    if ($metodo === 'POST' && $ruta === '/direcciones') {
        crearDireccion($pdo);
    }
    if ($metodo === 'PUT' && preg_match('#^/direcciones/(\d+)$#', $ruta, $coincidencias)) {
        actualizarDireccion($pdo, (int) $coincidencias[1]);
    }
    if ($metodo === 'DELETE' && preg_match('#^/direcciones/(\d+)$#', $ruta, $coincidencias)) {
        eliminarDireccion($pdo, (int) $coincidencias[1]);
    }
    if ($metodo === 'PUT' && $ruta === '/auth/metodo-pago') {
        cambiarMetodoPago($pdo);
    }

    // Resumen y avisos
    if ($metodo === 'GET' && $ruta === '/resumen') {
        resumen($pdo);
    }
    if ($metodo === 'GET' && $ruta === '/notificaciones') {
        listarNotificaciones($pdo);
    }
    if ($metodo === 'PUT' && preg_match('#^/notificaciones/(\d+)/leida$#', $ruta, $coincidencias)) {
        requerirUsuario();
        $pdo->prepare('UPDATE notificaciones SET leida = 1 WHERE id = ?')
            ->execute([(int) $coincidencias[1]]);
        responder(['ok' => true]);
    }

    fallo(404, 'no_encontrado', "La ruta $metodo $ruta no existe.");
}

// ---------------------------------------------------------------------------
// Sesión
// ---------------------------------------------------------------------------

function iniciarSesion(PDO $pdo): void
{
    $datos = cuerpo();
    $identificador = trim((string) ($datos['correo'] ?? ''));
    $contrasena = (string) ($datos['password'] ?? '');

    if ($identificador === '' || $contrasena === '') {
        fallo(400, 'datos_incompletos', 'Escribe tu correo (o teléfono) y tu contraseña.');
    }

    $consulta = $pdo->prepare(
        'SELECT * FROM usuarios WHERE correo = :correo OR telefono = :telefono LIMIT 1'
    );
    $consulta->execute(['correo' => $identificador, 'telefono' => $identificador]);
    $usuario = $consulta->fetch();

    if (!$usuario) {
        usleep(300000);
        fallo(401, 'credenciales', 'El correo o la contraseña no son correctos.');
    }
    if (!(bool) $usuario['activo']) {
        fallo(403, 'cuenta_inactiva', 'Esta cuenta está desactivada. Contacta a la panadería.');
    }
    if (!verificarContrasena((string) $usuario['password_hash'], $contrasena)) {
        usleep(300000);
        fallo(401, 'credenciales', 'El correo o la contraseña no son correctos.');
    }

    // Migración silenciosa: el script original guarda "123456" en texto plano.
    if (!contrasenaHasheada((string) $usuario['password_hash'])) {
        $pdo->prepare('UPDATE usuarios SET password_hash = ? WHERE id = ?')
            ->execute([password_hash($contrasena, PASSWORD_DEFAULT), (int) $usuario['id']]);
    }

    $usuario['id'] = (int) $usuario['id'];
    $usuario['activo'] = (bool) $usuario['activo'];

    responder([
        'ok' => true,
        'token' => crearToken($usuario['id'], (string) $usuario['rol']),
        'usuario' => usuarioDto($usuario),
    ]);
}

function registrarCliente(PDO $pdo): void
{
    $datos = cuerpo();
    $nombre = trim((string) ($datos['nombre'] ?? ''));
    $correo = trim((string) ($datos['correo'] ?? ''));
    $contrasena = (string) ($datos['password'] ?? '');
    $telefono = trim((string) ($datos['telefono'] ?? ''));
    $direccion = trim((string) ($datos['direccion'] ?? ''));

    if ($nombre === '' || $correo === '' || $telefono === '' || $direccion === '') {
        fallo(400, 'datos_incompletos', 'Completa nombre, correo, teléfono y dirección.');
    }
    if (!filter_var($correo, FILTER_VALIDATE_EMAIL)) {
        fallo(400, 'correo_invalido', 'El correo electrónico no es válido.');
    }
    if (strlen($contrasena) < 6) {
        fallo(422, 'contrasena_corta', 'La contraseña debe tener al menos 6 caracteres.');
    }

    $existe = $pdo->prepare('SELECT id FROM usuarios WHERE correo = ? LIMIT 1');
    $existe->execute([$correo]);
    if ($existe->fetch()) {
        fallo(409, 'correo_duplicado', 'Ya existe una cuenta con ese correo.');
    }

    $insertar = $pdo->prepare(
        'INSERT INTO usuarios (nombre, correo, password_hash, telefono, direccion, rol, activo)
         VALUES (?, ?, ?, ?, ?, "CLIENTE", 1)'
    );
    $insertar->execute([
        $nombre,
        $correo,
        password_hash($contrasena, PASSWORD_DEFAULT),
        $telefono,
        $direccion,
    ]);
    $id = (int) $pdo->lastInsertId();

    $consulta = $pdo->prepare('SELECT * FROM usuarios WHERE id = ?');
    $consulta->execute([$id]);
    $usuario = $consulta->fetch();
    $usuario['id'] = (int) $usuario['id'];
    $usuario['activo'] = (bool) $usuario['activo'];

    responder([
        'ok' => true,
        'token' => crearToken($usuario['id'], (string) $usuario['rol']),
        'usuario' => usuarioDto($usuario),
    ], 201);
}

function usuarioDto(array $usuario): array
{
    return [
        'id' => (int) $usuario['id'],
        'nombre' => (string) $usuario['nombre'],
        'correo' => (string) $usuario['correo'],
        'telefono' => (string) $usuario['telefono'],
        'direccion' => (string) $usuario['direccion'],
        'rol' => (string) $usuario['rol'],
        'metodoPagoPreferido' => (string) ($usuario['metodo_pago_preferido'] ?? 'EFECTIVO'),
        'activo' => (bool) $usuario['activo'],
    ];
}

// ---------------------------------------------------------------------------
// Catálogo
// ---------------------------------------------------------------------------

function listarCategorias(PDO $pdo): void
{
    $filas = $pdo->query(
        'SELECT id, nombre, icono_emoji, activo FROM categorias WHERE activo = 1 ORDER BY id'
    )->fetchAll();

    $categorias = array_map(static fn(array $fila): array => [
        'id' => (int) $fila['id'],
        'nombre' => (string) $fila['nombre'],
        'iconoEmoji' => (string) $fila['icono_emoji'],
        'activo' => (bool) $fila['activo'],
    ], $filas);

    responder(['ok' => true, 'categorias' => $categorias]);
}

function listarProductos(PDO $pdo): void
{
    $condiciones = [];
    $parametros = [];

    $categoriaId = isset($_GET['categoriaId']) && $_GET['categoriaId'] !== ''
        ? (int) $_GET['categoriaId']
        : null;
    if ($categoriaId !== null && $categoriaId > 0) {
        $condiciones[] = 'p.categoria_id = :categoria';
        $parametros['categoria'] = $categoriaId;
    }

    $buscar = trim((string) ($_GET['buscar'] ?? ''));
    if ($buscar !== '') {
        $condiciones[] = '(p.nombre LIKE :buscarNombre OR p.descripcion LIKE :buscarDescripcion)';
        $parametros['buscarNombre'] = '%' . $buscar . '%';
        $parametros['buscarDescripcion'] = '%' . $buscar . '%';
    }

    $incluirAgotados = (string) ($_GET['incluirAgotados'] ?? '0') === '1';
    if (!$incluirAgotados) {
        $condiciones[] = 'p.disponible = 1';
    }

    $sql = 'SELECT p.id, p.categoria_id, p.nombre, p.descripcion, p.precio, p.emoji, p.disponible,
                   c.nombre AS categoria_nombre
            FROM productos p
            LEFT JOIN categorias c ON c.id = p.categoria_id';
    if ($condiciones !== []) {
        $sql .= ' WHERE ' . implode(' AND ', $condiciones);
    }
    $sql .= ' ORDER BY p.categoria_id, p.id';

    $consulta = $pdo->prepare($sql);
    $consulta->execute($parametros);
    $productos = array_map('productoDto', $consulta->fetchAll());

    responder(['ok' => true, 'productos' => $productos]);
}

function verProducto(PDO $pdo, int $id): void
{
    $consulta = $pdo->prepare(
        'SELECT p.id, p.categoria_id, p.nombre, p.descripcion, p.precio, p.emoji, p.disponible,
                c.nombre AS categoria_nombre
         FROM productos p
         LEFT JOIN categorias c ON c.id = p.categoria_id
         WHERE p.id = ?'
    );
    $consulta->execute([$id]);
    $fila = $consulta->fetch();
    if (!$fila) {
        fallo(404, 'no_encontrado', 'El producto no existe.');
    }

    $multimedia = $pdo->prepare(
        'SELECT id, producto_id, tipo, url, orden FROM producto_multimedia WHERE producto_id = ? ORDER BY orden'
    );
    $multimedia->execute([$id]);

    $tandas = $pdo->prepare(
        'SELECT t.id, t.producto_id, p.nombre AS producto_nombre, t.hora_salida, t.piezas_estimadas,
                t.piezas_disponibles, t.estado, t.dias_semana
         FROM tandas_horneado t
         JOIN productos p ON p.id = t.producto_id
         WHERE t.producto_id = ?
         ORDER BY t.hora_salida'
    );
    $tandas->execute([$id]);

    responder([
        'ok' => true,
        'producto' => productoDto($fila),
        'multimedia' => array_map(static fn(array $m): array => [
            'id' => (int) $m['id'],
            'productoId' => (int) $m['producto_id'],
            'tipo' => (string) $m['tipo'],
            'url' => (string) $m['url'],
            'orden' => (int) $m['orden'],
        ], $multimedia->fetchAll()),
        'tandas' => array_map('tandaDto', $tandas->fetchAll()),
    ]);
}

function productoDto(array $fila): array
{
    return [
        'id' => (int) $fila['id'],
        'categoriaId' => (int) $fila['categoria_id'],
        'categoriaNombre' => isset($fila['categoria_nombre']) ? (string) $fila['categoria_nombre'] : null,
        'nombre' => (string) $fila['nombre'],
        'descripcion' => (string) $fila['descripcion'],
        'precio' => (float) $fila['precio'],
        'emoji' => (string) $fila['emoji'],
        'disponible' => (bool) $fila['disponible'],
    ];
}

function tandaDto(array $fila): array
{
    return [
        'id' => (int) $fila['id'],
        'productoId' => (int) $fila['producto_id'],
        'productoNombre' => (string) $fila['producto_nombre'],
        'horaSalida' => (string) $fila['hora_salida'],
        'piezasEstimadas' => (int) $fila['piezas_estimadas'],
        'piezasDisponibles' => (int) $fila['piezas_disponibles'],
        'estado' => (string) $fila['estado'],
        'diasSemana' => (string) $fila['dias_semana'],
    ];
}

function listarTandas(PDO $pdo): void
{
    $filas = $pdo->query(
        'SELECT t.id, t.producto_id, p.nombre AS producto_nombre, t.hora_salida, t.piezas_estimadas,
                t.piezas_disponibles, t.estado, t.dias_semana
         FROM tandas_horneado t
         JOIN productos p ON p.id = t.producto_id
         ORDER BY t.hora_salida'
    )->fetchAll();

    responder(['ok' => true, 'tandas' => array_map('tandaDto', $filas)]);
}

// ---------------------------------------------------------------------------
// Pedidos
// ---------------------------------------------------------------------------

function pedidoDto(PDO $pdo, string $folio): ?array
{
    $consulta = $pdo->prepare(
        'SELECT p.*, r.nombre AS repartidor_nombre, r.telefono AS repartidor_telefono
         FROM pedidos p
         LEFT JOIN usuarios r ON r.id = p.repartidor_id
         WHERE p.folio = ?'
    );
    $consulta->execute([$folio]);
    $fila = $consulta->fetch();
    if (!$fila) {
        return null;
    }

    $detalles = $pdo->prepare(
        'SELECT d.producto_id, d.cantidad, d.precio_unitario, d.subtotal, pr.nombre
         FROM detalles_pedido d
         JOIN productos pr ON pr.id = d.producto_id
         WHERE d.pedido_folio = ?
         ORDER BY d.id'
    );
    $detalles->execute([$folio]);

    return [
        'folio' => (string) $fila['folio'],
        'clienteId' => $fila['cliente_id'] !== null ? (int) $fila['cliente_id'] : null,
        'clienteNombre' => (string) $fila['cliente_nombre'],
        'clienteTelefono' => (string) $fila['cliente_telefono'],
        'tipoEntrega' => (string) $fila['tipo_entrega'],
        'direccionEntrega' => $fila['direccion_entrega'],
        'horaRecoleccion' => $fila['hora_recoleccion'],
        'tipoEmpaque' => (string) $fila['tipo_empaque'],
        'metodoPago' => (string) $fila['metodo_pago'],
        'requiereRebanado' => (bool) $fila['requiere_rebanado'],
        'notasPanadero' => $fila['notas_panadero'],
        'subtotal' => (float) $fila['subtotal'],
        'costoEnvio' => (float) $fila['costo_envio'],
        'total' => (float) $fila['total'],
        'estado' => (string) $fila['estado'],
        'repartidorId' => $fila['repartidor_id'] !== null ? (int) $fila['repartidor_id'] : null,
        'repartidorNombre' => $fila['repartidor_nombre'],
        'repartidorTelefono' => $fila['repartidor_telefono'],
        'tiempoEstimado' => (string) $fila['tiempo_estimado'],
        'fechaPedido' => (string) $fila['fecha_pedido'],
        'pendienteSync' => false,
        'detalles' => array_map(static fn(array $d): array => [
            'productoId' => (int) $d['producto_id'],
            'nombre' => (string) $d['nombre'],
            'cantidad' => (int) $d['cantidad'],
            'precioUnitario' => (float) $d['precio_unitario'],
            'subtotal' => (float) $d['subtotal'],
        ], $detalles->fetchAll()),
    ];
}

function listarPedidos(PDO $pdo): void
{
    $clienteId = isset($_GET['clienteId']) && $_GET['clienteId'] !== ''
        ? (int) $_GET['clienteId']
        : null;
    $estado = trim((string) ($_GET['estado'] ?? ''));
    $limite = min(200, max(1, (int) ($_GET['limite'] ?? 100)));

    if ($clienteId === null && $estado === '') {
        requerirAdmin();
    }

    $condiciones = [];
    $parametros = [];
    if ($clienteId !== null) {
        $condiciones[] = 'cliente_id = :cliente';
        $parametros['cliente'] = $clienteId;
    }
    if ($estado !== '') {
        $condiciones[] = 'estado = :estado';
        $parametros['estado'] = $estado;
    }

    $sql = 'SELECT folio FROM pedidos';
    if ($condiciones !== []) {
        $sql .= ' WHERE ' . implode(' AND ', $condiciones);
    }
    $sql .= ' ORDER BY fecha_pedido DESC LIMIT ' . $limite;

    $consulta = $pdo->prepare($sql);
    $consulta->execute($parametros);

    $pedidos = [];
    foreach ($consulta->fetchAll() as $fila) {
        $pedido = pedidoDto($pdo, (string) $fila['folio']);
        if ($pedido !== null) {
            $pedidos[] = $pedido;
        }
    }

    responder(['ok' => true, 'pedidos' => $pedidos]);
}

function siguienteFolio(PDO $pdo): string
{
    $fila = $pdo->query(
        "SELECT MAX(CAST(SUBSTRING(folio, 5) AS UNSIGNED)) AS ultimo
         FROM pedidos WHERE folio LIKE 'ACB-%'"
    )->fetch();
    $siguiente = (int) ($fila['ultimo'] ?? 1040) + 1;
    return sprintf('ACB-%04d', $siguiente);
}

function crearPedido(PDO $pdo): void
{
    $datos = cuerpo();
    $items = $datos['items'] ?? null;

    if (!is_array($items) || $items === []) {
        fallo(400, 'sin_productos', 'El pedido necesita al menos un producto.');
    }

    $folio = trim((string) ($datos['folio'] ?? ''));

    // Idempotencia: si la app reintenta el envío de un pedido que ya llegó,
    // se devuelve el mismo pedido en lugar de duplicarlo.
    if ($folio !== '') {
        $existente = pedidoDto($pdo, $folio);
        if ($existente !== null) {
            responder(['ok' => true, 'pedido' => $existente]);
        }
    } else {
        $folio = siguienteFolio($pdo);
    }

    $tipoEntrega = (string) ($datos['tipoEntrega'] ?? 'A domicilio');
    if (!in_array($tipoEntrega, ['A domicilio', 'Para recoger'], true)) {
        fallo(422, 'tipo_entrega', 'El tipo de entrega no es válido.');
    }
    $tipoEmpaque = (string) ($datos['tipoEmpaque'] ?? 'Bolsa ecológica de papel');
    if (!in_array($tipoEmpaque, ['Bolsa ecológica de papel', 'Caja tradicional de cartón'], true)) {
        fallo(422, 'tipo_empaque', 'El tipo de empaque no es válido.');
    }
    $metodoPago = (string) ($datos['metodoPago'] ?? 'EFECTIVO');
    if (!in_array($metodoPago, ['EFECTIVO', 'TARJETA_TERMINAL', 'TRANSFERENCIA'], true)) {
        fallo(422, 'metodo_pago', 'El método de pago no es válido.');
    }

    $detalles = [];
    $subtotal = 0.0;
    $consultaProducto = $pdo->prepare('SELECT nombre, precio FROM productos WHERE id = ?');

    foreach ($items as $item) {
        if (!is_array($item)) {
            fallo(400, 'item_invalido', 'Cada línea del pedido debe ser un objeto.');
        }
        $productoId = (int) ($item['productoId'] ?? 0);
        $cantidad = (int) ($item['cantidad'] ?? 0);
        if ($productoId <= 0 || $cantidad <= 0) {
            fallo(400, 'item_invalido', 'Cada línea necesita productoId y cantidad mayor a cero.');
        }

        $consultaProducto->execute([$productoId]);
        $producto = $consultaProducto->fetch();
        if (!$producto) {
            fallo(400, 'producto_inexistente', "El producto $productoId no existe.");
        }

        $precio = isset($item['precioUnitario'])
            ? round((float) $item['precioUnitario'], 2)
            : round((float) $producto['precio'], 2);

        $detalles[] = [
            'productoId' => $productoId,
            'cantidad' => $cantidad,
            'precioUnitario' => $precio,
        ];
        $subtotal += $cantidad * $precio;
    }

    $subtotal = round($subtotal, 2);
    $costoEnvio = round((float) ($datos['costoEnvio'] ?? 0), 2);
    $total = round($subtotal + $costoEnvio, 2);

    $clienteId = isset($datos['clienteId']) && $datos['clienteId'] !== null
        ? (int) $datos['clienteId']
        : null;
    $clienteNombre = trim((string) ($datos['clienteNombre'] ?? 'Cliente invitado'));
    if ($clienteNombre === '') {
        $clienteNombre = 'Cliente invitado';
    }
    $clienteTelefono = trim((string) ($datos['clienteTelefono'] ?? ''));
    $direccion = trim((string) ($datos['direccionEntrega'] ?? ''));
    $horaRecoleccion = trim((string) ($datos['horaRecoleccion'] ?? ''));

    $pdo->beginTransaction();
    try {
        $insertar = $pdo->prepare(
            'INSERT INTO pedidos
                (folio, cliente_id, cliente_nombre, cliente_telefono, tipo_entrega, direccion_entrega,
                 hora_recoleccion, tipo_empaque, metodo_pago, requiere_rebanado, notas_panadero, subtotal,
                 costo_envio, total, estado, tiempo_estimado)
             VALUES
                (:folio, :cliente, :nombre, :telefono, :entrega, :direccion,
                 :hora, :empaque, :pago, :rebanado, :notas, :subtotal,
                 :envio, :total, "RECIBIDO", "15 min")'
        );
        $insertar->execute([
            'folio' => $folio,
            'cliente' => $clienteId,
            'nombre' => $clienteNombre,
            'telefono' => $clienteTelefono,
            'entrega' => $tipoEntrega,
            'direccion' => $direccion !== '' ? $direccion : null,
            'hora' => $horaRecoleccion !== '' ? $horaRecoleccion : null,
            'empaque' => $tipoEmpaque,
            'pago' => $metodoPago,
            'rebanado' => !empty($datos['requiereRebanado']) ? 1 : 0,
            'notas' => isset($datos['notasPanadero']) ? (string) $datos['notasPanadero'] : null,
            'subtotal' => $subtotal,
            'envio' => $costoEnvio,
            'total' => $total,
        ]);

        $insertarDetalle = $pdo->prepare(
            'INSERT INTO detalles_pedido (pedido_folio, producto_id, cantidad, precio_unitario)
             VALUES (?, ?, ?, ?)'
        );
        foreach ($detalles as $detalle) {
            $insertarDetalle->execute([
                $folio,
                $detalle['productoId'],
                $detalle['cantidad'],
                $detalle['precioUnitario'],
            ]);
        }

        $pdo->prepare(
            'INSERT INTO notificaciones (usuario_id, pedido_folio, titulo, mensaje)
             VALUES (?, ?, ?, ?)'
        )->execute([
            $clienteId,
            $folio,
            'Pedido recibido',
            "Recibimos tu pedido $folio. Te avisaremos cuando esté en el horno.",
        ]);

        $pdo->commit();
    } catch (Throwable $error) {
        $pdo->rollBack();
        throw $error;
    }

    responder(['ok' => true, 'pedido' => pedidoDto($pdo, $folio)], 201);
}

function cambiarEstadoPedido(PDO $pdo, string $folio): void
{
    requerirAdmin();

    $datos = cuerpo();
    $nuevo = strtoupper(trim((string) ($datos['estado'] ?? '')));
    $validos = ['RECIBIDO', 'EN_PREPARACION', 'EN_CAMINO', 'ENTREGADO', 'CANCELADO'];
    if (!in_array($nuevo, $validos, true)) {
        fallo(422, 'estado_invalido', 'El estado enviado no es válido.');
    }

    $actual = pedidoDto($pdo, $folio);
    if ($actual === null) {
        fallo(404, 'no_encontrado', 'El pedido no existe.');
    }

    $transiciones = [
        'RECIBIDO' => ['EN_PREPARACION', 'CANCELADO'],
        'EN_PREPARACION' => ['EN_CAMINO', 'CANCELADO'],
        'EN_CAMINO' => ['ENTREGADO', 'CANCELADO'],
        'ENTREGADO' => [],
        'CANCELADO' => [],
    ];
    $permitidas = $transiciones[$actual['estado']] ?? [];
    if (!in_array($nuevo, $permitidas, true)) {
        fallo(
            409,
            'transicion_invalida',
            "No se puede pasar de {$actual['estado']} a $nuevo."
        );
    }

    $repartidorId = isset($datos['repartidorId']) && $datos['repartidorId'] !== null
        ? (int) $datos['repartidorId']
        : $actual['repartidorId'];
    $tiempo = trim((string) ($datos['tiempoEstimado'] ?? ''));
    if ($tiempo === '') {
        $tiempo = $actual['tiempoEstimado'];
    }

    $pdo->prepare(
        'UPDATE pedidos SET estado = ?, repartidor_id = ?, tiempo_estimado = ? WHERE folio = ?'
    )->execute([$nuevo, $repartidorId, $tiempo, $folio]);

    $mensajes = [
        'EN_PREPARACION' => ['Tu pedido está en preparación', 'Ya estamos horneando tu pedido.'],
        'EN_CAMINO' => ['Tu pedido va en camino', "Llega en aproximadamente $tiempo."],
        'ENTREGADO' => ['Pedido entregado', 'Gracias por comprar en Acambapan.'],
        'CANCELADO' => ['Pedido cancelado', 'Tu pedido fue cancelado.'],
    ];
    if (isset($mensajes[$nuevo])) {
        $pdo->prepare(
            'INSERT INTO notificaciones (usuario_id, pedido_folio, titulo, mensaje)
             VALUES (?, ?, ?, ?)'
        )->execute([
            $actual['clienteId'],
            $folio,
            $mensajes[$nuevo][0],
            $mensajes[$nuevo][1],
        ]);
    }

    responder(['ok' => true, 'pedido' => pedidoDto($pdo, $folio)]);
}

function cancelarPedido(PDO $pdo, string $folio): void
{
    requerirAdmin();

    $actual = pedidoDto($pdo, $folio);
    if ($actual === null) {
        fallo(404, 'no_encontrado', 'El pedido no existe.');
    }
    if (in_array($actual['estado'], ['ENTREGADO', 'CANCELADO'], true)) {
        fallo(409, 'estado_final', 'Ese pedido ya no se puede cancelar.');
    }

    $pdo->prepare('UPDATE pedidos SET estado = "CANCELADO" WHERE folio = ?')->execute([$folio]);
    $pdo->prepare(
        'INSERT INTO notificaciones (usuario_id, pedido_folio, titulo, mensaje)
         VALUES (?, ?, ?, ?)'
    )->execute([$actual['clienteId'], $folio, 'Pedido cancelado', 'Tu pedido fue cancelado.']);

    responder(['ok' => true, 'pedido' => pedidoDto($pdo, $folio)]);
}

// ---------------------------------------------------------------------------
// Inventario
// ---------------------------------------------------------------------------

function insumoDto(array $fila): array
{
    return [
        'id' => (int) $fila['id'],
        'nombre' => (string) $fila['nombre'],
        'stock' => (float) $fila['stock'],
        'minimo' => (float) $fila['minimo'],
        'unidad' => (string) $fila['unidad'],
    ];
}

function listarInsumos(PDO $pdo): void
{
    $filas = $pdo->query('SELECT id, nombre, stock, minimo, unidad FROM insumos ORDER BY nombre')
        ->fetchAll();
    responder(['ok' => true, 'insumos' => array_map('insumoDto', $filas)]);
}

function crearInsumo(PDO $pdo): void
{
    requerirAdmin();

    $datos = cuerpo();
    $nombre = trim((string) ($datos['nombre'] ?? ''));
    $unidad = trim((string) ($datos['unidad'] ?? ''));
    $stock = isset($datos['stock']) ? (float) $datos['stock'] : 0.0;
    $minimo = isset($datos['minimo']) ? (float) $datos['minimo'] : 0.0;

    if ($nombre === '' || $unidad === '') {
        fallo(400, 'datos_incompletos', 'El nombre y la unidad son obligatorios.');
    }

    $existe = $pdo->prepare('SELECT id FROM insumos WHERE nombre = ? LIMIT 1');
    $existe->execute([$nombre]);
    if ($existe->fetch()) {
        fallo(409, 'insumo_duplicado', 'Ya existe un insumo con ese nombre.');
    }

    $pdo->prepare(
        'INSERT INTO insumos (nombre, stock, minimo, unidad) VALUES (?, ?, ?, ?)'
    )->execute([$nombre, $stock, $minimo, $unidad]);

    $id = (int) $pdo->lastInsertId();
    $consulta = $pdo->prepare('SELECT id, nombre, stock, minimo, unidad FROM insumos WHERE id = ?');
    $consulta->execute([$id]);
    responder(['ok' => true, 'insumo' => insumoDto($consulta->fetch())], 201);
}

function actualizarInsumo(PDO $pdo, int $id): void
{
    requerirAdmin();

    $consulta = $pdo->prepare('SELECT id, nombre, stock, minimo, unidad FROM insumos WHERE id = ?');
    $consulta->execute([$id]);
    $actual = $consulta->fetch();
    if (!$actual) {
        fallo(404, 'no_encontrado', 'El insumo no existe.');
    }

    $datos = cuerpo();
    $nombre = isset($datos['nombre']) ? trim((string) $datos['nombre']) : (string) $actual['nombre'];
    $unidad = isset($datos['unidad']) ? trim((string) $datos['unidad']) : (string) $actual['unidad'];
    $stock = isset($datos['stock']) ? (float) $datos['stock'] : (float) $actual['stock'];
    $minimo = isset($datos['minimo']) ? (float) $datos['minimo'] : (float) $actual['minimo'];

    if ($nombre === '' || $unidad === '') {
        fallo(400, 'datos_incompletos', 'El nombre y la unidad son obligatorios.');
    }
    if ($stock < 0 || $minimo < 0) {
        fallo(422, 'datos_invalidos', 'Las cantidades no pueden ser negativas.');
    }

    $duplicado = $pdo->prepare('SELECT id FROM insumos WHERE nombre = ? AND id <> ? LIMIT 1');
    $duplicado->execute([$nombre, $id]);
    if ($duplicado->fetch()) {
        fallo(409, 'insumo_duplicado', 'Ya existe otro insumo con ese nombre.');
    }

    $pdo->prepare(
        'UPDATE insumos SET nombre = ?, stock = ?, minimo = ?, unidad = ? WHERE id = ?'
    )->execute([$nombre, $stock, $minimo, $unidad, $id]);

    $consulta->execute([$id]);
    responder(['ok' => true, 'insumo' => insumoDto($consulta->fetch())]);
}

function eliminarInsumo(PDO $pdo, int $id): void
{
    requerirAdmin();

    $consulta = $pdo->prepare('SELECT id FROM insumos WHERE id = ?');
    $consulta->execute([$id]);
    if (!$consulta->fetch()) {
        fallo(404, 'no_encontrado', 'El insumo no existe.');
    }

    $receta = $pdo->prepare('SELECT producto_id FROM producto_insumos WHERE insumo_id = ? LIMIT 1');
    $receta->execute([$id]);
    $uso = $receta->fetch();
    if ($uso) {
        fallo(
            409,
            'insumo_en_receta',
            'El insumo se usa en la receta del producto ' . $uso['producto_id'] . '; primero quítalo de la receta.'
        );
    }

    $pdo->prepare('DELETE FROM insumos WHERE id = ?')->execute([$id]);
    responder(['ok' => true]);
}

// ---------------------------------------------------------------------------
// Resumen y avisos
// ---------------------------------------------------------------------------

function resumen(PDO $pdo): void
{
    requerirAdmin();

    $hoy = $pdo->query(
        "SELECT COUNT(*) AS pedidos, COALESCE(SUM(total), 0) AS ventas
         FROM pedidos
         WHERE DATE(fecha_pedido) = CURDATE() AND estado <> 'CANCELADO'"
    )->fetch();

    $bajos = $pdo->query('SELECT COUNT(*) AS total FROM insumos WHERE stock <= minimo')->fetch();

    $recientes = $pdo->query(
        'SELECT folio FROM pedidos ORDER BY fecha_pedido DESC LIMIT 5'
    )->fetchAll();

    $pedidosRecientes = [];
    foreach ($recientes as $fila) {
        $pedido = pedidoDto($pdo, (string) $fila['folio']);
        if ($pedido !== null) {
            $pedidosRecientes[] = $pedido;
        }
    }

    $pedidosHoy = (int) $hoy['pedidos'];
    $ventasHoy = round((float) $hoy['ventas'], 2);

    responder([
        'ok' => true,
        'resumen' => [
            'pedidosHoy' => $pedidosHoy,
            'ventasHoy' => $ventasHoy,
            'ticketPromedio' => $pedidosHoy > 0 ? round($ventasHoy / $pedidosHoy, 2) : 0.0,
            'insumosBajos' => (int) $bajos['total'],
            'pedidosRecientes' => $pedidosRecientes,
        ],
    ]);
}

function listarNotificaciones(PDO $pdo): void
{
    $clienteId = isset($_GET['clienteId']) && $_GET['clienteId'] !== ''
        ? (int) $_GET['clienteId']
        : null;

    if ($clienteId === null) {
        $usuario = requerirUsuario();
        $clienteId = (int) $usuario['id'];
    }

    $consulta = $pdo->prepare(
        'SELECT id, usuario_id, pedido_folio, titulo, mensaje, leida, creado_en
         FROM notificaciones
         WHERE usuario_id = ?
         ORDER BY creado_en DESC
         LIMIT 100'
    );
    $consulta->execute([$clienteId]);

    $notificaciones = array_map(static fn(array $fila): array => [
        'id' => (int) $fila['id'],
        'usuarioId' => $fila['usuario_id'] !== null ? (int) $fila['usuario_id'] : null,
        'pedidoFolio' => $fila['pedido_folio'],
        'titulo' => (string) $fila['titulo'],
        'mensaje' => (string) $fila['mensaje'],
        'leida' => (bool) $fila['leida'],
        'creadoEn' => (string) $fila['creado_en'],
    ], $consulta->fetchAll());

    responder(['ok' => true, 'notificaciones' => $notificaciones]);
}

// ---------------------------------------------------------------------------
// Direcciones de entrega y método de pago
// ---------------------------------------------------------------------------

function direccionDto(array $fila): array
{
    return [
        'id' => (int) $fila['id'],
        'usuarioId' => (int) $fila['usuario_id'],
        'alias' => (string) $fila['alias'],
        'calle' => (string) $fila['calle'],
        'referencias' => $fila['referencias'],
        'predeterminada' => (bool) $fila['predeterminada'],
    ];
}

function listarDirecciones(PDO $pdo): void
{
    $usuario = requerirUsuario();
    $consulta = $pdo->prepare(
        'SELECT * FROM direcciones WHERE usuario_id = ? ORDER BY predeterminada DESC, id'
    );
    $consulta->execute([$usuario['id']]);
    responder([
        'ok' => true,
        'direcciones' => array_map('direccionDto', $consulta->fetchAll()),
    ]);
}

function crearDireccion(PDO $pdo): void
{
    $usuario = requerirUsuario();
    $datos = cuerpo();

    $alias = trim((string) ($datos['alias'] ?? ''));
    $calle = trim((string) ($datos['calle'] ?? ''));
    $referencias = trim((string) ($datos['referencias'] ?? ''));
    $predeterminada = !empty($datos['predeterminada']);

    if ($alias === '' || $calle === '') {
        fallo(400, 'datos_incompletos', 'El alias y la dirección son obligatorios.');
    }

    $cuenta = $pdo->prepare('SELECT COUNT(*) AS total FROM direcciones WHERE usuario_id = ?');
    $cuenta->execute([$usuario['id']]);
    $total = (int) $cuenta->fetch()['total'];
    // La primera dirección del cliente queda como predeterminada.
    $predeterminada = $predeterminada || $total === 0;

    $pdo->beginTransaction();
    try {
        if ($predeterminada) {
            $pdo->prepare('UPDATE direcciones SET predeterminada = 0 WHERE usuario_id = ?')
                ->execute([$usuario['id']]);
        }
        $pdo->prepare(
            'INSERT INTO direcciones (usuario_id, alias, calle, referencias, predeterminada)
             VALUES (?, ?, ?, ?, ?)'
        )->execute([
            $usuario['id'],
            $alias,
            $calle,
            $referencias !== '' ? $referencias : null,
            $predeterminada ? 1 : 0,
        ]);
        $id = (int) $pdo->lastInsertId();
        $pdo->commit();
    } catch (Throwable $error) {
        $pdo->rollBack();
        throw $error;
    }

    $consulta = $pdo->prepare('SELECT * FROM direcciones WHERE id = ?');
    $consulta->execute([$id]);
    responder(['ok' => true, 'direccion' => direccionDto($consulta->fetch())], 201);
}

function actualizarDireccion(PDO $pdo, int $id): void
{
    $usuario = requerirUsuario();
    $consulta = $pdo->prepare('SELECT * FROM direcciones WHERE id = ? AND usuario_id = ?');
    $consulta->execute([$id, $usuario['id']]);
    $actual = $consulta->fetch();
    if (!$actual) {
        fallo(404, 'no_encontrado', 'La dirección no existe.');
    }

    $datos = cuerpo();
    $alias = isset($datos['alias']) ? trim((string) $datos['alias']) : (string) $actual['alias'];
    $calle = isset($datos['calle']) ? trim((string) $datos['calle']) : (string) $actual['calle'];
    $referencias = isset($datos['referencias'])
        ? trim((string) $datos['referencias'])
        : (string) ($actual['referencias'] ?? '');
    $predeterminada = array_key_exists('predeterminada', $datos)
        ? (bool) $datos['predeterminada']
        : (bool) $actual['predeterminada'];

    if ($alias === '' || $calle === '') {
        fallo(400, 'datos_incompletos', 'El alias y la dirección son obligatorios.');
    }

    $pdo->beginTransaction();
    try {
        if ($predeterminada) {
            $pdo->prepare('UPDATE direcciones SET predeterminada = 0 WHERE usuario_id = ?')
                ->execute([$usuario['id']]);
        }
        $pdo->prepare(
            'UPDATE direcciones SET alias = ?, calle = ?, referencias = ?, predeterminada = ?
             WHERE id = ? AND usuario_id = ?'
        )->execute([
            $alias,
            $calle,
            $referencias !== '' ? $referencias : null,
            $predeterminada ? 1 : 0,
            $id,
            $usuario['id'],
        ]);
        $pdo->commit();
    } catch (Throwable $error) {
        $pdo->rollBack();
        throw $error;
    }

    $consulta->execute([$id, $usuario['id']]);
    responder(['ok' => true, 'direccion' => direccionDto($consulta->fetch())]);
}

function eliminarDireccion(PDO $pdo, int $id): void
{
    $usuario = requerirUsuario();
    $consulta = $pdo->prepare('SELECT * FROM direcciones WHERE id = ? AND usuario_id = ?');
    $consulta->execute([$id, $usuario['id']]);
    $actual = $consulta->fetch();
    if (!$actual) {
        fallo(404, 'no_encontrado', 'La dirección no existe.');
    }

    $pdo->prepare('DELETE FROM direcciones WHERE id = ? AND usuario_id = ?')
        ->execute([$id, $usuario['id']]);

    // Si se borró la predeterminada, la primera que quede toma su lugar.
    if ((bool) $actual['predeterminada']) {
        $siguiente = $pdo->prepare(
            'SELECT id FROM direcciones WHERE usuario_id = ? ORDER BY id LIMIT 1'
        );
        $siguiente->execute([$usuario['id']]);
        $fila = $siguiente->fetch();
        if ($fila) {
            $pdo->prepare('UPDATE direcciones SET predeterminada = 1 WHERE id = ?')
                ->execute([(int) $fila['id']]);
        }
    }

    responder(['ok' => true]);
}

function cambiarMetodoPago(PDO $pdo): void
{
    $usuario = requerirUsuario();
    $datos = cuerpo();
    $metodo = strtoupper(trim((string) ($datos['metodo'] ?? '')));

    if (!in_array($metodo, ['EFECTIVO', 'TARJETA_TERMINAL', 'TRANSFERENCIA'], true)) {
        fallo(422, 'metodo_invalido', 'El método de pago no es válido.');
    }

    $pdo->prepare('UPDATE usuarios SET metodo_pago_preferido = ? WHERE id = ?')
        ->execute([$metodo, $usuario['id']]);

    responder(['ok' => true, 'usuario' => usuarioDto(requerirUsuario())]);
}
