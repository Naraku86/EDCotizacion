# Changelog / Registro de cambios

Format based on [Keep a Changelog](https://keepachangelog.com/). Versions follow
[Semantic Versioning](https://semver.org/).

## [1.1.0] - 2026-09-25

**English**

- Public test instance (`app.prueba.activa`): shared `admin` / `admin` account that cannot be
  changed, notice on every page, credentials on the sign-in page and sample data when the database is
  empty.

**Español**

- Instancia de prueba pública (`app.prueba.activa`): cuenta compartida `admin` / `admin` que no se
  puede cambiar, aviso en todas las pantallas, credenciales en la pantalla de entrada y datos de
  ejemplo cuando la base está vacía.

## [1.0.0] - 2026-09-25

First public release. / Primera versión pública.

**English**

- Quotes with automatic client and product creation, cost + margin pricing, optional VAT and
  shipping without VAT; history with search, statuses, expiry, duplicate and PDF regeneration.
- PDF template edited directly on the page: four layouts, colors, logo, Letter/A4, optional bank
  details, note and signature.
- Several issuing companies per installation, each with its own data and template.
- Public demo at `/demo`: build a quote and download the PDF without an account; nothing is stored.
- Security: BCrypt passwords, forced change of the default password, sign-in attempt limits per user
  and IP, CSRF, strict CSP, input and size limits, local-only access by default.
- Desktop program: system tray icon, single instance, "Cerrar programa", log file in the data folder,
  optional `application.yml` in the data folder.
- Installers for Windows (MSI), macOS (DMG, Apple Silicon and Intel) and Linux (DEB/RPM, x64 and
  arm64) with Java included; portable versions; container image for Podman/Docker.

**Español**

- Cotizaciones con alta automática de clientes y productos, precio por costo + ganancia, IVA opcional y
  envío sin IVA; historial con búsqueda, estados, vencimiento, duplicar y volver a generar el PDF.
- Plantilla del PDF editada sobre la hoja: cuatro diseños, colores, logo, Carta/A4, datos bancarios,
  nota y firma opcionales.
- Varias empresas emisoras por instalación, cada una con sus datos y plantilla.
- Demo público en `/demo`: arma una cotización y descarga el PDF sin cuenta; no se guarda nada.
- Seguridad: contraseñas con BCrypt, cambio obligatorio de la contraseña de fábrica, límite de intentos
  por usuario e IP, CSRF, CSP estricta, límites de datos y tamaño, acceso solo local por defecto.
- Programa de escritorio: icono en la bandeja, una sola copia, "Cerrar programa", registro en la
  carpeta de datos y `application.yml` opcional en esa carpeta.
- Instaladores para Windows (MSI), macOS (DMG, Apple Silicon e Intel) y Linux (DEB/RPM, x64 y arm64)
  con Java incluido; versiones portables; imagen de contenedor para Podman/Docker.

[1.1.0]: https://github.com/Naraku86/EDCotizacion/releases/tag/v1.1.0
[1.0.0]: https://github.com/Naraku86/EDCotizacion/releases/tag/v1.0.0
