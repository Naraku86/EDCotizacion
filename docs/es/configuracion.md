# Configuración

[English](../en/configuration.md) · **Español**

EDCotizacion funciona sin configurar nada. Si necesitas cambiar algo, hay tres formas; si una opción
aparece en varias, gana la primera de esta lista:

1. **Argumentos** al arrancar: `--server.port=8095` (versión portable, `.jar` o `iniciar.sh`).
2. **Variables de entorno**: `SERVER_PORT=8095` (lo usual en [servidor](servidor.md)). El nombre es
   la opción en mayúsculas, con `_` en lugar de `.` y sin guiones: `app.demo.pdf-por-minuto` →
   `APP_DEMO_PDFPORMINUTO`.
3. **Archivo `application.yml` en tu [carpeta de datos](instalacion.md#tus-datos)**: lo más cómodo
   con la app instalada. Por ejemplo:

```yaml
server:
  port: 8095
app:
  demo:
    activo: false
```

Los cambios se aplican al volver a abrir el programa.

## Opciones

| Opción | Predeterminado | Para qué |
|---|---|---|
| `server.port` | `8090` | Puerto. La app queda en `http://localhost:<puerto>`. |
| `server.address` | `127.0.0.1` | Quién puede conectarse. `127.0.0.1` = solo esta computadora; `0.0.0.0` = toda la red. |
| `app.home` | `~/EDCotizacion` | Carpeta de datos. Solo como argumento (`--app.home=...`) o variable `APP_HOME`, no en el archivo. |
| `app.abrir-navegador` | `true` | Abrir el navegador al iniciar. |
| `app.modo` | `escritorio` | `escritorio`: navegador, icono en la bandeja y "Cerrar programa". `servidor`: nada de eso (contenedor). |
| `app.demo.activo` | `true` | Demo público en `/demo`. `false` lo apaga. |
| `app.demo.pdf-por-minuto` | `10` | PDF del demo por minuto por IP. |
| `app.demo.vistas-por-minuto` | `90` | Vistas previas del demo por minuto por IP. |
| `app.demo.pdf-simultaneos` | `2` | PDF del demo generándose a la vez. |
| `server.servlet.session.timeout` | `12h` | Tiempo sin uso para cerrar la sesión. |
| `server.forward-headers-strategy` | — | `native` detrás de un proxy inverso, para ver la IP real del visitante. |
| `server.servlet.session.cookie.secure` | `false` | `true` si la app se usa por HTTPS. |

## Usar la app desde otras computadoras

Por defecto solo la computadora donde corre puede abrirla. Para usarla desde otras de tu red local:

```yaml
server:
  address: 0.0.0.0
```

Luego abre `http://<ip-de-esa-computadora>:8090` desde las demás. Puede que tengas que permitir el
puerto en el firewall del sistema.

En la red la información viaja por HTTP sin cifrar, incluida la contraseña. En una red de confianza
(tu oficina) suele ser suficiente; para algo más, pon la app detrás de un proxy con HTTPS
(ver [Servidor](servidor.md#https-con-proxy-inverso)).

## Varias instalaciones en la misma computadora

Cada carpeta de datos es una instalación independiente (base, empresas, plantillas y usuarios). Usa
una carpeta y un puerto distintos para cada una:

```bash
./iniciar.sh ~/EDCotizacion 8090
./iniciar.sh ~/EDCotizacion-pruebas 8091
```

En Windows: `iniciar.bat C:\ruta\datos 8091`. Con la versión instalada:
`EDCotizacion --app.home=<carpeta> --server.port=<puerto>`.
