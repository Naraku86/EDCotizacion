# Security / Seguridad

**English** · [Español](#español)

## Reporting a vulnerability

Please **do not open a public issue**. Use GitHub's private reporting instead:
**Security › Report a vulnerability** in this repository
(<https://github.com/OWNER/EDCotizacion/security/advisories/new>).

Include the version, how to reproduce it and the impact you expect. You will get an answer as soon as
possible; fixes are released as a new version and credited in the changelog unless you prefer otherwise.

Only the latest release receives security fixes.

## Built-in protections

- Passwords hashed with BCrypt (minimum 8 characters); the default `admin` / `admin` password must be
  changed before the app can be used.
- Sign-in attempts limited per user + IP (5) and per IP (20), 5-minute lockout; the same message is
  shown for a wrong password, an unknown user or a lockout.
- Only reachable from the same computer by default (`server.address: 127.0.0.1`).
- CSRF protection, strict Content Security Policy (no inline scripts), `X-Frame-Options: DENY`,
  `nosniff`, `Referrer-Policy`.
- Server-side validation of all input: amounts limited to 12 integer and 4 decimal digits; logos must
  be real PNG/JPG files of at most 1.5 MB and 2000 × 2000 pixels; requests limited to 5 MB, including
  chunked ones.
- Public demo: no session, no database access, per-IP rate limits and a cap on concurrent PDF generation.
- The container image runs as an unprivileged user.

When exposing the app to a network you do not trust, put it behind a reverse proxy with HTTPS
(see [docs/en/server.md](docs/en/server.md#https-with-a-reverse-proxy)).

---

## Español

### Reportar una vulnerabilidad

Por favor **no abras un issue público**. Usa el reporte privado de GitHub:
**Security › Report a vulnerability** en este repositorio
(<https://github.com/OWNER/EDCotizacion/security/advisories/new>).

Incluye la versión, cómo reproducirlo y el impacto que esperas. Recibirás respuesta lo antes posible;
las correcciones se publican como una versión nueva y se da crédito en el registro de cambios, salvo
que prefieras lo contrario.

Solo la última versión recibe correcciones de seguridad.

### Protecciones incluidas

- Contraseñas con BCrypt (mínimo 8 caracteres); la contraseña de fábrica `admin` / `admin` se debe
  cambiar antes de usar la app.
- Intentos de entrada limitados por usuario + IP (5) y por IP (20), con bloqueo de 5 minutos; el mismo
  mensaje para contraseña incorrecta, usuario inexistente o bloqueo.
- Por defecto solo acepta conexiones de la misma computadora (`server.address: 127.0.0.1`).
- Protección CSRF, política de seguridad de contenido estricta (sin scripts en línea),
  `X-Frame-Options: DENY`, `nosniff`, `Referrer-Policy`.
- Validación en el servidor de todo lo que llega: montos con máximo 12 enteros y 4 decimales; logos
  PNG/JPG reales de máximo 1.5 MB y 2000 × 2000 píxeles; peticiones de máximo 5 MB, también las
  enviadas por partes.
- Demo público: sin sesión, sin acceso a la base, límites por IP y tope de PDF generándose a la vez.
- La imagen de contenedor corre con un usuario sin privilegios.

Si expones la app a una red que no es de confianza, ponla detrás de un proxy inverso con HTTPS
(ver [docs/es/servidor.md](docs/es/servidor.md#https-con-proxy-inverso)).
