# Desarrollo

[English](../en/development.md) · **Español**

## Requisitos

- JDK 21 (por ejemplo [Temurin](https://adoptium.net))
- Maven 3.9 o posterior

## Compilar y ejecutar

```bash
mvn package
java -jar target/edcotizacion.jar          # o: ./iniciar.sh  /  iniciar.bat
```

Desde NetBeans basta con *Run*. Para no mezclar pruebas con tus datos, usa otra carpeta:
`./iniciar.sh ~/EDCotizacion-dev 8091`.

## Pruebas

```bash
mvn test      # unitarias (*Test)
mvn verify    # también las de integración (*IT): levantan la app completa con una base SQLite temporal
```

Las pruebas nunca tocan `~/EDCotizacion`. GitHub Actions corre `mvn verify` en Linux, Windows y macOS
en cada push y pull request ([`ci.yml`](../../.github/workflows/ci.yml)).

## Estructura

```
src/main/java/com/edcotizacion/
├── cliente/      Alta automática y autocompletado
├── comun/        Utilidades (búsqueda sin acentos, textos, convertidores JPA)
├── config/       Valores por defecto y folio consecutivo
├── cotizacion/   Entidades Cotizacion/Partida, formulario (records), cálculos (Montos), servicio
├── demo/         Demo público: formulario (record) y límite de peticiones
├── empresa/      Empresas emisoras
├── escritorio/   Arranque, bandeja del sistema, una sola copia, cerrar programa
├── pdf/          Diseño + datos de empresa → HTML (Thymeleaf) → PDF (OpenHTMLtoPDF)
├── producto/     Alta automática, último costo/precio y autocompletado
├── seguridad/    Login (Spring Security), usuarios, intentos fallidos, límites de petición
└── web/          Controladores (pantallas y API JSON)
src/main/resources/
├── db/migration/   Esquema de la base (Flyway)
├── pdf/            Diseño genérico y ejemplos de la galería
├── static/         CSS, JavaScript (sin scripts en línea: CSP) y fuentes del PDF
└── templates/      Pantallas; templates/pdf/ es el HTML del PDF
packaging/          Script de jpackage, iconos y notas de release
```

## Empaquetar localmente

`jpackage` solo genera paquetes para el sistema donde corre:

```bash
mvn package
packaging/empaquetar.sh 1.0.0 portable        # Linux: también deb y rpm; Windows: msi; macOS: dmg
```

Los archivos quedan en `target/dist`. El `.msi` requiere [WiX Toolset 3](https://github.com/wixtoolset/wix3/releases);
el `.rpm`, `rpmbuild`; el `.deb`, `dpkg-deb`.

Imagen de contenedor: `podman build -t edcotizacion .`

Los iconos se generan con `java packaging/iconos/Icono.java <carpeta> 16,32,256,...`.

## Publicar una versión

1. Actualiza `<version>` en `pom.xml` y agrega la sección en `CHANGELOG.md`.
2. Commit, etiqueta y push:

   ```bash
   git tag v1.1.0
   git push origin main v1.1.0
   ```

3. [`publicar.yml`](../../.github/workflows/publicar.yml) comprueba que la etiqueta coincida con el
   `pom.xml`, corre las pruebas, arma los instaladores (Windows x64, Linux x64/arm64, macOS arm64/x64),
   publica la imagen en `ghcr.io` y crea el release con los archivos y `SHA256SUMS.txt`.

## Convenciones

- Records para formularios y datos; entidades JPA con constructor protegido y métodos con intención.
- Spring Data JPA; lógica en servicios; Flyway es dueño del esquema.
- Validación con Bean Validation y mensajes en español; nunca se enlazan entidades desde la petición.
- Frontend compatible con la CSP: sin scripts ni manejadores en línea (`data-*` + archivos `.js`).
- Cada cambio con sus pruebas (`*Test` unitarias, `*IT` de integración).
