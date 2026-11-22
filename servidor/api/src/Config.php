<?php
declare(strict_types=1);

/**
 * Configuración de la API de Acambapan.
 *
 * Es la única fuente de credenciales de la base de datos. Si XAMPP tiene otra
 * contraseña de root, se cambia aquí y en ningún otro lado.
 */

const BD_HOST = '127.0.0.1';
const BD_NOMBRE = 'pantradicion_db';
const BD_USUARIO = 'root';
const BD_PASSWORD = '';

const VERSION_API = '1.0.0';

/** Horas que dura el token de sesión. */
const HORAS_TOKEN = 12;

/**
 * Secreto para firmar los tokens (HMAC-SHA256).
 * Vive en `api/secret.key`, fuera de `src/`, y el .htaccess lo bloquea.
 */
function secreto(): string
{
    static $cache = null;
    if ($cache !== null) {
        return $cache;
    }
    $ruta = dirname(__DIR__) . '/secret.key';
    if (!is_file($ruta)) {
        http_response_code(500);
        header('Content-Type: application/json; charset=utf-8');
        echo json_encode([
            'ok' => false,
            'error' => [
                'codigo' => 'sin_secreto',
                'mensaje' => 'Falta api/secret.key en el servidor.',
            ],
        ], JSON_UNESCAPED_UNICODE);
        exit;
    }
    $cache = trim((string) file_get_contents($ruta));
    return $cache;
}
