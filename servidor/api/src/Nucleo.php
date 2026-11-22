<?php
declare(strict_types=1);

require_once __DIR__ . '/Config.php';

/**
 * Piezas comunes de la API: conexión, respuestas JSON, CORS y bitácora.
 */

function bd(): PDO
{
    static $pdo = null;
    if ($pdo instanceof PDO) {
        return $pdo;
    }
    $dsn = sprintf('mysql:host=%s;dbname=%s;charset=utf8mb4', BD_HOST, BD_NOMBRE);
    $pdo = new PDO($dsn, BD_USUARIO, BD_PASSWORD, [
        PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
        PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
        PDO::ATTR_EMULATE_PREPARES => false,
    ]);
    return $pdo;
}

/** Cabeceras CORS para que la app (y el navegador) puedan consumir la API. */
function cors(): void
{
    header('Access-Control-Allow-Origin: *');
    header('Access-Control-Allow-Headers: Authorization, Content-Type');
    header('Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS');
    if (($_SERVER['REQUEST_METHOD'] ?? 'GET') === 'OPTIONS') {
        http_response_code(204);
        exit;
    }
}

/** Responde JSON con el código HTTP indicado. */
function responder(array $datos, int $codigo = 200): void
{
    http_response_code($codigo);
    header('Content-Type: application/json; charset=utf-8');
    echo json_encode($datos, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
    exit;
}

/** Respuesta de error uniforme: {"ok":false,"error":{...}}. */
function fallo(int $codigo, string $codigoError, string $mensaje): void
{
    responder([
        'ok' => false,
        'error' => ['codigo' => $codigoError, 'mensaje' => $mensaje],
    ], $codigo);
}

/** Cuerpo JSON de la petición (o array vacío). */
function cuerpo(): array
{
    $texto = file_get_contents('php://input');
    if ($texto === false || trim($texto) === '') {
        return [];
    }
    $datos = json_decode($texto, true);
    if (!is_array($datos)) {
        fallo(400, 'json_invalido', 'El cuerpo de la petición no es JSON válido.');
    }
    return $datos;
}

/** Ruta normalizada: '/productos/3'.
 *
 * Descuenta el prefijo desde donde se sirva la API (el vhost en la raíz, una
 * subcarpeta o el viejo prefijo /api) para que el router no dependa de dónde
 * esté montado el proyecto.
 */
function rutaSolicitada(): string
{
    $ruta = parse_url($_SERVER['REQUEST_URI'] ?? '/', PHP_URL_PATH) ?: '/';

    $script = $_SERVER['SCRIPT_NAME'] ?? '/index.php';
    $base = rtrim(str_replace('\\', '/', dirname($script)), '/');
    if ($base !== '' && $base !== '/' && str_starts_with($ruta, $base)) {
        $ruta = substr($ruta, strlen($base));
    }

    if ($ruta === '/api' || str_starts_with($ruta, '/api/')) {
        $ruta = substr($ruta, 4);
    }
    $ruta = '/' . trim($ruta, '/');
    return $ruta;
}

function b64url(string $texto): string
{
    return rtrim(strtr(base64_encode($texto), '+/', '-_'), '=');
}

function b64urlDecodificar(string $texto): string
{
    $relleno = strlen($texto) % 4;
    if ($relleno > 0) {
        $texto .= str_repeat('=', 4 - $relleno);
    }
    return (string) base64_decode(strtr($texto, '-_', '+/'), true);
}

/** Deja constancia de un error sin filtrar detalles al cliente. */
function registrar_error(Throwable $error): void
{
    $linea = sprintf(
        "[%s] %s: %s en %s:%d\n",
        date('Y-m-d H:i:s'),
        get_class($error),
        $error->getMessage(),
        $error->getFile(),
        $error->getLine()
    );
    @file_put_contents(__DIR__ . '/../logs/api.log', $linea, FILE_APPEND | LOCK_EX);
}
