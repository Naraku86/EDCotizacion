# EDCotizacion

[![CI](https://github.com/Naraku86/EDCotizacion/actions/workflows/ci.yml/badge.svg)](https://github.com/Naraku86/EDCotizacion/actions/workflows/ci.yml)
[![Release](https://img.shields.io/github/v/release/Naraku86/EDCotizacion)](https://github.com/Naraku86/EDCotizacion/releases/latest)
[![License: MIT](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)

**English** · [Español](README.es.md)

Quote generator for small businesses: capture a quote, get a clean PDF. It runs on your own
computer (Windows, macOS or Linux), keeps everything in a local SQLite file and works without an
internet connection. It can also run on a server with Podman or Docker.

> The user interface is in **Spanish** and amounts follow Mexican conventions (MXN, 16% VAT by
> default). The VAT rate and every text printed on the PDF can be changed.

![Quote history](docs/img/historial.png)

## Features

- **Quotes to PDF** with four layouts (classic, minimal, modern, compact), your colors, logo and
  Letter/A4 paper. Everything printed on the PDF is edited directly on the page.
- **Several companies** in one installation, each with its own data, logo and template.
- **Clients and products are created as you type**: no extra forms.
- **Pricing helpers**: cost + margin (30% by default), or type the final price with or without VAT.
- **History** with search, status (draft / sent / accepted / rejected), expired quotes highlighted,
  edit, duplicate and regenerate the PDF.
- **Public demo** at `/demo`: anyone can fill in a quote on one page and download the PDF, without
  an account and without anything being stored on the server.
- **Security**: login with BCrypt passwords, forced change of the default password, brute-force
  protection per user and IP, CSRF, strict CSP. Only reachable from the same computer by default.

| Quote form | Template editor | Resulting PDF |
|---|---|---|
| ![Form](docs/img/formulario.png) | ![Template editor](docs/img/plantilla.png) | ![PDF](docs/img/pdf.png) |

## Download

Get the latest version from **[Releases](https://github.com/Naraku86/EDCotizacion/releases/latest)**.
Java is included; nothing else needs to be installed.

| System | File |
|---|---|
| Windows 10/11 | `EDCotizacion-<version>-windows-x64.msi` |
| macOS Apple Silicon (M1–M4) | `EDCotizacion-<version>-macos-arm64.dmg` |
| macOS Intel | `EDCotizacion-<version>-macos-x64.dmg` |
| Debian, Ubuntu, Linux Mint | `EDCotizacion-<version>-linux-x64.deb` (or `arm64`) |
| Fedora, RHEL, openSUSE | `EDCotizacion-<version>-linux-x64.rpm` (or `arm64`) |
| Any Linux, no install | `EDCotizacion-<version>-linux-x64-portable.tar.gz` |
| Windows, no install | `EDCotizacion-<version>-windows-x64-portable.zip` |

The binaries are **not code-signed** yet, so Windows and macOS show a warning the first time.
The [installation guide](docs/en/installation.md) explains how to open them on each system.

## Quick start

1. Install and open **EDCotizacion**. Your browser opens at <http://localhost:8090>.
2. Sign in with user **admin** and password **admin**. You will be asked to choose a new password.
3. Go to **Configuración › Empresas y plantillas** to enter your company data, logo and template.
4. Click **+ Nueva cotización**.

An icon next to the clock (system tray) lets you reopen the browser or quit. Where there is no
tray (some Linux desktops), use **Cerrar programa** in the top bar.

Your data lives in the `EDCotizacion` folder inside your user folder. To back up, copy
`cotizaciones.db`. Details in the [installation guide](docs/en/installation.md#your-data).

## Server (Podman / Docker)

```bash
podman run -d --name edcotizacion -p 8090:8090 -v edcotizacion-data:/data \
  ghcr.io/naraku86/edcotizacion:latest
```

Works the same with `docker`. See the [server guide](docs/en/server.md) for Compose, HTTPS behind a
reverse proxy, backups and updates.

## Documentation

| | |
|---|---|
| [Installation](docs/en/installation.md) | Windows, macOS and Linux; unsigned-binary warnings; updating; uninstalling |
| [Server](docs/en/server.md) | Podman/Docker, Compose, reverse proxy, backups |
| [Configuration](docs/en/configuration.md) | Port, network access, public demo, data folder |
| [Usage](docs/en/usage.md) | PDF template, several companies, public demo, several installations |
| [Development](docs/en/development.md) | Build from source, tests, packaging, releases |
| [Security](SECURITY.md) | Reporting vulnerabilities and built-in protections |
| [Changelog](CHANGELOG.md) | |

## Built with

Java 21 · Spring Boot 4 · Spring Security · Spring Data JPA (Hibernate) · Thymeleaf · SQLite + Flyway ·
OpenHTMLtoPDF · jpackage

## License

[MIT](LICENSE) © 2026 Edgar Vigueras
