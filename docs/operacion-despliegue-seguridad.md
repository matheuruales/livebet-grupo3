# Operación, despliegue y seguridad

## 1. Entornos

| Entorno | Objetivo | Entrada |
|---|---|---|
| Desarrollo local | Construcción, pruebas y demostración sin nube | `localhost:8080`, `localhost:8081` |
| Producción académica | Exposición y validación en AWS | `18.224.52.92:8080`, `18.224.52.92:8081` |
| CI | Compilación y pruebas aisladas | GitHub Actions |

La IP actual no es elástica y puede cambiar si la instancia se detiene.

## 2. Requisitos locales

- Docker Engine o Docker Desktop con Compose v2.
- Al menos 8 GiB de RAM disponibles; 12–16 GiB ofrecen mayor estabilidad.
- Puertos 8080 y 8081 libres.
- Git para clonar y versionar.

Maven y Java no son obligatorios en el host porque la construcción se ejecuta dentro de contenedores.

## 3. Configuración

Crear el archivo privado a partir del ejemplo:

```bash
cp .env.example .env
```

### Variables

| Variable | Obligatoria en producción | Uso |
|---|---:|---|
| `POSTGRES_USER` | Sí | Usuario PostgreSQL |
| `POSTGRES_PASSWORD` | Sí | Contraseña PostgreSQL |
| `RABBITMQ_USER` | Sí | Usuario RabbitMQ |
| `RABBITMQ_PASSWORD` | Sí | Contraseña RabbitMQ |
| `JWT_SECRET` | Sí | Firma de tokens HMAC |
| `ADMIN_EMAIL` | Sí | Cuenta inicial administrativa |
| `ADMIN_PASSWORD` | Sí | Contraseña inicial administrativa |
| `MATCH_DURATION_MS` | No | Duración de partido, 60.000 por defecto |
| `GENERATION_INTERVAL_MS` | No | Frecuencia de creación, 10.000 en Compose |
| `MAIL_ENABLED` | No | Habilita SMTP |
| `MAIL_HOST` | Si correo está activo | Proveedor SMTP |
| `MAIL_PORT` | Si correo está activo | Puerto, normalmente 587 |
| `MAIL_USERNAME` | Si correo está activo | Cuenta SMTP |
| `MAIL_PASSWORD` | Si correo está activo | Contraseña de aplicación |
| `MAIL_FROM_ADDRESS` | Si correo está activo | Remitente |
| `MAIL_FROM_NAME` | No | Nombre visible |

Reglas:

- `.env` nunca se agrega a Git.
- Producción no debe utilizar valores predeterminados.
- Las contraseñas de aplicación se rotan si se comparten accidentalmente.
- No imprimir `PRODUCTION_ENV` en logs de GitHub.

## 4. Arranque local

```bash
docker compose up --build -d
./scripts/verify.sh
docker compose ps
```

En Apple Silicon, si una imagen no tiene variante ARM:

```bash
DOCKER_DEFAULT_PLATFORM=linux/amd64 docker compose up --build -d
```

Comprobaciones manuales:

```bash
curl -fsS http://localhost:8080/actuator/health
curl -fsS http://localhost:8081/actuator/health
curl -fsS http://localhost:8080/api/matches
```

## 5. Parada

Conservar datos:

```bash
docker compose down
```

Eliminar también PostgreSQL:

```bash
docker compose down -v
```

La segunda orden es destructiva y no debe usarse en producción.

## 6. Servicios y dependencias

| Servicio | Depende de | Health check |
|---|---|---|
| zookeeper | — | `srvr` |
| kafka | zookeeper | broker API versions |
| kafka-init | kafka | termina al crear topic |
| rabbitmq | — | port connectivity |
| postgres-db | — | `pg_isready` |
| match-generator | Kafka inicializado, RabbitMQ | Actuator interno |
| match-simulator | Kafka inicializado, RabbitMQ | Actuator interno |
| results-engine | Kafka, RabbitMQ, PostgreSQL | Actuator interno |
| betting-api | Kafka, RabbitMQ, PostgreSQL | HTTP 8081 |
| dashboard-backend | RabbitMQ | HTTP 8080 |
| email-worker | RabbitMQ | Actuator interno |

