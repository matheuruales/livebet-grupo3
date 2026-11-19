# Decisiones técnicas

## Kafka como fuente de verdad

`match_events` conserva las decisiones del negocio como eventos inmutables. Cada contrato declara `eventType` y la clave `match_id` mantiene juntos y ordenados los eventos del agregado. En cada inicio, `results-engine` consume desde `earliest` con un grupo de reconstrucción nuevo. Siempre reaplica los eventos al estado en memoria y usa `processed_events` para impedir que la reproducción duplique pagos, correos o auditorías. El dashboard sí recibe la fotografía reconstruida.

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

El evento de apuesta transporta la dirección registrada del usuario para que el motor y el worker permanezcan desacoplados de la tabla de cuentas. El worker usa Gmail por SMTP con STARTTLS obligatorio y genera versiones HTML y texto plano. Solo confirma el mensaje de RabbitMQ después de que el proveedor acepta el correo; ante errores se aplican reintentos y la cola de mensajes fallidos.

## PostgreSQL para auditoría formal

Kafka conserva eventos, mientras PostgreSQL ofrece consultas relacionales sobre partidos cerrados, apuestas y liquidaciones. `results-engine` guarda el agregado consolidado dentro de una transacción. La tabla `processed_events` actúa como bandeja de entrada idempotente.

## Consistencia

El sistema adopta consistencia eventual. `betting-api` confirma que guardó la solicitud. Un Transactional Outbox almacena el débito, la apuesta y el evento pendiente en la misma transacción; un worker publica a Kafka con reintentos y marca el evento como enviado. El agregado decide después si la apuesta cumple las invariantes. Una apuesta posterior al cierre genera `REJECTED` y el reconciliador devuelve su importe exactamente una vez.

El sistema utiliza una sola instancia activa de `results-engine` porque cada arranque reconstruye su estado desde el log. Escalarlo horizontalmente requeriría asignar la propiedad de particiones y una tienda de estado por réplica, por ejemplo con Kafka Streams.

## Seguridad y operación

- Los contenedores Java ejecutan un usuario no privilegiado.
- La JVM limita el heap mediante `MaxRAMPercentage`.
- Compose define límites de CPU y memoria.
- PostgreSQL usa un volumen nombrado.
- Las redes separan mensajería y persistencia.
- Las credenciales se inyectan por variables de entorno.
- Las colas de mensajes fallidos aíslan notificaciones o simulaciones problemáticas.

Para una instalación académica en una sola EC2 se exponen únicamente 80/8080, 8081 y 22 restringido. Los puertos de Kafka, RabbitMQ y PostgreSQL deben permanecer cerrados a Internet.
