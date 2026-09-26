#!/usr/bin/env bash
# Crea los instaladores de EDCotizacion con jpackage (incluyen Java; no hace falta instalarlo).
# jpackage solo genera paquetes del sistema donde corre: el .msi se hace en Windows, el .dmg en
# macOS y el .deb/.rpm en Linux. GitHub Actions lo corre en los tres (ver .github/workflows).
#
#   packaging/empaquetar.sh <versión> <tipo>...
#   tipos: msi | exe | deb | rpm | dmg | pkg | portable (carpeta lista para usar, en .zip o .tar.gz)
#
# Ejemplo:  mvn package && packaging/empaquetar.sh 1.0.0 deb portable
# Los archivos quedan en target/dist con nombres como EDCotizacion-1.0.0-linux-x64.deb
set -euo pipefail

if [ $# -lt 2 ]; then
    sed -n '2,11p' "$0" | sed 's/^# \{0,1\}//'
    exit 1
fi
VERSION="$1"; shift
RAIZ="$(cd "$(dirname "$0")/.." && pwd)"
cd "$RAIZ"

JAR=target/edcotizacion.jar
[ -f "$JAR" ] || { echo "Falta $JAR: corre primero 'mvn package'" >&2; exit 1; }

case "$(uname -s)" in
    Linux*) SO=linux ;;
    Darwin*) SO=macos ;;
    MINGW*|MSYS*|CYGWIN*) SO=windows ;;
    *) echo "Sistema no soportado: $(uname -s)" >&2; exit 1 ;;
esac
case "$(uname -m)" in
    x86_64|amd64) ARQ=x64 ;;
    arm64|aarch64) ARQ=arm64 ;;
    *) ARQ="$(uname -m)" ;;
esac
BASE="EDCotizacion-$VERSION-$SO-$ARQ"

TRABAJO=target/jpackage
DIST=target/dist
rm -rf "$TRABAJO"
mkdir -p "$TRABAJO/entrada" "$DIST"
cp "$JAR" "$TRABAJO/entrada/"

COMUNES=(
    --name EDCotizacion
    --app-version "$VERSION"
    --vendor "EDCotizacion"
    --description "Cotizaciones en PDF para pequeños negocios"
    --copyright "Copyright (c) 2026 Edgar Vigueras. MIT License."
    --input "$TRABAJO/entrada"
    --main-jar edcotizacion.jar
    --java-options "-Dfile.encoding=UTF-8"
    # Solo los módulos de Java que usa la app (jdeps sobre el jar y sus dependencias), más los que
    # se cargan por reflexión: criptografía, juegos de caracteres, zip y formatos en/es.
    # Si una versión nueva de alguna dependencia necesita otro módulo, la app falla al arrancar
    # con NoClassDefFoundError: agregarlo aquí.
    --add-modules "java.base,java.compiler,java.desktop,java.instrument,java.logging,java.management,java.naming,java.net.http,java.prefs,java.rmi,java.scripting,java.security.jgss,java.sql,java.sql.rowset,java.transaction.xa,java.xml,jdk.charsets,jdk.crypto.ec,jdk.jfr,jdk.localedata,jdk.unsupported,jdk.zipfs"
    # sin símbolos de depuración, páginas man ni cabeceras; locales solo en/es
    --jlink-options "--strip-debug --no-man-pages --no-header-files --compress=zip-6 --include-locales=en,es"
)

case "$SO" in
    windows)
        POR_SO=(--icon packaging/iconos/edcotizacion.ico)
        INSTALADOR=(
            --win-menu --win-menu-group EDCotizacion --win-shortcut --win-dir-chooser
            # instalación por usuario: no pide permisos de administrador
            --win-per-user-install
            # fijo para siempre: así una versión nueva reemplaza a la anterior al instalarla
            --win-upgrade-uuid 443d6fe0-f08e-460b-a36f-28b46998b051
            --license-file LICENSE
        ) ;;
    macos)
        POR_SO=(--icon packaging/iconos/edcotizacion.icns
                --mac-package-identifier io.github.edcotizacion --mac-package-name EDCotizacion)
        INSTALADOR=(--license-file LICENSE) ;;
    linux)
        POR_SO=(--icon packaging/iconos/edcotizacion.png)
        INSTALADOR=(
            --linux-package-name edcotizacion --linux-shortcut
            --linux-menu-group Office --linux-app-category office
            --linux-rpm-license-type MIT
            --linux-deb-maintainer "${MANTENEDOR_DEB:-edcotizacion@users.noreply.github.com}"
        ) ;;
esac

comprimir() { # $1 carpeta a comprimir (dentro de $TRABAJO/salida), $2 archivo destino sin extensión
    local origen="$1" destino="$2"
    if [ "$SO" = windows ]; then
        if command -v 7z >/dev/null; then
            (cd "$TRABAJO/salida" && 7z a -tzip -bso0 "$RAIZ/$destino.zip" "$origen")
        else
            powershell -NoProfile -Command "Compress-Archive -Path '$TRABAJO/salida/$origen' -DestinationPath '$destino.zip'"
        fi
    else
        tar -C "$TRABAJO/salida" -czf "$destino.tar.gz" "$origen"
    fi
}

for TIPO in "$@"; do
    rm -rf "$TRABAJO/salida"
    if [ "$TIPO" = portable ]; then
        echo "==> $BASE-portable"
        jpackage "${COMUNES[@]}" "${POR_SO[@]}" --type app-image --dest "$TRABAJO/salida"
        comprimir "$(ls "$TRABAJO/salida")" "$DIST/$BASE-portable"
    else
        echo "==> $BASE.$TIPO"
        jpackage "${COMUNES[@]}" "${POR_SO[@]}" "${INSTALADOR[@]}" --type "$TIPO" --dest "$TRABAJO/salida"
        mv "$TRABAJO/salida"/*."$TIPO" "$DIST/$BASE.$TIPO"
    fi
done

echo "Listo:"
ls -l "$DIST"
