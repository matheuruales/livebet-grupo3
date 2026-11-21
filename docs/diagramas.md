# Catálogo de diagramas de LiveBet

Los siguientes diagramas representan la implementación actual. Mermaid permite revisarlos en GitHub y modificarlos junto con el código.

## 1. Contexto del sistema

```mermaid
flowchart LR
    bettor[Jugador] -->|HTTPS/HTTP y WebSocket| livebet[LiveBet]
    admin[Administrador] -->|Gestión de usuarios| livebet
    livebet -->|Correo HTML y texto| gmail[Google SMTP]
    developer[Equipo del proyecto] -->|Push a main| github[GitHub Actions]
    github -->|Despliegue automático| livebet
```

## 2. Arquitectura de contenedores

```mermaid
flowchart LR
    browser[Navegador]
    generator[match-generator]
    api[betting-api]
    simulator[match-simulator]
    engine[results-engine]
    dashboard[dashboard-backend]
    worker1[email-worker 1]
    worker2[email-worker 2]
    kafka[(Kafka: match_events)]
    rabbit[(RabbitMQ)]
    postgres[(PostgreSQL)]
    smtp[Google SMTP]

    generator -->|MATCH_CREATED, key=matchId| kafka
    generator -->|x-delay| rabbit
    rabbit -->|match_simulation_queue| simulator
    simulator -->|MATCH_FINISHED, key=matchId| kafka
    browser -->|REST + Bearer token| api
    api -->|BET_PLACED, key=matchId| kafka
    api -->|cuentas, apuestas, outbox, ruleta| postgres
    kafka -->|reproducción desde earliest| engine
    engine -->|auditoría consolidada| postgres
    engine -->|notifications topic exchange| rabbit
    rabbit -->|notify.dashboard.*| dashboard
    rabbit -->|notify.email.*| worker1
    rabbit -->|notify.email.*| worker2
    dashboard -->|WebSocket /ws| browser
    worker1 --> smtp
    worker2 --> smtp
```

## 3. Topología Kafka y RabbitMQ

```mermaid
flowchart TB
    subgraph Kafka
      topic[match_events<br/>3 particiones<br/>key = match_id]
    end
    created[MATCH_CREATED] --> topic
    bet[BET_PLACED] --> topic
    finished[MATCH_FINISHED] --> topic
    topic --> engine[results-engine]

    subgraph RabbitMQ
      delayed[match.delayed<br/>x-delayed-message]
      simq[match_simulation_queue]
      simdlq[match_simulation_dlq]
      notifications[notifications<br/>topic exchange]
      dashq[dashboard_notifications]
      emailq[email_notifications]
      emaildlq[email_notifications_dlq]
      delayed -->|match.simulate| simq
      simq -. error .-> simdlq
      notifications -->|notify.dashboard.*| dashq
      notifications -->|notify.email.*| emailq
      emailq -. error .-> emaildlq
    end
```

## 4. Diagrama de clases del dominio de partidos

```mermaid
classDiagram
    class MatchAggregate {
      -MatchId id
      -List~String~ teams
      -List~Bet~ bets
      -List~Settlement~ settlements
      -String winner
      -MatchState state
      +create(MatchId, List) MatchAggregate
      +placeBet(Bet)
      +finish(String)
      +settle(PayoutStrategy)
      +status() MatchStatus
      +totalBet() BigDecimal
    }
    class MatchState {
      <<interface>>
      +status() MatchStatus
      +placeBet(MatchAggregate, Bet)
      +finish(MatchAggregate, String)
      +settle(MatchAggregate, PayoutStrategy)
    }
    class OpenForBetsState
    class ClosedState
    class SettledState
    class PayoutStrategy {
      <<interface>>
      +payout(Bet, String) Money
    }
    class FixedOddsPayoutStrategy
    class MatchId { <<record>> }
    class BetId { <<record>> }
    class UserId { <<record>> }
    class Money { <<record>> }
    class Bet { <<record>> }
    class Settlement { <<record>> }

    MatchAggregate *-- MatchId
    MatchAggregate *-- Bet
    MatchAggregate *-- Settlement
    MatchAggregate --> MatchState
    MatchState <|.. OpenForBetsState
    MatchState <|.. ClosedState
    MatchState <|.. SettledState
    PayoutStrategy <|.. FixedOddsPayoutStrategy
    Bet *-- BetId
    Bet *-- UserId
    Bet *-- Money
    Settlement *-- Bet
    Settlement *-- Money
```

