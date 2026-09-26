# Server (Podman / Docker)

**English** · [Español](../es/servidor.md)

To use EDCotizacion on **one computer**, the [installer](installation.md) is the better choice. The
container image is meant for **servers**: a PC shared by several people, a NAS, or an internet-facing
server (for example, to publish the demo).

The `ghcr.io/naraku86/edcotizacion` image is available for `amd64` and `arm64`. Every command works the
same with `docker` instead of `podman`.

## Start

```bash
podman run -d --name edcotizacion \
  -p 8090:8090 \
  -v edcotizacion-data:/data \
  -e TZ=America/Mexico_City \
  ghcr.io/naraku86/edcotizacion:latest
```

Open `http://<server>:8090`, sign in with **admin** / **admin** and choose a new password (the app
does not let you continue without changing it).

- Data (SQLite database and log) is stored in the `edcotizacion-data` volume, mounted at `/data`.
- The container runs as an unprivileged user and in `servidor` (server) mode: it does not try to
  open a browser and does not show "Cerrar programa" (close program).
- `TZ` sets the time zone used for dates.

### With Compose

The repository includes [`compose.yaml`](../../compose.yaml):

```bash
podman compose up -d        # or: docker compose up -d
```

### Build the image from source

```bash
podman build -t edcotizacion .
```

## Options

Pass them as environment variables (`-e NAME=value`). The most useful ones:

| Variable | Effect |
|---|---|
| `APP_DEMO_ACTIVO=false` | Turns off the public demo at `/demo`. |
| `SERVER_FORWARDHEADERSSTRATEGY=native` | Behind a proxy: use the visitor's real IP (demo limits and sign-in attempts). |
| `SERVER_SERVLET_SESSION_COOKIE_SECURE=true` | The session cookie is only sent over HTTPS. |

All options are listed in [Configuration](configuration.md). You can also place an
`application.yml` in the volume (`/data/application.yml`).

## HTTPS with a reverse proxy

If the server is reachable from the internet, **do not expose port 8090 directly**: put a proxy with
a certificate in front of it. Example with [Caddy](https://caddyserver.com), which obtains the
certificate automatically:

```bash
podman run -d --name edcotizacion \
  -p 127.0.0.1:8090:8090 \
  -v edcotizacion-data:/data \
  -e SERVER_FORWARDHEADERSSTRATEGY=native \
  -e SERVER_SERVLET_SESSION_COOKIE_SECURE=true \
  ghcr.io/naraku86/edcotizacion:latest
```

`Caddyfile`:

```
quotes.example.com {
    reverse_proxy 127.0.0.1:8090
}
```

With `-p 127.0.0.1:8090:8090` the port is only open to the server itself; outside traffic can only
come in through the proxy.

## Backup

The database is a single file. Stop the container to copy it in a consistent state:

```bash
podman stop edcotizacion
podman run --rm -v edcotizacion-data:/data -v "$PWD":/backup:Z \
  docker.io/library/alpine cp /data/cotizaciones.db /backup/cotizaciones-$(date +%F).db
podman start edcotizacion
```

To restore, stop the container and copy the backup back to `/data/cotizaciones.db` the same way.

## Updating

```bash
podman pull ghcr.io/naraku86/edcotizacion:latest
podman rm -f edcotizacion
# run the same "podman run" command as above
```

The volume data is kept and the database is upgraded automatically if needed. Back up first: going
back to an older version requires restoring the backup. To pin a version, use the `:1.0.0` tag
instead of `:latest`.

## Logs

```bash
podman logs -f edcotizacion
```

They are also written to `/data/edcotizacion.log`.
