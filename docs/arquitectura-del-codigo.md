# Arquitectura y responsabilidades del código

LiveBet se organiza primero por microservicio. Cada módulo representa una capacidad desplegable y `domain` permanece libre de Spring, Kafka, RabbitMQ y JPA.

## Flujo de una solicitud

`BearerTokenFilter` autentica la sesión, el controlador recibe un record validado con `@Valid`, el servicio coordina el caso de uso y el dominio protege las invariantes. Los repositorios y adaptadores se invocan después de esas validaciones. Ningún controlador accede directamente a PostgreSQL o a un broker.

## Puertos y adaptadores

- `BetEventOutboxPort` separa el caso de uso de apuestas de PostgreSQL y Kafka.
- `BetEventPublisherPort` abstrae la publicación final en Kafka.
- `MatchEventPort` y `MatchTimerPort` separan el generador de Kafka y RabbitMQ.
- `FinishedEventPort` separa el simulador de Kafka.
- `NotificationPort` separa el motor de resultados de RabbitMQ.

## Responsabilidad única

`EventProcessor` solamente clasifica y despacha contratos. `MatchCreatedHandler`, `BetPlacedHandler` y `MatchFinishedHandler` implementan cada caso. `MatchRegistry` conserva la réplica en memoria. Cada DTO, repositorio, filtro y servicio tiene un archivo propio.

## Dominio enriquecido

`MatchAggregate` es la raíz del agregado: admite apuestas, finaliza y liquida sin setters. `MatchState` encapsula transiciones y `PayoutStrategy` desacopla el cálculo de premios. `MatchId`, `BetId`, `UserId` y `Money` son Value Objects inmutables con validación Fail-Fast.

## Recuperación e idempotencia

Kafka es la fuente de verdad. Al reiniciar, el motor reproduce eventos y reconstruye agregados. La auditoría por `eventId` evita repetir efectos externos. Las liquidaciones bloquean la apuesta en PostgreSQL y solo acreditan o reembolsan cuando continúa pendiente.
