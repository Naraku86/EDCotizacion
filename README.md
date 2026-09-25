# EDCotizacion

Aplicación local para hacer cotizaciones y generar su PDF.

- Clientes y productos se dan de alta solos: escribes el nombre y, si no existe, se agrega al guardar.
- Precio sugerido = costo + % de ganancia (30% por defecto), o escribes directo el precio final (con o sin IVA).
- IVA opcional por cotización; el envío va aparte y no lleva IVA.
- Historial con búsqueda, estados (Borrador / Enviada / Aceptada / Rechazada), cotizaciones vencidas en rojo, editar, duplicar y volver a generar el PDF.

**Stack:** Java 21 · Spring Boot 4 · Thymeleaf · SQLite · OpenHTMLtoPDF

## Ejecutar

```bash
mvn package
java -jar target/edcotizacion.jar
```

Se abre el navegador en http://localhost:8090. Desde NetBeans basta con *Run*.

## Dónde quedan los datos

Todo vive en `~/EDCotizacion/`:

| Archivo | Qué es |
|---|---|
| `cotizaciones.db` | Base de datos (SQLite): cotizaciones, configuración, datos de tu empresa, logo y diseño del PDF. **Para respaldar, copia este archivo.** |

## Plantilla del PDF

**Configuración › Editar plantilla** abre un editor visual:

- A la izquierda, los bloques (encabezado, proveedor/cliente, tabla, totales, condiciones, título,
  párrafo, imagen, firma, línea, espacio, salto de página). Se arrastran a la hoja o se agregan con clic.
- En la hoja, los bloques se arrastran para cambiar el orden; con clic se editan en el panel derecho.
- Pestañas **Empresa** (nombre, RFC, teléfono, logo…; lo vacío no se imprime) y **Colores**.
- Vista previa en vivo, *Ver PDF* sin guardar, deshacer/rehacer (Ctrl+Z / Ctrl+Y), Ctrl+S para guardar.
- *Volver a la genérica* carga el diseño neutro que trae la app (`src/main/resources/pdf/diseno-generico.json`).

El diseño se guarda como JSON en la tabla `config` (`plantilla.diseno`) y los datos de la empresa como
`empresa.*`. El HTML que lo dibuja es `src/main/resources/templates/pdf/cotizacion.html`.

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
├── cotizacion/   Cotizacion, Partida, cálculos (Montos), servicio y repositorio
├── cliente/      Alta automática y autocompletado
├── producto/     Alta automática, último costo/precio y autocompletado
├── pdf/          Diseño (bloques) + datos de empresa → HTML (Thymeleaf) → PDF
├── config/       Valores por defecto y folio consecutivo
└── web/          Controladores (pantallas y API JSON del formulario)
src/main/resources/
├── templates/          Pantallas de la app
├── static/             app.css, cotizacion.js (formulario), plantilla.js (editor), fuentes del PDF
├── templates/pdf/      HTML del PDF, armado a partir de los bloques del diseño
├── pdf/                Diseño genérico por defecto
└── db/migration/       Esquema de la base (Flyway)
```
