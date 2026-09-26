# Development

**English** · [Español](../es/desarrollo.md)

The code (classes, methods, comments) is written in Spanish, like the interface.

## Requirements

- JDK 21 (for example [Temurin](https://adoptium.net))
- Maven 3.9 or later

## Build and run

```bash
mvn package
java -jar target/edcotizacion.jar          # or: ./iniciar.sh  /  iniciar.bat
```

To keep test data apart from your own, use another folder: `./iniciar.sh ~/EDCotizacion-dev 8091`.

## Tests

```bash
mvn test      # unit tests (*Test)
mvn verify    # also integration tests (*IT): start the whole app with a temporary SQLite database
```

Tests never touch `~/EDCotizacion`. GitHub Actions runs `mvn verify` on Linux, Windows and macOS on
every push and pull request ([`ci.yml`](../../.github/workflows/ci.yml)).

## Layout

```
src/main/java/com/edcotizacion/
├── cliente/      Clients: automatic creation and autocomplete
├── comun/        Utilities (accent-insensitive search, text helpers, JPA converters)
├── config/       Defaults and quote numbering
├── cotizacion/   Quote/line entities, form records, calculations (Montos), service
├── demo/         Public demo: form record and request limits
├── empresa/      Issuing companies
├── escritorio/   Start-up, system tray, single instance, quit
├── pdf/          Layout + company data → HTML (Thymeleaf) → PDF (OpenHTMLtoPDF)
├── producto/     Products: automatic creation, last cost/price, autocomplete
├── seguridad/    Sign-in (Spring Security), users, failed attempts, request limits
└── web/          Controllers (pages and JSON API)
src/main/resources/
├── db/migration/   Database schema (Flyway)
├── pdf/            Default layout and gallery examples
├── static/         CSS, JavaScript (no inline scripts: CSP) and PDF fonts
└── templates/      Pages; templates/pdf/ is the PDF HTML
packaging/          jpackage script, icons and release notes
```

## Packaging locally

`jpackage` only builds packages for the system it runs on:

```bash
mvn package
packaging/empaquetar.sh 1.0.0 portable        # Linux: also deb and rpm; Windows: msi; macOS: dmg
```

Output goes to `target/dist`. The `.msi` needs [WiX Toolset 3](https://github.com/wixtoolset/wix3/releases);
the `.rpm`, `rpmbuild`; the `.deb`, `dpkg-deb`.

Container image: `podman build -t edcotizacion .`

Icons are generated with `java packaging/iconos/Icono.java <folder> 16,32,256,...`.

## Releasing

1. Update `<version>` in `pom.xml` and add the section to `CHANGELOG.md`.
2. Commit, tag and push:

   ```bash
   git tag v1.1.0
   git push origin main v1.1.0
   ```

3. [`publicar.yml`](../../.github/workflows/publicar.yml) checks that the tag matches `pom.xml`, runs
   the tests, builds the installers (Windows x64, Linux x64/arm64, macOS arm64/x64), pushes the image
   to `ghcr.io` and creates the release with the files and `SHA256SUMS.txt`.

## Conventions

- Records for forms and data; JPA entities with a protected constructor and intention-revealing methods.
- Spring Data JPA; logic in services; Flyway owns the schema.
- Bean Validation with Spanish messages; entities are never bound from requests.
- CSP-compatible frontend: no inline scripts or handlers (`data-*` attributes + `.js` files).
- Every change comes with tests (`*Test` unit, `*IT` integration).
