# Installation

**English** · [Español](../es/instalacion.md)

Download the file for your system from [Releases](https://github.com/OWNER/EDCotizacion/releases/latest).
All of them include Java: nothing else needs to be installed.

- [Windows](#windows)
- [macOS](#macos)
- [Linux](#linux)
- [Java only (any system)](#java-only-any-system)
- [First run](#first-run)
- [Your data](#your-data)
- [Updating](#updating)
- [Uninstalling](#uninstalling)
- [Troubleshooting](#troubleshooting)

> **Unsigned binaries.** Code-signing certificates cost money, so for now Windows and macOS show a
> warning the first time. The steps below explain how to open them. To check that a file is the one
> that was published, compare it with `SHA256SUMS.txt`:
> `sha256sum -c SHA256SUMS.txt --ignore-missing` (Linux/macOS) or `Get-FileHash .\file.msi` (PowerShell).

## Windows

Windows 10 or 11, 64-bit.

1. Download `EDCotizacion-<version>-windows-x64.msi`. If the browser says the file is not commonly
   downloaded, choose **Keep**.
2. Open it. If **Windows protected your PC** (SmartScreen) appears, click **More info** and then
   **Run anyway**.
3. Follow the wizard. It installs for your user only (no administrator rights needed) and creates
   Start menu and desktop shortcuts.
4. Open **EDCotizacion** from the Start menu.

**Without installing:** download `EDCotizacion-<version>-windows-x64-portable.zip`, extract it and run
`EDCotizacion\EDCotizacion.exe`.

## macOS

macOS 12 (Monterey) or later. Download the `.dmg` for your processor:
**Apple Silicon** (M1, M2, M3, M4) → `macos-arm64`; **Intel** → `macos-x64`.
Not sure? Apple menu  › **About This Mac**.

1. Open the `.dmg` and drag **EDCotizacion** to **Applications**.
2. Open EDCotizacion from Applications. macOS says it cannot verify the developer.
3. Go to **System Settings › Privacy & Security**, scroll to the message about EDCotizacion and click
   **Open Anyway**. Confirm with your password.

If macOS says the app **is damaged**, remove the download flag from Terminal and open it again:

```bash
xattr -dr com.apple.quarantine /Applications/EDCotizacion.app
```

The EDCotizacion icon appears in the menu bar (top right).

## Linux

Packages for x64 and arm64 (for example, Raspberry Pi 4/5 with a 64-bit OS).

**Debian, Ubuntu, Linux Mint:**

```bash
sudo apt install ./EDCotizacion-<version>-linux-x64.deb
```

**Fedora, RHEL, openSUSE:**

```bash
sudo dnf install ./EDCotizacion-<version>-linux-x64.rpm     # openSUSE: sudo zypper install ./...
```

It is installed in `/opt/edcotizacion` and shows up in the applications menu (Office). It can also be
started with `/opt/edcotizacion/bin/EDCotizacion`.

**Without installing (any distribution):**

```bash
tar xzf EDCotizacion-<version>-linux-x64-portable.tar.gz
./EDCotizacion/bin/EDCotizacion
```

Some desktops (GNOME, for example) have no system tray, so the icon does not appear. In that case,
quit with **Cerrar programa** (close program) in the app's top bar.

## Java only (any system)

With Java 21 or later installed:

```bash
java -jar EDCotizacion-<version>.jar
```

## First run

1. Opening EDCotizacion opens your browser at <http://localhost:8090>. If it does not, type that
   address yourself.
2. Sign in with user **admin** and password **admin**. The app asks you to choose a new password
   (at least 8 characters) before you can continue.
3. In **Configuración › Empresas y plantillas › Editar datos y plantilla** (Settings › Companies and
   templates › Edit data and template) enter your company, upload your logo and pick a layout.
   See the [usage guide](usage.md).

To quit: tray icon › **Salir** (quit), or **Cerrar programa** in the app's top bar. Opening
EDCotizacion while it is already running just opens another browser tab.

By default the app only accepts connections from the same computer. To use it from other computers
on your network, see [Configuration](configuration.md#using-the-app-from-other-computers).

## Your data

Everything is stored in the `EDCotizacion` folder inside your user folder:

| System | Folder |
|---|---|
| Windows | `C:\Users\<your user>\EDCotizacion` |
| macOS | `/Users/<your user>/EDCotizacion` |
| Linux | `/home/<your user>/EDCotizacion` |

| File | What it is |
|---|---|
| `cotizaciones.db` | The database: quotes, clients, products, companies, logos, templates and users. |
| `edcotizacion.log` | Application log, useful when reporting a problem. |
| `application.yml` | Optional: your [configuration](configuration.md). |

**Backup:** quit the program and copy `cotizaciones.db` somewhere else. To restore, quit the program
and put the copy back.

## Updating

1. Quit the program and back up `cotizaciones.db`.
2. Install the new version over the old one (the `.msi`, `.dmg` or Linux package replaces it). Your
   data is not touched.
3. On first start, the database is upgraded automatically if the new version needs it. Going back to
   an older version after that requires restoring the backup.

## Uninstalling

- **Windows:** Settings › Apps › EDCotizacion › Uninstall.
- **macOS:** drag EDCotizacion from Applications to the Trash.
- **Linux:** `sudo apt remove edcotizacion` or `sudo dnf remove edcotizacion`.

The data folder is not removed; delete it yourself if you no longer need it.

## Troubleshooting

**"El puerto 8090 está ocupado por otro programa" (port 8090 is in use).** Another program uses that
port. Create `application.yml` in your data folder with:

```yaml
server:
  port: 8095
```

and open <http://localhost:8095>.

**Forgot the password.** Quit the program and delete the users from the database (your quotes are
kept). On next start `admin` / `admin` is created again and you will be asked to change it:

```bash
sqlite3 ~/EDCotizacion/cotizaciones.db "DELETE FROM usuario;"
```

On Windows, with [sqlite3](https://sqlite.org/download.html):
`sqlite3 %USERPROFILE%\EDCotizacion\cotizaciones.db "DELETE FROM usuario;"`.

**It does not start or closes by itself.** Check `edcotizacion.log` in your data folder and include
its last lines if you report the problem.