## 5. Clases y puertos del procesamiento de eventos

```mermaid
classDiagram
    class KafkaEventListener
    class EventProcessor {
      +process(String)
    }
    class MatchCreatedHandler
    class BetPlacedHandler
    class MatchFinishedHandler
    class MatchRegistry
    class BetContactRegistry
    class NotificationPort {
      <<interface>>
      +dashboard(String, DashboardNotification)
      +email(String, EmailNotification)
    }
    class NotificationPublisher
    class ProcessedEventRepository
    class MatchAuditRepository
    class BetAuditRepository

    KafkaEventListener --> EventProcessor
    EventProcessor --> MatchCreatedHandler
    EventProcessor --> BetPlacedHandler
    EventProcessor --> MatchFinishedHandler
    EventProcessor --> ProcessedEventRepository
    MatchCreatedHandler --> MatchRegistry
    BetPlacedHandler --> MatchRegistry
    BetPlacedHandler --> BetContactRegistry
    BetPlacedHandler --> BetAuditRepository
    MatchFinishedHandler --> MatchRegistry
    MatchFinishedHandler --> BetContactRegistry
    MatchFinishedHandler --> MatchAuditRepository
    MatchCreatedHandler --> NotificationPort
    BetPlacedHandler --> NotificationPort
    MatchFinishedHandler --> NotificationPort
    NotificationPort <|.. NotificationPublisher
```

## 6. Clases principales de cuentas y apuestas

```mermaid
classDiagram
    class AuthController
    class BetController
    class RechargeController
    class RouletteController
    class AdminController
    class AuthService
    class BetService
    class RechargeService
    class RouletteService
    class SettlementService
    class UserAccount
    class BetRequestEntity
    class BetOutboxEntity
    class RechargeTransaction
    class RouletteRound
    class RouletteBet
    class UserAccountRepository
    class BetRequestRepository
    class BetOutboxRepository
    class RechargeRepository
    class RouletteRoundRepository
    class RouletteBetRepository

    AuthController --> AuthService
    BetController --> BetService
    RechargeController --> RechargeService
    RouletteController --> RouletteService
    AdminController --> UserAccountRepository
    AuthService --> UserAccountRepository
    BetService --> UserAccountRepository
    BetService --> BetRequestRepository
    BetService --> BetOutboxEntity
    RechargeService --> UserAccountRepository
    RechargeService --> RechargeRepository
    RouletteService --> RouletteRoundRepository
    RouletteService --> RouletteBetRepository
    RouletteService --> UserAccountRepository
    SettlementService --> BetRequestRepository
    SettlementService --> UserAccountRepository
```

## 7. Máquina de estados del partido

```mermaid
stateDiagram-v2
    [*] --> OPEN_FOR_BETS: MATCH_CREATED
    OPEN_FOR_BETS --> OPEN_FOR_BETS: BET_PLACED válido
    OPEN_FOR_BETS --> CLOSED: MATCH_FINISHED / finish()
    CLOSED --> SETTLED: settle(strategy)
    SETTLED --> [*]
    CLOSED --> CLOSED: BET_PLACED rechazado
    SETTLED --> SETTLED: BET_PLACED rechazado
```

## 8. Secuencia completa de un partido

```mermaid
sequenceDiagram
    autonumber
    participant G as match-generator
    participant K as Kafka
    participant R as RabbitMQ
    participant E as results-engine
    participant D as dashboard-backend
    participant B as Navegador
    participant S as match-simulator
    participant P as PostgreSQL

    G->>K: MATCH_CREATED (key=matchId)
    G->>R: MatchSimulationCommand + x-delay
    K->>E: MATCH_CREATED
    E->>R: notify.dashboard.created
    R->>D: actualización
    D-->>B: WebSocket: partido abierto
    Note over R: RabbitMQ conserva el comando hasta vencer el delay
    R->>S: match_simulation_queue
    S->>K: MATCH_FINISHED (key=matchId)
    K->>E: MATCH_FINISHED
    E->>E: finish() + settle(strategy)
    E->>P: partido y liquidaciones
    E->>R: dashboard.finished + email.win/loss
    R->>D: resultado final
    D-->>B: WebSocket: marcador y ganador
```

