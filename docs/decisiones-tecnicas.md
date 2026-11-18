# Decisiones técnicas

## Kafka como fuente de verdad

`match_events` conserva las decisiones del negocio como eventos inmutables. La clave `match_id` mantiene juntos y ordenados los eventos de cada agregado. El `results-engine` consume desde `earliest` en la primera ejecución y reconstruye los partidos. Cada contrato incluye `eventId`, lo que permite deduplicar en `processed_events`.

Kafka responde a la pregunta «¿qué ocurrió y en qué orden?». Su retención y capacidad de relectura permiten auditoría y reconstrucción.

## RabbitMQ para tiempo y distribución

El exchange `x-delayed-message` mantiene el comando de simulación hasta que vence `x-delay`. Así ningún proceso Java necesita permanecer dormido durante el partido. La imagen de RabbitMQ se construye sobre la versión oficial con una versión compatible del complemento, evitando imágenes comunitarias desactualizadas. El topic exchange `notifications` enruta mensajes por intención:

- `notify.dashboard.created`
- `notify.dashboard.updated`
- `notify.dashboard.finished`
- `notify.email.win`
- `notify.email.loss`
- `notify.email.rejected`

RabbitMQ responde a la pregunta «¿quién debe ejecutar esta tarea o recibir esta notificación ahora?». Dos instancias de `email-worker` compiten por una cola compartida y evidencian reparto de carga.

## PostgreSQL para auditoría formal

Kafka conserva eventos, mientras PostgreSQL ofrece consultas relacionales sobre partidos cerrados, apuestas y liquidaciones. `results-engine` guarda el agregado consolidado dentro de una transacción. La tabla `processed_events` actúa como bandeja de entrada idempotente.

## Consistencia

El sistema adopta consistencia eventual. `betting-api` confirma que recibió y publicó una solicitud, pero el agregado decide después si la apuesta cumple las invariantes. Una apuesta posterior al cierre genera una notificación `REJECTED`.

El prototipo utiliza una sola instancia de `results-engine` porque el estado vive en memoria. Escalar ese servicio requiere Kafka Streams con state stores, particionado explícito y restauración de changelog, o una vista materializada externa.

## Seguridad y operación

- Los contenedores Java ejecutan un usuario no privilegiado.
- La JVM limita el heap mediante `MaxRAMPercentage`.
- Compose define límites de CPU y memoria.
- PostgreSQL usa un volumen nombrado.
- Las redes separan mensajería y persistencia.
- Las credenciales se inyectan por variables de entorno.
- Las colas de mensajes fallidos aíslan notificaciones o simulaciones problemáticas.

Para una instalación académica en una sola EC2 se exponen únicamente 80/8080, 8081 y 22 restringido. Los puertos de Kafka, RabbitMQ y PostgreSQL deben permanecer cerrados a Internet.
