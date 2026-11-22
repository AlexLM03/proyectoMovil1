<?php
declare(strict_types=1);

require_once __DIR__ . '/Nucleo.php';

/**
 * Autenticación por token firmado (HMAC-SHA256, 12 horas).
 *
 * El token viaja en `Authorization: Bearer <token>` y no se guarda en el
 * servidor: la firma y la fecha de expiración bastan. En cada petición se
 * vuelve a leer el usuario para respetar bajas y cambios de rol.
 */

function cabeceraAutorizacion(): string
{
    if (!empty($_SERVER['HTTP_AUTHORIZATION'])) {
        return (string) $_SERVER['HTTP_AUTHORIZATION'];
    }
    if (!empty($_SERVER['REDIRECT_HTTP_AUTHORIZATION'])) {
        return (string) $_SERVER['REDIRECT_HTTP_AUTHORIZATION'];
    }
    if (function_exists('apache_request_headers')) {
        foreach (apache_request_headers() as $clave => $valor) {
            if (strtolower((string) $clave) === 'authorization') {
                return (string) $valor;
            }
        }
    }
    return '';
}

function crearToken(int $id, string $rol): string
{
    $expira = time() + HORAS_TOKEN * 3600;
    $carga = b64url($id . '|' . $rol . '|' . $expira);
    $firma = b64url(hash_hmac('sha256', $carga, secreto(), true));
    return $carga . '.' . $firma;
}

/**
 * Devuelve el usuario del token (o null si no hay). Con $obligatorio = true
 * responde 401 y corta la petición.
 */
function usuarioDelToken(bool $obligatorio = false): ?array
{
    $cabecera = cabeceraAutorizacion();
    $token = '';

    if (preg_match('/^Bearer\s+(.+)$/i', $cabecera, $coincidencias)) {
        $token = trim($coincidencias[1]);
    } elseif (!empty($_GET['token'])) {
        // Útil para pruebas con curl; la app siempre usa la cabecera.
        $token = trim((string) $_GET['token']);
    }

    if ($token === '') {
        if ($obligatorio) {
            fallo(401, 'sin_token', 'Inicia sesión para continuar.');
        }
        return null;
    }

    $partes = explode('.', $token);
    if (count($partes) !== 2) {
        if ($obligatorio) {
            fallo(401, 'token_invalido', 'El token no tiene el formato esperado.');
        }
        return null;
    }

    [$carga, $firma] = $partes;
    $esperada = b64url(hash_hmac('sha256', $carga, secreto(), true));
    if (!hash_equals($esperada, $firma)) {
        if ($obligatorio) {
            fallo(401, 'token_invalido', 'La firma del token no es válida.');
        }
        return null;
    }

    $datos = explode('|', b64urlDecodificar($carga));
    if (count($datos) !== 3) {
        if ($obligatorio) {
            fallo(401, 'token_invalido', 'El contenido del token no es válido.');
        }
        return null;
    }

    [$id, $rol, $expira] = $datos;
    if ((int) $expira < time()) {
        if ($obligatorio) {
            fallo(401, 'token_vencido', 'Tu sesión expiró. Vuelve a iniciar sesión.');
        }
        return null;
    }

    $consulta = bd()->prepare(
        'SELECT id, nombre, correo, telefono, direccion, rol, metodo_pago_preferido, activo
         FROM usuarios WHERE id = ?'
    );
    $consulta->execute([(int) $id]);
    $usuario = $consulta->fetch();

    if (!$usuario || !(bool) $usuario['activo']) {
        if ($obligatorio) {
            fallo(401, 'usuario_inactivo', 'La cuenta no existe o está desactivada.');
        }
        return null;
    }
    if ((string) $usuario['rol'] !== (string) $rol) {
        if ($obligatorio) {
            fallo(401, 'token_invalido', 'El rol del token ya no coincide con la cuenta.');
        }
        return null;
    }

    $usuario['id'] = (int) $usuario['id'];
    $usuario['activo'] = (bool) $usuario['activo'];
    return $usuario;
}

function requerirUsuario(): array
{
    $usuario = usuarioDelToken(true);
    if ($usuario === null) {
        fallo(401, 'sin_token', 'Inicia sesión para continuar.');
    }
    return $usuario;
}

function requerirAdmin(): array
{
    $usuario = requerirUsuario();
    if ($usuario['rol'] !== 'ADMIN') {
        fallo(403, 'sin_permiso', 'Esta acción es solo para administradores.');
    }
    return $usuario;
}

/**
 * Comprueba la contraseña. Acepta los hashes de password_hash() y, por
 * compatibilidad con el script original, las contraseñas en texto plano.
 */
function verificarContrasena(string $guardada, string $recibida): bool
{
    if (str_starts_with($guardada, '$2y$') || str_starts_with($guardada, '$argon2')) {
        return password_verify($recibida, $guardada);
    }
    return hash_equals($guardada, $recibida);
}

/** true si la contraseña guardada ya está hasheada. */
function contrasenaHasheada(string $guardada): bool
{
    return str_starts_with($guardada, '$2y$') || str_starts_with($guardada, '$argon2');
}
