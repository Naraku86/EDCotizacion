# Servidor (Podman / Docker)

[English](../en/server.md) · **Español**

Para usar EDCotizacion en **una computadora**, conviene el [instalador](instalacion.md). La imagen de
contenedor es para **servidores**: una PC que atiende a varias personas, un NAS o un servidor en
internet (por ejemplo, para publicar el demo).

La imagen `ghcr.io/naraku86/edcotizacion` existe para `amd64` y `arm64`. Todos los comandos funcionan
igual cambiando `podman` por `docker`.

## Arrancar

```bash
podman run -d --name edcotizacion \
  -p 8090:8090 \
  -v edcotizacion-datos:/data \
  -e TZ=America/Mexico_City \
  ghcr.io/naraku86/edcotizacion:latest
```

Abre `http://<servidor>:8090`, entra con **admin** / **admin** y elige una contraseña nueva (la app
no deja continuar sin cambiarla).

- Los datos (base SQLite y registro) quedan en el volumen `edcotizacion-datos`, montado en `/data`.
- El contenedor corre como usuario sin privilegios y en modo `servidor`: no intenta abrir un navegador
  ni muestra "Cerrar programa".
- `TZ` define la zona horaria de las fechas.

### Con Compose

El repositorio incluye [`compose.yaml`](../../compose.yaml):

```bash
podman compose up -d        # o: docker compose up -d
```

### Construir la imagen desde el código

```bash
podman build -t edcotizacion .
```

## Opciones

Se pasan como variables de entorno (`-e NOMBRE=valor`). Las más útiles:

| Variable | Efecto |
|---|---|
| `APP_DEMO_ACTIVO=false` | Apaga el demo público en `/demo`. |
| `SERVER_FORWARDHEADERSSTRATEGY=native` | Detrás de un proxy: usa la IP real del visitante (límites del demo e intentos de entrada). |
| `SERVER_SERVLET_SESSION_COOKIE_SECURE=true` | La cookie de sesión solo viaja por HTTPS. |

Todas las opciones están en [Configuración](configuracion.md). También puedes poner un
`application.yml` dentro del volumen (`/data/application.yml`).

## HTTPS con proxy inverso

Si el servidor es accesible desde internet, **no expongas el puerto 8090 directamente**: pon delante
un proxy con certificado. Ejemplo con [Caddy](https://caddyserver.com), que obtiene el certificado solo:

```bash
podman run -d --name edcotizacion \
  -p 127.0.0.1:8090:8090 \
  -v edcotizacion-datos:/data \
  -e SERVER_FORWARDHEADERSSTRATEGY=native \
  -e SERVER_SERVLET_SESSION_COOKIE_SECURE=true \
  ghcr.io/naraku86/edcotizacion:latest
```

`Caddyfile`:

```
cotizaciones.tudominio.com {
    reverse_proxy 127.0.0.1:8090
}
```

Con `-p 127.0.0.1:8090:8090` el puerto solo se abre para el propio servidor; el tráfico de fuera
entra únicamente por el proxy.

## Respaldo

La base es un solo archivo. Detén el contenedor para copiarlo en un estado consistente:

```bash
podman stop edcotizacion
podman run --rm -v edcotizacion-datos:/data -v "$PWD":/respaldo:Z \
  docker.io/library/alpine cp /data/cotizaciones.db /respaldo/cotizaciones-$(date +%F).db
podman start edcotizacion
```

Para restaurar, detén el contenedor y copia el respaldo de vuelta a `/data/cotizaciones.db` de la
misma forma.

## Actualizar

```bash
podman pull ghcr.io/naraku86/edcotizacion:latest
podman rm -f edcotizacion
# vuelve a correr el mismo "podman run" de arriba
```

Los datos del volumen se conservan y la base se actualiza sola si hace falta. Respalda antes: volver
a una versión anterior requiere restaurar el respaldo. Para fijar una versión, usa la etiqueta
`:1.0.0` en lugar de `:latest`.

## Registro

```bash
podman logs -f edcotizacion
```

También queda en `/data/edcotizacion.log`.
