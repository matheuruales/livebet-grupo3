# Modelo de dominio y pruebas

## Agregado `MatchAggregate`

El agregado protege la colección de apuestas y su ciclo de vida. No expone setters. Sus operaciones públicas expresan intenciones de negocio:

- `create`
- `placeBet`
- `finish`
- `settle`

La máquina de estados permite apuestas únicamente en `OPEN_FOR_BETS`. `ClosedState` permite liquidar una vez y `SettledState` rechaza nuevas modificaciones.

## Objetos inmutables

`MatchId`, `BetId`, `UserId` y `Money` son records. Sus constructores compactos rechazan identificadores vacíos y montos negativos. La entidad `Bet` exige además que el importe sea estrictamente mayor que cero.

## Estrategia de pagos

`PayoutStrategy` desacopla la liquidación. La implementación de demostración usa `FixedOddsPayoutStrategy` con multiplicador 2.0. Puede reemplazarse sin modificar el agregado.

## Inversión de dependencias

El módulo `domain` no depende de Spring, Kafka, RabbitMQ ni JPA. Los servicios de aplicación convierten contratos externos en objetos de dominio y los adaptadores publican o persisten resultados.

## Pruebas aisladas

`MatchAggregateTest` verifica:

- apuesta válida durante el estado abierto;
- rechazo después del cierre;
- cálculo del pago ganador;
- validación temprana de dinero negativo.

`MatchEventPublisherTest` usa Mockito para comprobar una publicación en Kafka y una programación en RabbitMQ sin conexiones reales.

`EventProcessorTest` reconstruye una secuencia completa en memoria y verifica el guardado JPA y las routing keys. También comprueba que una apuesta tardía produzca una notificación de rechazo sin detener el consumidor.
