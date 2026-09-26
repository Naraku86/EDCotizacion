# EDCotizacion

Aplicación local para hacer cotizaciones y generar su PDF.

- Demo público en `/demo`: cualquiera captura su empresa y su cotización en una sola página y descarga el PDF, sin cuenta y sin guardar nada.
- Varias empresas en una misma instancia: cada una con sus datos, logo y plantilla; elige la emisora al cotizar.
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

Se abre el navegador en http://localhost:8090 (solo accesible desde esta computadora; ver
[Seguridad](#seguridad) para abrirla a la red). Desde NetBeans basta con *Run*.

Pruebas: `mvn test` (unitarias) o `mvn verify` (también las de integración `*IT`, que levantan la
app completa con una base SQLite temporal; nunca tocan `~/EDCotizacion`).

## Dónde quedan los datos

Todo vive en `~/EDCotizacion/`:

| Archivo | Qué es |
|---|---|
| `cotizaciones.db` | Base de datos (SQLite): cotizaciones, configuración, datos de tu empresa, logo y diseño del PDF. **Para respaldar, copia este archivo.** |

## Entrar

La primera vez el usuario es **admin** y la contraseña **admin**. Al entrar con esa contraseña la app
lleva a **Cuenta** y no permite usar ninguna otra pantalla hasta cambiarla.

## Seguridad

- Contraseñas guardadas con BCrypt; mínimo 8 caracteres. La contraseña de fábrica se debe cambiar
  antes de usar la app.
- Intentos fallidos de entrar: 5 con un mismo usuario desde una misma IP, o 20 desde una IP con
  cualquier usuario, bloquean 5 minutos solo esa combinación. Desde otra computadora se puede seguir
  entrando, así nadie puede dejar fuera al dueño de la cuenta. El mensaje es el mismo para contraseña
  incorrecta, usuario inexistente o bloqueo.
- Protección CSRF, cabeceras de seguridad y política CSP sin scripts en línea.
- Validación de todo lo que llega del navegador: montos con máximo 12 enteros y 4 decimales; logo
  PNG/JPG real, de máx. 1.5 MB y 2000 × 2000 píxeles (se revisa sin cargar la imagen completa);
  ninguna petición puede pasar de 5 MB, tampoco las que llegan por partes (chunked).
- El demo público no abre sesión: su token CSRF viaja en una cookie propia.
- **Por defecto la app solo acepta conexiones de esta computadora** (`server.address: 127.0.0.1`).
  Para usarla desde otras computadoras de la red, arráncala con `--server.address=0.0.0.0`
  (p. ej. `./iniciar.sh ~/EDCotizacion 8090 --server.address=0.0.0.0`). En red viaja por HTTP sin
  cifrar; para cifrar, configura HTTPS (`server.ssl.*` de Spring Boot) o ponla detrás de un proxy con
  certificado.

## Plantilla del PDF

**Configuración › Empresas y plantillas › Editar datos y plantilla** muestra la hoja tal como saldrá el PDF y se edita encima:

- Clic en cualquier texto (nombre, RFC, títulos, encabezados de la tabla, pie…) para cambiarlo.
  Lo que dejes vacío no se imprime.
- El logo se sube o se suelta en el recuadro punteado del encabezado.
- **Plantillas**: galería con ejemplos (Clásica, Minimalista, Moderna, Compacta y variantes de color),
  mostrados con tus datos.
- **Color**, **Carta / A4** y **Opciones** (nombre en mayúsculas, columna #, número de página…).
- Secciones opcionales: *+ Datos bancarios*, *+ Nota*, *+ Línea de firma*.
- *Ver PDF* sin guardar, deshacer/rehacer (Ctrl+Z / Ctrl+Y) y Ctrl+S para guardar.

Cada empresa guarda sus datos y diseño como JSON en la tabla `empresa`. Las cotizaciones
conservan su `empresa_id`, también al duplicarlas; sus PDF usan la plantilla actual de esa empresa. El HTML del PDF está en `src/main/resources/templates/pdf/cotizacion.html`; los ejemplos
de la galería en `src/main/resources/pdf/ejemplos.json`.

## Varias empresas en una misma instancia

1. En **Configuración › Empresas y plantillas**, captura el nombre y pulsa **Agregar empresa**.
2. Edita sus datos, logo y diseño, y guarda la plantilla.
3. Al crear o editar una cotización, elige **Empresa emisora**. El PDF usa sus datos y plantilla.
4. El historial muestra la empresa debajo del folio y permite filtrar por emisora.

Los clientes, productos, folios consecutivos y preferencias generales se comparten entre empresas.
La empresa no limita permisos: los usuarios de la instancia pueden trabajar con todas.
Al actualizar, la empresa y plantilla existentes se conservan como primera empresa y las cotizaciones
anteriores se vinculan a ella. Respalda la base antes de actualizar; la versión anterior de la aplicación
requiere restaurar ese respaldo si se desea volver atrás.

## Demo público (`/demo`)

`http://localhost:8090/demo` abre sin iniciar sesión (también hay un enlace en la pantalla de entrada).
En una sola página se capturan los datos de la empresa y el logo, se elige un diseño de la galería,
colores y papel, se capturan cliente, productos y condiciones, y se descarga el PDF. La vista previa se
actualiza mientras se escribe.

- **No se guarda nada en el servidor**: el demo no usa la base de datos (ni clientes, ni productos, ni
  folios). Lo capturado solo se queda en el `localStorage` del navegador del visitante; *Empezar de cero*
  lo borra.
- Para frenar abusos: hasta 50 productos, 10 PDF y 90 vistas previas por minuto por IP, y 2 PDF
  generándose a la vez. Al pasarse responde 429 con un mensaje.
- Se ajusta o apaga en `application.yml` (o con `--app.demo.activo=false` al arrancar):

```yaml
app:
  demo:
    activo: true            # false = /demo no existe y pide sesión como todo lo demás
    vistas-por-minuto: 90
    pdf-por-minuto: 10
    pdf-simultaneos: 2
```

Si la app queda detrás de un proxy inverso (nginx, Caddy…), configura
`server.forward-headers-strategy: native` para que el límite por IP use la IP real del visitante y no la
del proxy.

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
├── demo/         Demo público: formulario (record) y límite de peticiones
├── empresa/      Empresas emisoras
├── seguridad/    Login (Spring Security) y usuarios
└── web/          Controladores (pantallas y API JSON del formulario)
src/main/resources/
├── templates/          Pantallas de la app
├── static/             app.css, cotizacion.js (formulario), plantilla.js (editor), demo.js, fuentes del PDF
├── templates/pdf/      HTML del PDF, armado a partir de los bloques del diseño
├── pdf/                Diseño genérico por defecto
└── db/migration/       Esquema de la base (Flyway)
```

## Licencia

[MIT](LICENSE).
