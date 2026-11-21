# Documentación técnica completa de LiveBet

## 1. Identificación

| Campo | Valor |
|---|---|
| Proyecto | Plataforma de apuestas deportivas en vivo |
| Ejercicio | 8 |
| Asignatura | Computación Ubicua |
| Grupo | Grupo 3, Sexto B |
| Integrantes | Luis Castillo, Bayron Jaramillo, Matheu Ruales y Carlos Burbano |
| Lenguaje principal | Java 17 |
| Framework | Spring Boot |
| Mensajería | Apache Kafka y RabbitMQ |
| Persistencia | PostgreSQL |
| Orquestación | Docker Compose |
| Nube | AWS EC2 |

## 2. Resumen ejecutivo

LiveBet es una plataforma distribuida que genera partidos aleatorios, mantiene una ventana temporal para apostar, simula marcadores, liquida apuestas y notifica resultados. Kafka funciona como log ordenado de hechos; RabbitMQ ejecuta tareas diferidas y distribuye notificaciones; PostgreSQL conserva auditoría, cuentas y saldos; WebSocket actualiza la interfaz en tiempo real.

La versión actual amplía el enunciado con autenticación, autorización por roles, saldo personal, historial, recargas simuladas, correos HTML, panel administrativo, ruleta global y despliegue continuo.

## 3. Objetivos

### 3.1 Objetivo general

Diseñar e implementar una arquitectura distribuida, observable y reproducible que coordine eventos, temporizadores, persistencia y notificaciones sin concentrar todas las responsabilidades en una sola aplicación.

### 3.2 Objetivos específicos

- Mantener un historial inmutable y ordenado de los eventos por partido.
- Ejecutar el cierre de partidos sin bloquear hilos Java con esperas activas.
- Proteger las reglas de negocio mediante un modelo de dominio rico.
- Liquidar apuestas de forma idempotente y mantener trazabilidad contable.
- Actualizar múltiples clientes en tiempo real.
- Distribuir el trabajo de correo entre consumidores concurrentes.
- Ejecutar los servicios como contenedores no privilegiados con límites de recursos.
- Automatizar pruebas, empaquetado, despliegue y reversión.

## 4. Alcance

### 4.1 Incluido

- Partidos de dos equipos generados aleatoriamente.
- Apertura, cierre, marcador y ganador.
- Apuestas con cuota fija 2.0.
- Registro, login, tokens, roles y panel administrativo.
- Débito, crédito, premio, reembolso y recarga simulada.
- Ruleta europea de cero único, compartida por todos los clientes.
- Correos de resultados y recibos.
- Historial combinado de apuestas deportivas y ruleta.
- Infraestructura local y despliegue académico en una EC2.

### 4.2 Fuera del alcance

- Dinero real, adquirencia bancaria o procesamiento PCI DSS.
- Cuotas dinámicas u odds de proveedores deportivos.
- Identificación de identidad, límites regulatorios o autoexclusión.
- Alta disponibilidad multi-zona.
- Cluster Kafka, RabbitMQ o PostgreSQL con réplica.
- Aplicación móvil nativa.

La pasarela y los saldos son una simulación académica. Nunca deben utilizarse datos bancarios reales.

## 5. Atributos de calidad

| Atributo | Decisión |
|---|---|
| Trazabilidad | Kafka y `processed_events` conservan hechos y payloads |
| Orden | Todos los eventos de un partido usan `matchId` como key Kafka |
| Resiliencia | Reintentos, DLQ, outbox, health checks y reinicio automático |
| Idempotencia | `eventId`, claves primarias y estados terminales |
| Mantenibilidad | Módulos, handlers, puertos, records y responsabilidad única |
| Seguridad | BCrypt, HMAC-SHA256, roles, variables de entorno y usuario no root |
| Escalabilidad | Particiones Kafka y workers de correo competitivos |
| Auditabilidad | Tablas de partidos, apuestas, eventos, solicitudes y transacciones |
| Portabilidad | Docker Compose y configuración externa |

## 6. Arquitectura lógica

La arquitectura combina cuatro estilos:

1. **Event Sourcing académico:** Kafka conserva `MATCH_CREATED`, `BET_PLACED` y `MATCH_FINISHED`.
2. **Procesamiento stateful:** `results-engine` reconstruye `MatchAggregate` en memoria.
3. **Mensajería orientada a tareas:** RabbitMQ programa simulaciones con `x-delay`.
4. **Arquitectura hexagonal parcial:** el dominio no conoce Spring ni brokers; puertos separan casos de uso y adaptadores.

