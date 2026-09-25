# EDCotizacion

Aplicación local para hacer cotizaciones y generar su PDF.

- Clientes y productos se dan de alta solos: escribes el nombre y, si no existe, se agrega al guardar.
- Precio sugerido = costo + % de ganancia (30% por defecto), o escribes directo el precio final (con o sin IVA).
- IVA opcional por cotización; el envío va aparte y no lleva IVA.
- Historial con búsqueda, estados (Borrador / Enviada / Aceptada / Rechazada), cotizaciones vencidas en rojo, editar, duplicar y volver a generar el PDF.

**Stack:** Java 21 · Spring Boot 4 · Spring Data JPA (Hibernate) · Spring Security · Thymeleaf · SQLite + Flyway · OpenHTMLtoPDF

## Ejecutar

```bash
mvn package
java -jar target/edcotizacion.jar
```

Se abre el navegador en http://localhost:8090. Desde NetBeans basta con *Run*.

Pruebas: `mvn test` (unitarias) o `mvn verify` (también las de integración `*IT`, que levantan la
app completa con una base SQLite temporal; nunca tocan `~/EDCotizacion`).

## Dónde quedan los datos

Todo vive en `~/EDCotizacion/`:

| Archivo | Qué es |
|---|---|
| `cotizaciones.db` | Base de datos (SQLite): cotizaciones, configuración, datos de tu empresa, logo y diseño del PDF. **Para respaldar, copia este archivo.** |

## Entrar

La primera vez el usuario es **admin** y la contraseña **admin**. Mientras no los cambies, la app
muestra un aviso; se cambian en **Cuenta** (tu nombre de usuario, arriba a la derecha).

## Seguridad

- Contraseñas guardadas con BCrypt; mínimo 8 caracteres. Tras 5 intentos fallidos la cuenta se
  bloquea 5 minutos.
- Protección CSRF, cabeceras de seguridad y política CSP sin scripts en línea.
- Validación de todo lo que llega del navegador; el logo solo acepta PNG/JPG (máx. 1.5 MB) y
  ninguna petición puede pasar de 5 MB.
- **La app escucha en toda la red local por HTTP**: desde otra computadora la contraseña viaja sin
  cifrar. Si solo la usas en tu PC, arráncala con `--server.address=127.0.0.1`. Para usarla en red con
  cifrado, configura HTTPS (`server.ssl.*` de Spring Boot) o ponla detrás de un proxy con certificado.

## Plantilla del PDF

**Configuración › Editar plantilla** muestra la hoja tal como saldrá el PDF y se edita encima:

- Clic en cualquier texto (nombre, RFC, títulos, encabezados de la tabla, pie…) para cambiarlo.
  Lo que dejes vacío no se imprime.
- El logo se sube o se suelta en el recuadro punteado del encabezado.
- **Plantillas**: galería con ejemplos (Clásica, Minimalista, Moderna, Compacta y variantes de color),
  mostrados con tus datos.
- **Color**, **Carta / A4** y **Opciones** (nombre en mayúsculas, columna #, número de página…).
- Secciones opcionales: *+ Datos bancarios*, *+ Nota*, *+ Línea de firma*.
- *Ver PDF* sin guardar, deshacer/rehacer (Ctrl+Z / Ctrl+Y) y Ctrl+S para guardar.

El diseño se guarda como JSON en la tabla `config` (`plantilla.diseno`) y los datos de la empresa como
`empresa.*`. El HTML del PDF está en `src/main/resources/templates/pdf/cotizacion.html`; los ejemplos
de la galería en `src/main/resources/pdf/ejemplos.json`.

## Varias instalaciones en la misma PC

Cada carpeta de datos es una instalación independiente (base, empresa, plantilla y usuarios):

```bash
./iniciar.sh ~/EDCotizacion 8090          # tu empresa
./iniciar.sh ~/EDCotizacion-demo 8091     # otra, p. ej. para probar la versión genérica
```

En Windows: `iniciar.bat C:\ruta\datos 8091`. La sesión de cada puerto es independiente.
La carpeta `local/` está en `.gitignore` para guardar ahí scripts propios que no se publican.

Para usar otra carpeta: `java -Dapp.home=/ruta/datos -jar target/edcotizacion.jar`.
Otro puerto: `--server.port=9000`. Sin abrir el navegador: `--app.abrir-navegador=false`.

## Instalador (.exe / .deb / .rpm)

`jpackage` (viene con el JDK) crea un ejecutable que ya incluye Java. Se genera en el sistema
operativo destino (el `.exe` se hace en Windows):

```bash
mvn package
mkdir -p target/dist-in && cp target/edcotizacion.jar target/dist-in/
jpackage --name EDCotizacion --input target/dist-in --main-jar edcotizacion.jar \
         --dest target/dist --type app-image      # o: exe, msi, deb, rpm
```

En Windows, para `--type exe` agrega `--win-shortcut --win-menu` (requiere WiX Toolset).

## Estructura

```
src/main/java/com/edcotizacion/
├── comun/        Utilidades (búsqueda sin acentos, textos, convertidores JPA)
├── cotizacion/   Entidades Cotizacion/Partida, formulario (records), cálculos (Montos), servicio
├── cliente/      Alta automática y autocompletado
├── producto/     Alta automática, último costo/precio y autocompletado
├── pdf/          Diseño (bloques) + datos de empresa → HTML (Thymeleaf) → PDF
├── config/       Valores por defecto y folio consecutivo
├── seguridad/    Login (Spring Security) y usuarios
└── web/          Controladores (pantallas y API JSON del formulario)
src/main/resources/
├── templates/          Pantallas de la app
├── static/             app.css, cotizacion.js (formulario), plantilla.js (editor), fuentes del PDF
├── templates/pdf/      HTML del PDF, armado a partir de los bloques del diseño
├── pdf/                Diseño genérico por defecto
└── db/migration/       Esquema de la base (Flyway)
```

## Licencia

[MIT](LICENSE).