## 9. Secuencia de una apuesta con Transactional Outbox

```mermaid
sequenceDiagram
    autonumber
    participant U as Usuario
    participant API as betting-api
    participant DB as PostgreSQL
    participant O as BetOutboxDispatcher
    participant K as Kafka
    participant E as results-engine
    participant R as RabbitMQ

    U->>API: POST /api/bets + Bearer token
    API->>API: validar DTO, usuario y saldo
    API->>DB: BEGIN
    API->>DB: debitar saldo
    API->>DB: guardar bet_request
    API->>DB: guardar bet_event_outbox
    API->>DB: COMMIT
    API-->>U: 202 Accepted + nuevo saldo
    O->>DB: leer outbox pendiente
    O->>K: BET_PLACED (key=matchId)
    O->>DB: marcar publishedAt
    K->>E: procesar apuesta
    E->>R: notify.dashboard.updated
```

## 10. Apuesta tardía y reembolso

```mermaid
sequenceDiagram
    autonumber
    participant K as Kafka
    participant E as results-engine
    participant D as Dominio
    participant P as PostgreSQL
    participant A as betting-api reconciler
    participant R as RabbitMQ

    K->>E: BET_PLACED posterior a MATCH_FINISHED
    E->>D: match.placeBet(...)
    D-->>E: BettingClosedException
    E->>P: auditoría REJECTED
    E->>R: notify.email.rejected
    A->>P: localizar solicitud aún pendiente
    A->>P: acreditar importe una sola vez
    A->>P: estado REJECTED
```

## 11. Recarga de saldo

```mermaid
sequenceDiagram
    autonumber
    participant U as Usuario
    participant API as betting-api
    participant MP as Mercado Pago
    participant P as PostgreSQL
    participant R as RabbitMQ
    participant W as email-worker
    participant M as Google SMTP

    U->>API: POST /api/recharges/checkout
    API->>P: payment_recharge CREATING + idempotency key
    API->>MP: POST /v1/orders
    MP-->>API: orderId + checkoutUrl
    API-->>U: redirigir a Checkout Pro
    U->>MP: completar pago
    MP->>API: webhook order firmado
    API->>API: validar HMAC
    API->>MP: GET /v1/orders/{id}
    MP-->>API: processed / accredited
    API->>P: bloqueo + crédito + APPROVED
    API->>R: notify.email.recharge
    R->>W: notificación de recarga
    W->>M: correo HTML + texto
```

## 12. Ronda global de ruleta

```mermaid
stateDiagram-v2
    [*] --> BETTING: crear ronda
    BETTING --> SPINNING: closesAt
    SPINNING --> SETTLED: resolvesAt
    SETTLED --> BETTING: nextRoundAt / nueva ronda
```

```mermaid
sequenceDiagram
    autonumber
    participant C1 as Cliente 1
    participant C2 as Cliente 2
    participant API as betting-api
    participant DB as PostgreSQL
    participant R as RabbitMQ
    participant W as email-worker

    C1->>API: GET /api/roulette/round
    C2->>API: GET /api/roulette/round
    API-->>C1: misma roundId y tiempos
    API-->>C2: misma roundId y tiempos
    C1->>API: POST /api/roulette/bets
    API->>DB: débito y RouletteBet PENDING
    API->>DB: transición BETTING → SPINNING → SETTLED
    API->>DB: liquidar todas las apuestas de la ronda
    API->>R: email.roulette-win o email.roulette-loss
    R->>W: resultado individual
```

## 13. Modelo entidad-relación

