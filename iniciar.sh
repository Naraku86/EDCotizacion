#!/usr/bin/env sh
# Inicia EDCotizacion.
#   ./iniciar.sh [carpeta-de-datos] [puerto] [opciones extra, p. ej. --app.abrir-navegador=false]
# Cada carpeta de datos es una instalación independiente (su base, empresa, plantilla y usuarios),
# así que puedes tener varias en la misma PC usando carpetas y puertos distintos.
DIR="$(cd "$(dirname "$0")" && pwd)"
DATOS="${1:-$HOME/EDCotizacion}"
PUERTO="${2:-8090}"
[ $# -gt 0 ] && shift
[ $# -gt 0 ] && shift
exec java -Dapp.home="$DATOS" -jar "$DIR/target/edcotizacion.jar" --server.port="$PUERTO" "$@"
