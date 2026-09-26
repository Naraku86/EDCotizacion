# Uso

[English](../en/usage.md) · **Español**

## Cotizaciones

- **+ Nueva cotización** abre el formulario. Escribe el cliente: si ya existe se completa solo; si
  no, se da de alta al guardar. Lo mismo con los productos.
- Por cada producto puedes capturar el **costo** y se sugiere el precio con el % de ganancia
  (30% por defecto), o escribir directo el **precio unitario** o el **precio con IVA** y se calcula
  lo demás. Las columnas en ámbar (costo, ganancia) son internas: no salen en el PDF.
- El IVA es opcional por cotización. El **envío** va aparte y no lleva IVA.
- El folio se asigna al guardar (`COT-0001`, `COT-0002`…; prefijo y número en **Configuración**).
- En el historial: búsqueda por folio, cliente o producto; estados Borrador / Enviada / Aceptada /
  Rechazada; las vencidas se marcan en rojo; editar, duplicar y volver a generar el PDF.

## Plantilla del PDF

**Configuración › Empresas y plantillas › Editar datos y plantilla** muestra la hoja tal como saldrá
el PDF y se edita encima:

- Clic en cualquier texto (nombre, RFC, títulos, encabezados de la tabla, pie…) para cambiarlo. Lo que
  dejes vacío no se imprime.
- El logo se sube o se suelta en el recuadro punteado del encabezado (PNG o JPG).
- **Plantillas**: galería con ejemplos (Clásica, Minimalista, Moderna, Compacta y variantes de color),
  mostrados con tus datos.
- **Color**, **Carta / A4** y **Opciones** (nombre en mayúsculas, columna #, número de página…).
- Secciones opcionales: *+ Datos bancarios*, *+ Nota*, *+ Línea de firma*.
- *Ver PDF* sin guardar, deshacer/rehacer (Ctrl+Z / Ctrl+Y) y Ctrl+S para guardar.

![Editor de plantilla](../img/plantilla.png)

## Varias empresas

1. En **Configuración › Empresas y plantillas**, captura el nombre y pulsa **Agregar empresa**.
2. Edita sus datos, logo y diseño, y guarda la plantilla.
3. Al crear o editar una cotización, elige **Empresa emisora**. El PDF usa sus datos y plantilla.
4. El historial muestra la empresa debajo del folio y permite filtrar por emisora.

Los clientes, productos, folios consecutivos y preferencias se comparten entre empresas. Todos los
usuarios de la instalación pueden trabajar con todas las empresas.

## Demo público (`/demo`)

`http://localhost:8090/demo` abre sin iniciar sesión (también hay un enlace en la pantalla de
entrada). En una sola página se capturan los datos de la empresa y el logo, se elige un diseño,
colores y papel, se capturan cliente, productos y condiciones, y se descarga el PDF. La vista previa
se actualiza mientras se escribe.

![Demo](../img/demo.png)

- **No se guarda nada en el servidor**: el demo no usa la base de datos. Lo capturado solo se queda en
  el navegador del visitante; *Empezar de cero* lo borra.
- Límites contra abusos: hasta 50 productos, 10 PDF y 90 vistas previas por minuto por IP, y 2 PDF
  generándose a la vez.
- Se apaga con `app.demo.activo: false` (ver [Configuración](configuracion.md)).

## Usuario y contraseña

Tu nombre de usuario, arriba a la derecha, abre **Cuenta**, donde se cambian usuario y contraseña.
Tras 5 intentos fallidos desde una misma computadora hay que esperar 5 minutos.
