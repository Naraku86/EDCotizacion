## Downloads / Descargas

| System / Sistema | File / Archivo |
|---|---|
| Windows 10/11 (x64) | `EDCotizacion-*-windows-x64.msi` (installer) · `*-windows-x64-portable.zip` |
| macOS (Apple Silicon: M1–M4) | `EDCotizacion-*-macos-arm64.dmg` |
| macOS (Intel) | `EDCotizacion-*-macos-x64.dmg` |
| Linux (Debian, Ubuntu, Mint) | `EDCotizacion-*-linux-x64.deb` · `*-linux-arm64.deb` |
| Linux (Fedora, RHEL, openSUSE) | `EDCotizacion-*-linux-x64.rpm` · `*-linux-arm64.rpm` |
| Linux (any / cualquiera) | `*-linux-x64-portable.tar.gz` · `*-linux-arm64-portable.tar.gz` |
| Server / Servidor | `podman run -p 8090:8090 -v edcotizacion-datos:/data {{IMAGEN}}:{{VERSION}}` |
| Java 21 (any OS) | `EDCotizacion-*.jar` → `java -jar EDCotizacion-*.jar` |

Java is included in every installer. The binaries are **not signed**: Windows and macOS show a
warning the first time. See the [installation guide](https://github.com/{{REPO}}/blob/main/docs/en/installation.md).

Java viene incluido en todos los instaladores. Los binarios **no están firmados**: Windows y macOS
muestran una advertencia la primera vez. Consulta la [guía de instalación](https://github.com/{{REPO}}/blob/main/docs/es/instalacion.md).

Verify downloads / Verificar descargas: `sha256sum -c SHA256SUMS.txt`