El diagrama completo se encuentra en [diagramas.md](diagramas.md#2-arquitectura-de-contenedores).

## 7. Responsabilidad de cada módulo

### 7.1 `shared-contracts`

Contiene contratos inmutables compartidos:

- `MatchCreatedEvent`
- `BetPlacedEvent`
- `MatchFinishedEvent`
- `MatchSimulationCommand`
- `DashboardNotification`
- `EmailNotification`
- `EventType`

Los contratos son records serializables mediante Jackson. Transportan datos, no lógica de infraestructura.

### 7.2 `domain`

Es el núcleo independiente. Contiene:

- `MatchAggregate` como raíz del agregado.
- `Bet`, `Settlement`, `Money`, `MatchId`, `BetId` y `UserId`.
- Estados `OpenForBetsState`, `ClosedState` y `SettledState`.
- `PayoutStrategy` y `FixedOddsPayoutStrategy`.
- Excepciones de transiciones y apuestas cerradas.

No depende de Spring, JPA, Kafka o RabbitMQ.

### 7.3 `match-generator`

- Selecciona dos equipos distintos.
- Crea `matchId` y `eventId`.
- Publica `MATCH_CREATED` en `match_events`.
- Programa `MatchSimulationCommand` en `match.delayed`.
- Usa puertos `MatchEventPort` y `MatchTimerPort`.

### 7.4 `betting-api`

Es la entrada HTTP y propietaria de cuentas y saldos:

- Registro, login y perfil.
- Apuestas deportivas e historial.
- Recargas simuladas.
- Ruleta y sus apuestas.
- Administración de cuentas.
- Transactional Outbox para publicar `BET_PLACED`.
- Conciliación de premios y reembolsos.

### 7.5 `match-simulator`

- Consume `match_simulation_queue` después del retraso.
- Genera un marcador no negativo.
- Determina ganador de acuerdo con el marcador simulado.
- Publica `MATCH_FINISHED` con la misma clave de partido.
- Envía mensajes fallidos a `match_simulation_dlq`.

### 7.6 `results-engine`

Es el procesador central:

- Consume `match_events` desde `earliest`.
- Despacha cada evento a un handler específico.
- Mantiene agregados en `MatchRegistry`.
- Evalúa reglas del dominio.
- Liquida con `PayoutStrategy`.
- Audita en PostgreSQL.
- Emite notificaciones por RabbitMQ.
- Reconstruye partidos inconclusos al iniciar.
- Cierra partidos vencidos mediante reconciliación defensiva.

### 7.7 `dashboard-backend`

- Consume `notify.dashboard.*`.
- Mantiene una vista materializada acotada.
- Nunca reabre un partido ya finalizado por un mensaje viejo.
- Expone `/api/matches` para carga inicial.
- Difunde actualizaciones por `/ws`.
- Sirve HTML, CSS y JavaScript.

### 7.8 `email-worker`

- Consume `notify.email.*` desde una cola compartida.
- Renderiza HTML y texto plano.
- Envía por SMTP con STARTTLS.
- Permite dos réplicas competitivas.
- Aplica reintentos y DLQ.

## 8. Flujos de negocio

### 8.1 Creación y cierre de partido

1. El scheduler genera un partido.
2. `match-generator` publica el evento y programa el temporizador.
3. `results-engine` crea el agregado abierto.
4. RabbitMQ entrega el comando cuando vence `x-delay`.
5. `match-simulator` produce marcador y ganador.
6. `results-engine` cierra, liquida, persiste y notifica.

### 8.2 Apuesta deportiva

1. Spring Security valida el token.
2. Bean Validation comprueba `matchId`, selección y monto.
3. `BetService` carga la cuenta y debita el saldo.
4. Dentro de la misma transacción guarda `bet_requests` y `bet_event_outbox`.
5. La API responde `202 Accepted`.
6. El dispatcher publica el evento pendiente en Kafka.
7. El motor aplica la invariante del estado del partido.
8. El dashboard recibe el nuevo total.
9. Al finalizar, el reconciliador acredita premios o devuelve apuestas rechazadas.

### 8.3 Recarga simulada

1. El usuario indica un monto entre 5.000 y 2.000.000 COP.
2. Se valida número ficticio de 16 cifras, `MM/AA` y CVV de 3 o 4 cifras.
3. Solo se conservan los últimos cuatro dígitos.
4. Cuenta y transacción se actualizan dentro de una transacción.
5. Se publica `notify.email.recharge`.

### 8.4 Ruleta compartida

Todos los clientes consultan una ronda persistida con los mismos tiempos del servidor:

- `BETTING`: 30 segundos para apostar.
- `SPINNING`: 8 segundos de animación y cierre.
- `SETTLED`: 6 segundos para presentar el resultado.

La selección puede ser número, color, paridad, rango o docena. El multiplicador es 36 para un número, 3 para docenas y 2 para apuestas simples.

## 9. Consistencia y transacciones

### 9.1 Modelo de consistencia

El sistema adopta **consistencia eventual**. La respuesta `202` significa que la solicitud fue registrada y se publicará; no significa que el motor ya la incorporó al agregado.

### 9.2 Transactional Outbox

El problema resuelto es evitar este fallo:

1. debitar el saldo;
2. fallar antes de publicar en Kafka;
3. dejar dinero descontado sin evento.

`BetService` guarda cuenta, apuesta y outbox en la misma transacción PostgreSQL. `BetOutboxDispatcher` publica después y registra `publishedAt`. Si Kafka falla, incrementa `attempts` y conserva `lastError`.

### 9.3 Idempotencia de consumo

`ProcessedEventEntity.eventId` es clave primaria. Antes de ejecutar efectos externos, el procesador comprueba si el evento es nuevo. Durante una relectura:

- el estado en memoria puede reconstruirse;
- no se vuelven a guardar auditorías;
- no se reenvían correos;
- no se duplican créditos ni reembolsos.

### 9.4 Liquidación de saldo

La auditoría del motor determina el resultado. `SettlementService` cambia una solicitud solo si continúa `ACCEPTED_FOR_PROCESSING`:

- ganada: acredita el payout completo y marca `WON`;
- perdida: marca `LOST` con pago cero;
- rechazada: devuelve el stake y marca `REJECTED`.

Los estados terminales hacen que reintentar sea seguro.

## 10. Diseño orientado a objetos

### 10.1 Modelo rico

`MatchAggregate` no permite modificar listas o estados desde fuera. Las operaciones públicas expresan intenciones:

- `placeBet`
- `finish`
- `settle`

La colección de apuestas se devuelve como copia inmutable.

### 10.2 Value Objects

Los records validan al construirse:

- identificadores no vacíos;
- dinero no nulo ni negativo;
- apuesta estrictamente mayor que cero;
- equipo obligatorio.

### 10.3 State

`MatchState` define las operaciones disponibles. Sus métodos por defecto rechazan transiciones no válidas. Cada implementación habilita únicamente las operaciones permitidas.

### 10.4 Strategy

`PayoutStrategy` separa la política de cálculo. La estrategia actual devuelve `stake × 2` al ganador y cero al perdedor. Otra estrategia puede sustituirse sin modificar el agregado.

### 10.5 Dependency Inversion

Los adaptadores principales implementan puertos:

| Puerto | Adaptador |
|---|---|
| `MatchEventPort` | `KafkaMatchEventAdapter` |
| `MatchTimerPort` | `RabbitMatchTimerAdapter` |
| `FinishedEventPort` | `KafkaFinishedEventAdapter` |
| `BetEventPublisherPort` | `BetPublisher` |
| `BetEventOutboxPort` | `JpaBetEventOutbox` |
| `NotificationPort` | `NotificationPublisher` |
| `AccountEmailNotifier` | `RabbitAccountEmailNotifier` |

## 11. Contratos de mensajería

### 11.1 Kafka

| Evento | Productor | Consumidor | Propósito |
|---|---|---|---|
| `MATCH_CREATED` | match-generator | results-engine | Abrir partido |
| `BET_PLACED` | betting-api | results-engine | Registrar intención de apuesta |
| `MATCH_FINISHED` | match-simulator | results-engine | Cerrar y liquidar |

Todos se publican en `match_events` con `matchId` como key.

### 11.2 RabbitMQ

| Exchange | Tipo | Routing key | Destino |
|---|---|---|---|
| `match.delayed` | x-delayed-message | `match.simulate` | `match_simulation_queue` |
| `notifications` | topic | `notify.dashboard.*` | `dashboard_notifications` |
| `notifications` | topic | `notify.email.*` | `email_notifications` |

Las routing keys actuales incluyen `created`, `updated`, `finished`, `win`, `loss`, `rejected`, `recharge`, `roulette-win` y `roulette-loss`.

## 12. Manejo de fallos

| Fallo | Respuesta del sistema |
|---|---|
| Kafka no disponible al apostar | El outbox permanece pendiente y reintenta |
| Mensaje Kafka duplicado | `eventId` impide duplicar efectos |
| RabbitMQ tarda en entregar el cierre | Reconciliador detecta partidos vencidos |
| Simulación falla repetidamente | Mensaje termina en `match_simulation_dlq` |
| SMTP falla | Reintentos y `email_notifications_dlq` |
| Dashboard se desconecta | WebSocket reconecta y REST resincroniza cada 10 s |
| Evento OPEN antiguo | Dashboard no reabre partidos finalizados |
| Despliegue no pasa health check | Reversión automática |
| Reinicio del motor | Recuperación desde auditoría y Kafka |

## 13. Seguridad

### 13.1 Controles implementados

- Contraseñas BCrypt con coste 12.
- Tokens firmados con HMAC-SHA256 y expiración de 12 horas.
- Autorización `ROLE_ADMIN` para `/api/admin/**`.
- Sesiones stateless.
- Validación declarativa de requests.
- Contenedores Java no root.
- Brokers y base de datos sin puertos públicos.
- Secretos de producción en GitHub Environment.
- `.env` excluido de Git.
- SMTP mediante STARTTLS obligatorio.
- LiveBet no captura la tarjeta ni el CVV: el pago se completa en Checkout Pro.
- Webhook de Mercado Pago validado con firma HMAC y consulta server-to-server.
- Clave de idempotencia, referencias únicas y bloqueo transaccional contra doble abono.

### 13.2 Limitaciones conocidas

- El despliegue académico usa HTTP y debe migrarse a HTTPS para producción.
- CORS admite cualquier origen para facilitar la demostración.
- No hay rate limiting ni bloqueo progresivo de login.
- No se implementan requisitos regulatorios de apuestas reales.
- Los valores por defecto de desarrollo deben reemplazarse en producción.

## 14. Observabilidad

Cada servicio expone Actuator `health` e `info`. Los logs incluyen:

- identificador de partido;
- apuesta y estado;
- marcador y ganador;
- intento de publicación del outbox;
- envío o fallo de correo;
- recuperación del estado.

No se exponen métricas Prometheus ni trazas distribuidas; son mejoras futuras.

## 15. Despliegue

La plataforma se ejecuta en una EC2 Ubuntu 24.04 mediante Docker Compose. El despliegue conserva la arquitectura local, simplificando la exposición académica.

Los únicos puertos de aplicación publicados son:

- `8080`: dashboard y WebSocket;
- `8081`: API.

Kafka, Zookeeper, RabbitMQ y PostgreSQL usan redes internas.

## 16. CI/CD

La canalización realiza:

1. checkout;
2. preparación de Java 17;
3. `mvn verify`;
4. validación de Compose;
5. comprobación de secretos;
6. `git archive` del commit aprobado;
7. checksum SHA-256;
8. despliegue desde runner self-hosted;
9. health checks;
10. reversión automática si la versión falla.

## 17. Pruebas

La suite contiene 33 pruebas unitarias en 14 clases. No utiliza `@SpringBootTest`, bases de datos, brokers o sockets reales. Cubre dominio, orquestación, saldo, outbox, autenticación, ruleta, correos y dashboard.

Consulte [pruebas-y-trazabilidad.md](pruebas-y-trazabilidad.md) para la matriz detallada.

## 18. Deuda técnica y evolución

### Prioridad alta

- Configurar TLS mediante dominio y reverse proxy.
- Restringir CORS al dominio oficial.
- Eliminar credenciales predeterminadas de scripts de aprovisionamiento.
- Sustituir `ddl-auto=update` por Flyway.

### Prioridad media

- Añadir JaCoCo y umbral de cobertura.
- Incorporar pruebas Testcontainers para Kafka, RabbitMQ y PostgreSQL.
- Introducir métricas Prometheus y panel Grafana.
- Añadir puertos propios para persistencia del motor.
- Modelar explícitamente `CreatedState` si se exige el ciclo completo literal.

### Escalabilidad futura

- Kafka Streams con state store por partición.
- PostgreSQL administrado con respaldos automáticos.
- RabbitMQ administrado o clusterizado.
- Balanceador y múltiples instancias stateless de API y dashboard.
- Redis para sesión materializada de la ruleta si se separa de la API.

## 19. Glosario

| Término | Definición |
|---|---|
| Aggregate | Objeto que protege invariantes y cambios consistentes de un conjunto de entidades |
| Event Sourcing | Reconstrucción del estado a partir de una secuencia de eventos |
| Outbox | Tabla transaccional de mensajes pendientes por publicar |
| Idempotencia | Propiedad que permite repetir una operación sin duplicar efectos |
| DLQ | Cola que aísla mensajes fallidos después de agotar reintentos |
| Routing key | Clave que RabbitMQ usa para enrutar mensajes |
| Partition key | Clave que Kafka usa para escoger una partición |
| Consistencia eventual | Los componentes convergen al mismo estado después de propagarse los eventos |
| Value Object | Objeto inmutable definido por su valor y validaciones |
| Stateful consumer | Consumidor que mantiene una vista del estado derivada del stream |

## 20. Referencias internas

- [Diagramas](diagramas.md)
- [API y eventos](referencia-api-y-eventos.md)
- [Modelo de datos](modelo-de-datos.md)
- [Operación y seguridad](operacion-despliegue-seguridad.md)
- [Pruebas y trazabilidad](pruebas-y-trazabilidad.md)
