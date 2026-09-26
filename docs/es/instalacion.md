# Instalación

[English](../en/installation.md) · **Español**

Descarga el archivo de tu sistema en [Releases](https://github.com/Naraku86/EDCotizacion/releases/latest).
Todos incluyen Java: no hay que instalar nada más.

- [Windows](#windows)
- [macOS](#macos)
- [Linux](#linux)
- [Solo con Java (cualquier sistema)](#solo-con-java-cualquier-sistema)
- [Primer uso](#primer-uso)
- [Tus datos](#tus-datos)
- [Actualizar](#actualizar)
- [Desinstalar](#desinstalar)
- [Problemas frecuentes](#problemas-frecuentes)

> **Binarios sin firmar.** Firmar instaladores requiere certificados de pago, así que por ahora
> Windows y macOS muestran una advertencia la primera vez. Abajo se explica cómo abrirlos. Si
> quieres comprobar que el archivo es el publicado, compara su suma con `SHA256SUMS.txt`:
> `sha256sum -c SHA256SUMS.txt --ignore-missing` (Linux/macOS) o
> `Get-FileHash .\archivo.msi` (PowerShell).

## Windows

Windows 10 u 11 de 64 bits.

1. Descarga `EDCotizacion-<versión>-windows-x64.msi`. Si el navegador dice que el archivo no se
   descarga con frecuencia, elige **Conservar**.
2. Ábrelo. Si aparece **Windows protegió su PC** (SmartScreen), pulsa **Más información** y luego
   **Ejecutar de todas formas**.
3. Sigue el asistente. Se instala solo para tu usuario (no pide permisos de administrador) y crea
   accesos en el menú Inicio y en el escritorio.
4. Abre **EDCotizacion** desde el menú Inicio.

**Sin instalar:** descarga `EDCotizacion-<versión>-windows-x64-portable.zip`, descomprímelo y abre
`EDCotizacion\EDCotizacion.exe`.

## macOS

macOS 12 (Monterey) o posterior. Descarga el `.dmg` de tu procesador:
**Apple Silicon** (M1, M2, M3, M4) → `macos-arm64`; **Intel** → `macos-x64`.
Si no sabes cuál tienes: menú  › **Acerca de esta Mac**.

1. Abre el `.dmg` y arrastra **EDCotizacion** a **Aplicaciones**.
2. Abre EDCotizacion desde Aplicaciones. macOS dirá que no puede verificar al desarrollador.
3. Ve a **Ajustes del Sistema › Privacidad y seguridad**, baja hasta el mensaje sobre EDCotizacion
   y pulsa **Abrir de todas formas**. Confirma con tu contraseña.

Si macOS dice que la app **está dañada**, quita la marca de descarga desde la Terminal y ábrela de nuevo:

```bash
xattr -dr com.apple.quarantine /Applications/EDCotizacion.app
```

El icono de EDCotizacion aparece en la barra de menús (arriba a la derecha).

## Linux

Paquetes para x64 y arm64 (por ejemplo, Raspberry Pi 4/5 con sistema de 64 bits).

**Debian, Ubuntu, Linux Mint:**

```bash
sudo apt install ./EDCotizacion-<versión>-linux-x64.deb
```

**Fedora, RHEL, openSUSE:**

```bash
sudo dnf install ./EDCotizacion-<versión>-linux-x64.rpm     # openSUSE: sudo zypper install ./...
```

Se instala en `/opt/edcotizacion` y aparece en el menú de aplicaciones (sección Oficina). También
se abre con `/opt/edcotizacion/bin/EDCotizacion`.

**Sin instalar (cualquier distribución):**

```bash
tar xzf EDCotizacion-<versión>-linux-x64-portable.tar.gz
./EDCotizacion/bin/EDCotizacion
```

En algunos escritorios (por ejemplo GNOME) no hay bandeja del sistema y el icono no aparece. En ese
caso, cierra el programa con **Cerrar programa** en la barra superior de la app.

## Solo con Java (cualquier sistema)

Con Java 21 o posterior instalado:

```bash
java -jar EDCotizacion-<versión>.jar
```

## Primer uso

1. Al abrir EDCotizacion se abre el navegador en <http://localhost:8090>. Si no se abre, escribe esa
   dirección a mano.
2. Entra con usuario **admin** y contraseña **admin**. La app pide elegir una contraseña nueva
   (mínimo 8 caracteres) antes de dejarte continuar.
3. En **Configuración › Empresas y plantillas › Editar datos y plantilla** captura tu empresa, sube tu
   logo y elige un diseño. Consulta la [guía de uso](uso.md).

Para cerrar el programa: icono junto al reloj › **Salir**, o **Cerrar programa** en la barra de la app.
Si abres EDCotizacion cuando ya está abierto, solo se abre otra pestaña del navegador.

Por defecto la app solo acepta conexiones de esta computadora. Para usarla desde otras de tu red,
consulta [Configuración](configuracion.md#usar-la-app-desde-otras-computadoras).

## Tus datos

Todo se guarda en la carpeta `EDCotizacion` dentro de tu carpeta de usuario:

| Sistema | Carpeta |
|---|---|
| Windows | `C:\Users\<tu usuario>\EDCotizacion` |
| macOS | `/Users/<tu usuario>/EDCotizacion` |
| Linux | `/home/<tu usuario>/EDCotizacion` |

| Archivo | Qué es |
|---|---|
| `cotizaciones.db` | La base de datos: cotizaciones, clientes, productos, empresas, logos, plantillas y usuarios. |
| `edcotizacion.log` | Registro de la app, útil para reportar un problema. |
| `application.yml` | Opcional: tu [configuración](configuracion.md). |

**Respaldo:** cierra el programa y copia `cotizaciones.db` a otro lugar. Para restaurar, cierra el
programa y pon la copia en su lugar.

## Actualizar

1. Cierra el programa y respalda `cotizaciones.db`.
2. Instala la versión nueva encima de la anterior (el `.msi`, el `.dmg` o el paquete de Linux la
   reemplazan). Tus datos no se tocan.
3. Al abrirla, la base se actualiza sola si la versión nueva lo necesita. Para volver a una versión
   anterior después de eso, hay que restaurar el respaldo.

## Desinstalar

- **Windows:** Configuración › Aplicaciones › EDCotizacion › Desinstalar.
- **macOS:** arrastra EDCotizacion de Aplicaciones a la Papelera.
- **Linux:** `sudo apt remove edcotizacion` o `sudo dnf remove edcotizacion`.

La carpeta de datos no se borra; elimínala a mano si ya no la necesitas.

## Problemas frecuentes

**"El puerto 8090 está ocupado por otro programa".** Otro programa usa ese puerto. Crea el archivo
`application.yml` en tu carpeta de datos con:

```yaml
server:
  port: 8095
```

y abre <http://localhost:8095>.

**Olvidé la contraseña.** Cierra el programa y borra los usuarios de la base (tus cotizaciones no se
tocan). Al abrirlo de nuevo se crea otra vez `admin` / `admin` y pedirá cambiarla:

```bash
sqlite3 ~/EDCotizacion/cotizaciones.db "DELETE FROM usuario;"
```

En Windows, con [sqlite3](https://sqlite.org/download.html):
`sqlite3 %USERPROFILE%\EDCotizacion\cotizaciones.db "DELETE FROM usuario;"`.

**No abre o se cierra sola.** Revisa `edcotizacion.log` en tu carpeta de datos y, si reportas el
problema, incluye las últimas líneas.