```mermaid
erDiagram
    USER_ACCOUNTS ||--o{ BET_REQUESTS : realiza
    USER_ACCOUNTS ||--o{ RECHARGE_TRANSACTIONS : recibe
    USER_ACCOUNTS ||--o{ ROULETTE_BETS : realiza
    ROULETTE_ROUNDS ||--o{ ROULETTE_BETS : contiene
    MATCHES ||--o{ BETS : consolida

    USER_ACCOUNTS {
      string id PK
      string email UK
      string password_hash
      string role
      decimal balance
      boolean active
    }
    BET_REQUESTS {
      string id PK
      string user_id
      string match_id
      string bet_on
      decimal amount
      string status
      decimal payout
    }
    BET_EVENT_OUTBOX {
      string event_id PK
      string match_id
      text payload
      datetime published_at
      int attempts
    }
    MATCHES {
      string id PK
      string home_team
      string away_team
      string winner
      int home_score
      int away_score
      decimal total_bet
    }
    BETS {
      string id PK
      string match_id FK
      string user_id
      decimal stake
      decimal payout
      string status
    }
    PROCESSED_EVENTS {
      string event_id PK
      string event_type
      string match_id
      text payload
    }
    RECHARGE_TRANSACTIONS {
      string id PK
      string user_id
      decimal amount
      string card_last4
      string status
    }
    ROULETTE_ROUNDS {
      string id PK
      string status
      datetime closes_at
      datetime resolves_at
      int result_number
    }
    ROULETTE_BETS {
      string id PK
      string round_id
      string user_id
      string selection_type
      decimal amount
      decimal payout
      string status
    }
```

## 14. Despliegue en AWS

```mermaid
flowchart TB
    internet[Internet]
    github[GitHub Actions]
    smtp[Google SMTP]

    subgraph AWS[AWS us-east-2]
      subgraph EC2[EC2 Ubuntu 24.04 · Docker Compose]
        runner[Runner GitHub self-hosted]
        dash[dashboard-backend :8080]
        api[betting-api :8081]
        gen[match-generator]
        sim[match-simulator]
        eng[results-engine]
        ew1[email-worker 1]
        ew2[email-worker 2]
        kafka[Kafka + Zookeeper]
        rabbit[RabbitMQ]
        pg[(PostgreSQL)]
        volume[(Docker volume postgres_data)]
        pg --- volume
      end
      sg[Security Group]
    end

    internet -->|8080| sg --> dash
    internet -->|8081| sg --> api
    github --> runner
    runner -->|release inmutable| EC2
    ew1 --> smtp
    ew2 --> smtp
```

## 15. Segmentación de red

```mermaid
flowchart LR
    browser[Navegador]
    subgraph messaging[Red bridge: messaging]
      kafka[Kafka]
      zk[Zookeeper]
      rabbit[RabbitMQ]
      generator[match-generator]
      simulator[match-simulator]
      engine[results-engine]
      dashboard[dashboard-backend]
      api[betting-api]
      email[email-workers]
    end
    subgraph persistence[Red bridge: persistence]
      postgres[(PostgreSQL)]
    end
    browser -->|8080| dashboard
    browser -->|8081| api
    engine --> postgres
    api --> postgres
```

## 16. Pipeline CI/CD

```mermaid
flowchart LR
    push[Push a main] --> test[Compilar y ejecutar 33 pruebas]
    test --> compose[Validar Docker Compose]
    compose --> secrets[Comprobar secretos]
    secrets --> archive[git archive]
    archive --> checksum[SHA-256]
    checksum --> artifact[Artefacto inmutable]
    artifact --> runner[Runner de producción]
    runner --> release["/opt/livebet/releases/commit"]
    release --> containers[docker compose up]
    containers --> health{Dashboard y API UP}
    health -->|sí| current[Actualizar enlace current]
    health -->|no| rollback[Reversión automática]
```

## 17. Recuperación e idempotencia

```mermaid
flowchart TD
    restart[Reinicio results-engine] --> audit[Leer eventos de partidos inconclusos en processed_events]
    audit --> rebuild[Reconstruir MatchAggregate en memoria]
    restart --> replay[Kafka desde earliest con grupo nuevo]
    replay --> exists{eventId ya procesado}
    exists -->|sí| state[Reaplicar solo estado]
    exists -->|no| effects[Persistir y emitir efectos externos]
    state --> ready[Estado recuperado]
    effects --> ready
```
