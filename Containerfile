# Imagen de EDCotizacion para servidores (Podman o Docker). Para usarla en una PC conviene más el
# instalador de tu sistema (ver README).
#
#   podman build -t edcotizacion .
#   podman run -d --name edcotizacion -p 8090:8090 -v edcotizacion-datos:/data edcotizacion
#
# Los datos (base SQLite y registro) viven en el volumen /data.

# Compilación. Corre en la arquitectura de quien construye: el .jar es el mismo para amd64 y arm64.
FROM --platform=$BUILDPLATFORM docker.io/library/maven:3.9-eclipse-temurin-21 AS compilacion
WORKDIR /src
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src src
# las pruebas corren en CI antes de publicar la imagen
RUN mvn -B -q package -DskipTests

FROM docker.io/library/eclipse-temurin:21-jre
RUN groupadd --system edcot \
 && useradd --system --gid edcot --home-dir /data --shell /usr/sbin/nologin edcot \
 && mkdir /data && chown edcot:edcot /data
COPY --from=compilacion /src/target/edcotizacion.jar /app/edcotizacion.jar

# servidor: sin navegador, sin bandeja y sin "Cerrar programa"; escucha en todas las interfaces
# del contenedor (el puerto se publica con -p).
ENV APP_MODO=servidor \
    APP_HOME=/data \
    SERVER_ADDRESS=0.0.0.0 \
    APP_ABRIR_NAVEGADOR=false

USER edcot
VOLUME /data
EXPOSE 8090
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/edcotizacion.jar"]