## 7. Redes y exposición

### Red `messaging`

Conecta Kafka, Zookeeper, RabbitMQ y servicios que publican o consumen mensajes.

### Red `persistence`

Conecta solamente PostgreSQL con `betting-api` y `results-engine`.

### Puertos

| Puerto | Exposición | Uso |
|---:|---|---|
| 8080 | Público | Dashboard, estáticos, WebSocket y estado de partidos |
| 8081 | Público | API autenticada |
| 5432 | Solo Compose | PostgreSQL |
| 5672 | Solo Compose | AMQP |
| 15672 | Solo Compose | RabbitMQ Management |
| 29092 | Solo Compose | Kafka interno |
| 2181 | Solo Compose | Zookeeper |

## 8. Recursos

Los servicios Java comparten límites base de 0,50 CPU y 384 MiB. Email worker usa 0,35 CPU y 256 MiB por réplica. Kafka dispone de 1 CPU y 1 GiB; RabbitMQ y PostgreSQL tienen 512 MiB.

La JVM utiliza:

```text
-XX:+UseContainerSupport
-XX:MaxRAMPercentage=75.0
-XX:+ExitOnOutOfMemoryError
```

`ExitOnOutOfMemoryError` permite que Docker reinicie un proceso corrupto en lugar de mantenerlo parcialmente operativo.

## 9. Dockerfiles

Cada microservicio usa dos etapas:

1. Maven 3.9.6 + Temurin 17 Alpine para descargar dependencias y empaquetar.
2. Temurin 17 JRE Alpine para ejecutar únicamente el JAR.

El runtime crea `appgroup` y `appuser`, copia el artefacto con propiedad correcta y ejecuta `USER appuser`.

## 10. AWS

### Infraestructura actual

- Región: `us-east-2`.
- EC2: Ubuntu Server 24.04 x86_64.
- Tipo utilizado: `m7i-flex.large`.
- Disco: gp3 de 40 GiB.
- Orquestación: Docker Compose.
- Runner GitHub self-hosted instalado en la instancia.

### Aprovisionamiento

`scripts/aws-provision.sh`:

1. crea o reutiliza un bucket temporal;
2. publica el archivo de entrega;
3. crea grupo de seguridad;
4. genera secretos de PostgreSQL, RabbitMQ y JWT;
5. crea rol SSM;
6. inicia EC2 con IMDSv2 obligatorio;
7. instala Docker;
8. descarga y levanta Compose.

Para un nuevo entorno se deben proporcionar explícitamente correo y contraseña administrativos; no usar valores de demostración.

## 11. Pipeline de despliegue

### Disparadores

- Pull request: pruebas y validaciones.
- Push a `main`: pruebas, paquete y despliegue.
- `workflow_dispatch`: ejecución manual.

### Entrega inmutable

El pipeline usa `git archive`, no el directorio de trabajo completo. Por tanto:

- no incluye `.env`;
- no incluye artefactos sin versionar;
- corresponde exactamente al commit;
- se verifica mediante SHA-256.

### Estructura de releases

```text
/opt/livebet/
├── releases/
│   ├── <commit-anterior>/
│   └── <commit-nuevo>/
└── current -> releases/<commit-nuevo>
```

El secreto `PRODUCTION_ENV` se instala con modo 600 dentro de la release.

### Reversión

Después de levantar la versión, el script consulta dashboard y API hasta 36 veces, cada cinco segundos. Si no se vuelven saludables, vuelve a ejecutar Compose desde la release anterior y no cambia `current`.

## 12. Operación diaria

Estado:

```bash
docker compose ps
docker stats --no-stream
```

Logs generales:

```bash
docker compose logs --tail=200
```

Flujo de partidos:

```bash
docker compose logs -f match-generator match-simulator results-engine
```

Correos:

```bash
docker compose logs -f email-worker
```

Outbox:

```bash
docker compose exec postgres-db psql -U betting -d betting -c \
  "select event_id, attempts, last_error from bet_event_outbox where published_at is null;"
```

## 13. Diagnóstico

