# Entornos de LiveBet

LiveBet se ejecuta en dos entornos aislados. Cada uno usa una instancia EC2, una base de datos PostgreSQL, brokers y contenedores propios. No comparten datos ni secretos.

| Entorno | Rama | GitHub Environment | Runner | Dominio |
|---|---|---|---|---|
| Desarrollo | `develop` | `development` | `livebet-development` | Se define al aprovisionar su EC2 |
| Producción | `main` | `production` | `livebet-production` | `livebet.beauty` |

## Flujo de despliegue

1. Un cambio enviado a `develop` ejecuta pruebas y, si pasan, llega únicamente a la EC2 de desarrollo.
2. Al fusionar el cambio comprobado en `main`, GitHub Actions repite las pruebas y despliega únicamente en producción.
3. Ambos despliegues crean un paquete inmutable, verifican checksum, esperan las comprobaciones de salud y conservan la versión anterior para reversión.

## Aislamiento de configuración

Los valores privados se guardan como secretos propios de cada GitHub Environment bajo el nombre `PRODUCTION_ENV`. El nombre se conserva por compatibilidad con el script de despliegue; en `development` contiene exclusivamente datos de desarrollo.

Producción usa Mercado Pago. Desarrollo deja Mercado Pago apagado y habilita recargas simuladas, de modo que no se puedan crear cobros reales durante pruebas.

El dominio público de producción es `livebet.beauty`; `www.livebet.beauty` redirige al mismo servicio. La dirección histórica basada en `nip.io` se mantiene como alternativa durante la transición del certificado HTTPS.

## Docker

En desarrollo se configura `COMPOSE_PROJECT_NAME=livebet-development`; por eso los contenedores aparecen identificados como el entorno de desarrollo. Producción sigue conservando sus contenedores existentes para evitar una interrupción durante el cambio.

El chequeo interno de la API en desarrollo se ejecuta desde su propio runner mediante `127.0.0.1:8081`. El puerto no se expone a Internet: el acceso público entra exclusivamente por HTTPS.
