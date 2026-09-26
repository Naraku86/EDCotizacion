# Configuration

**English** · [Español](../es/configuracion.md)

EDCotizacion works without any configuration. To change something there are three ways; when an
option is set in more than one, the first one in this list wins:

1. **Arguments** at start-up: `--server.port=8095` (portable version, `.jar` or `iniciar.sh`).
2. **Environment variables**: `SERVER_PORT=8095` (typical on a [server](server.md)). The name is the
   option in upper case, with `_` instead of `.` and without dashes: `app.demo.pdf-por-minuto` →
   `APP_DEMO_PDFPORMINUTO`.
3. **An `application.yml` file in your [data folder](installation.md#your-data)**: the easiest way
   with the installed app. For example:

```yaml
server:
  port: 8095
app:
  demo:
    activo: false
```

Changes apply the next time the program starts.

## Options

| Option | Default | Purpose |
|---|---|---|
| `server.port` | `8090` | Port. The app is at `http://localhost:<port>`. |
| `server.address` | `127.0.0.1` | Who can connect. `127.0.0.1` = this computer only; `0.0.0.0` = the whole network. |
| `app.home` | `~/EDCotizacion` | Data folder. Only as an argument (`--app.home=...`) or `APP_HOME` variable, not in the file. |
| `app.abrir-navegador` | `true` | Open the browser on start-up. |
| `app.modo` | `escritorio` | `escritorio` (desktop): browser, tray icon and "Cerrar programa". `servidor` (server): none of those (container). |
| `app.demo.activo` | `true` | Public demo at `/demo`. `false` turns it off. |
| `app.demo.pdf-por-minuto` | `10` | Demo PDFs per minute per IP. |
| `app.demo.vistas-por-minuto` | `90` | Demo previews per minute per IP. |
| `app.demo.pdf-simultaneos` | `2` | Demo PDFs generated at the same time. |
| `app.prueba.activa` | `false` | [Public test instance](#public-test-instance). |
| `app.prueba.datos-ejemplo` | `true` | In the test instance, load sample data when the database is empty. |
| `app.prueba.aviso` | (text) | Notice shown on every page of the test instance. |
| `server.servlet.session.timeout` | `12h` | Idle time before the session expires. |
| `server.forward-headers-strategy` | — | `native` behind a reverse proxy, to see the visitor's real IP. |
| `server.servlet.session.cookie.secure` | `false` | `true` when the app is used over HTTPS. |

## Using the app from other computers

By default only the computer running it can open it. To use it from other computers on your local
network:

```yaml
server:
  address: 0.0.0.0
```

Then open `http://<ip-of-that-computer>:8090` from the others. You may need to allow the port in the
system firewall.

On the network, data travels over plain HTTP, password included. On a trusted network (your office)
that is usually enough; otherwise put the app behind a reverse proxy with HTTPS
(see [Server](server.md#https-with-a-reverse-proxy)).

## Public test instance

With `app.prueba.activa: true` the app is ready for anyone to try the full application (not only the
`/demo` page):

- Everybody signs in with user **admin** and password **admin**; the sign-in page says so.
- The account cannot be changed and the default password does not have to be changed.
- Every page shows a notice: the account is shared and the data is deleted.
- If the database is empty on start-up, two companies and a few sample quotes are loaded.

The app **does not delete** the data by itself: whoever runs the server does, for example by
recreating the container with a new volume every 8 hours (see [Server](server.md)).

## Several installations on one computer

Each data folder is an independent installation (database, companies, templates and users). Use a
different folder and port for each:

```bash
./iniciar.sh ~/EDCotizacion 8090
./iniciar.sh ~/EDCotizacion-test 8091
```

On Windows: `iniciar.bat C:\path\data 8091`. With the installed version:
`EDCotizacion --app.home=<folder> --server.port=<port>`.