### No aparecen partidos

1. comprobar `kafka-init`;
2. revisar `match-generator`;
3. revisar consumo en `results-engine`;
4. consultar `dashboard_notifications` y WebSocket.

### Partido no cierra

1. buscar publicación con `x-delay`;
2. revisar `match_simulation_queue`;
3. revisar DLQ;
4. comprobar `match-simulator`;
5. observar el reconciliador del motor.

### Apuesta permanece procesando

1. consultar `bet_event_outbox`;
2. revisar Kafka;
3. buscar el `eventId` en `processed_events`;
4. revisar auditoría `bets`;
5. revisar `SettlementReconciler` en `betting-api`.

### No llega correo

1. confirmar `MAIL_ENABLED=true`;
2. comprobar que SMTP usa contraseña de aplicación;
3. revisar logs del worker;
4. revisar `email_notifications`;
5. revisar `email_notifications_dlq`;
6. comprobar spam del destinatario.

### Dashboard conserva datos viejos

La interfaz resincroniza `/api/matches` cada 10 segundos y elimina partidos abiertos vencidos después de una gracia. Si el navegador conserva una versión antigua, hacer una recarga forzada una vez.

## 14. Recuperación ante desastres

### Reinicio de contenedor

Docker aplica `restart: unless-stopped`. El estado durable permanece en Kafka, PostgreSQL y RabbitMQ.

### Reinicio del motor

`results-engine` recupera eventos de partidos inconclusos desde PostgreSQL y consume Kafka desde el inicio con un grupo nuevo. `processed_events` evita repetir efectos externos.

### Pérdida de una release

Cambiar al directorio anterior y ejecutar:

```bash
docker compose up -d --build --remove-orphans
```

### Respaldo PostgreSQL

```bash
docker compose exec -T postgres-db pg_dump -U betting -d betting -Fc > livebet.dump
```

Restauración debe ensayarse en un entorno diferente antes de utilizarse sobre producción.

## 15. Seguridad

### Autenticación y autorización

- BCrypt con factor 12.
- Token stateless de 12 horas.
- Firma HMAC usando `JWT_SECRET` externo.
- Reglas específicas para público, autenticado y ADMIN.
- Cuenta desactivada no inicia sesión.

### Protección de datos

- Contraseñas nunca se almacenan en texto plano.
- La respuesta de usuario no incluye `passwordHash`.
- Las recargas guardan solo últimos cuatro dígitos.
- El CVV nunca se persiste ni se registra.
- Kafka y RabbitMQ no están publicados al exterior.

### Cadena de suministro

- Versiones explícitas de imágenes base.
- Paquete de despliegue con checksum.
- Solo se empaquetan archivos de Git.
- Pipeline comprueba ausencia de secretos comunes.
- Contenedores Java sin root.

### Riesgos conocidos y mitigación propuesta

| Riesgo | Estado actual | Mitigación recomendada |
|---|---|---|
| Tráfico HTTP | Académico | HTTPS con Nginx/ALB y certificado |
| CORS `*` | Facilita demo | Permitir solo dominio oficial |
| API sin rate limit | No implementado | Bucket por IP/usuario |
| Valores de desarrollo | Disponibles como fallback | Fallar al arrancar producción si no se configuran |
| `ddl-auto=update` | Conveniente para prototipo | Flyway y validación de esquema |
| EC2 única | Punto único de fallo | Servicios administrados y múltiples zonas |
| SMTP personal | Dependencia externa | Proveedor transaccional con métricas |

## 16. Checklist previa a exposición

- [ ] Dashboard y API responden `UP`.
- [ ] Hay al menos un partido abierto.
- [ ] El correo de demostración está configurado.
- [ ] El profesor tiene acceso al repositorio privado.
- [ ] No se muestran secretos en terminal o diapositivas.
- [ ] Se conoce el usuario de demostración.
- [ ] Se ha ensayado una apuesta completa.
- [ ] Se ha ensayado ruleta y recarga.
- [ ] Hay una consulta SQL de auditoría preparada.
- [ ] Los integrantes conocen su segmento de la exposición.
- [ ] Existe un plan local si falla Internet.
