# Referencia de API, WebSocket y contratos de eventos

## 1. Convenciones HTTP

### URLs

- Dashboard: `http://HOST:8080`
- API: `http://HOST:8081`
- WebSocket: `ws://HOST:8080/ws`

### Autenticación

Los endpoints protegidos requieren:

```http
Authorization: Bearer <token>
Content-Type: application/json
```

El token contiene identificador, correo, rol y expiración; está firmado con HMAC-SHA256.

### Formato de errores

```json
{
  "timestamp": "2026-11-18T20:00:00Z",
  "error": "Solicitud inválida",
  "detail": "Revisa los campos enviados",
  "fieldErrors": {
    "amount": "debe ser mayor que 0"
  }
}
```

Los códigos principales son:

| Código | Significado |
|---:|---|
| 200 | Consulta o actualización completada |
| 202 | Apuesta guardada y aceptada para procesamiento asíncrono |
| 400 | Validación o regla de negocio inválida |
| 401 | Token ausente, inválido o expirado |
| 403 | Rol insuficiente |
| 409 | Conflicto de estado o recurso duplicado |
| 500 | Error interno no controlado |

## 2. Autenticación

### `POST /api/auth/register`

Acceso: público.

Request:

```json
{
  "name": "Ana Torres",
  "email": "ana@example.com",
  "password": "ClaveSegura123"
}
```

Validaciones:

- nombre entre 3 y 80 caracteres;
- correo válido y único;
- contraseña entre 8 y 72 caracteres.

Response:

```json
{
  "token": "eyJ...",
  "user": {
    "id": "u-a1b2c3d4",
    "name": "Ana Torres",
    "email": "ana@example.com",
    "role": "USER",
    "balance": 250000.00,
    "active": true,
    "createdAt": "2026-11-18T20:00:00Z"
  }
}
```

### `POST /api/auth/login`

Acceso: público.

```json
{
  "email": "ana@example.com",
  "password": "ClaveSegura123"
}
```

Devuelve el mismo formato de autenticación del registro.

### `GET /api/auth/me`

Acceso: token `USER` o `ADMIN`.

Devuelve el perfil y saldo actual. Es la fuente para refrescar el saldo después de una liquidación.

## 3. Apuestas deportivas

### `POST /api/bets`

Alias compatible: `POST /bet`.

Acceso: autenticado.

```json
{
  "matchId": "m-a1b2c3d4",
  "betOn": "Cúcuta",
  "amount": 10000
}
```

Validaciones:

- `matchId` obligatorio;
- `betOn` obligatorio;
- `amount` mínimo 0,01;
- cuenta activa;
- saldo suficiente.

Response `202 Accepted`:

```json
{
  "status": "ACCEPTED_FOR_PROCESSING",
  "betId": "b-a1b2c3d4",
  "balance": 240000.00,
  "message": "Apuesta registrada para procesamiento"
}
```

La respuesta no promete que el motor ya haya aceptado la apuesta. La validación temporal definitiva ocurre en `results-engine` al respetar el orden de Kafka.

### `GET /api/bets`

Acceso: autenticado.

Devuelve únicamente las apuestas del usuario actual:

```json
[
  {
    "betId": "b-a1b2c3d4",
    "matchId": "m-a1b2c3d4",
    "betOn": "Cúcuta",
    "amount": 10000.00,
    "status": "WON",
    "payout": 20000.00,
    "createdAt": "2026-11-18T20:00:00Z",
    "settledAt": "2026-11-18T20:01:00Z"
  }
]
```

Estados:

| Estado | Significado |
|---|---|
| `ACCEPTED_FOR_PROCESSING` | Guardada y pendiente de decisión o liquidación |
| `WON` | Selección ganadora y premio acreditado |
| `LOST` | Selección perdedora |
| `REJECTED` | Llegó después del cierre y fue reembolsada |

## 4. Recargas

### `POST /api/recharges`

Acceso: autenticado.

```json
{
  "amount": 50000,
  "cardholder": "ANA TORRES",
  "cardNumber": "1111 2222 3333 4444",
  "expiry": "01/20",
  "cvv": "123"
}
```

Validaciones:

- monto entre 5.000 y 2.000.000 COP;
- titular entre 3 y 80 caracteres;
- exactamente 16 cifras, permitiendo espacios;
- mes entre 01 y 12 en formato `MM/AA`;
- código de 3 o 4 cifras.

La fecha se valida por formato, no contra el calendario, porque es una simulación académica.

Response:

```json
{
  "reference": "r-a1b2c3d4",
  "amount": 50000.00,
  "cardLast4": "4444",
  "status": "APPROVED",
  "processedAt": "2026-11-18T20:00:00Z",
  "balance": 300000.00
}
```

No se almacenan el número completo ni el CVV.

## 5. Ruleta

### `GET /api/roulette/round`

Acceso: público.

Devuelve la ronda global y la hora del servidor:

```json
{
  "roundId": "r-a1b2c3d4",
  "status": "BETTING",
  "serverTime": "2026-11-18T20:00:00Z",
  "openedAt": "2026-11-18T19:59:50Z",
  "closesAt": "2026-11-18T20:00:20Z",
  "resolvesAt": "2026-11-18T20:00:28Z",
  "nextRoundAt": "2026-11-18T20:00:34Z",
  "result": null,
  "resultColor": null,
  "totalBets": 12,
  "recentResults": [7, 18, 0, 32]
}
```

### `POST /api/roulette/bets`

Acceso: autenticado.

```json
{
  "roundId": "r-a1b2c3d4",
  "selectionType": "COLOR",
  "selectionValue": "RED",
  "amount": 5000
}
```

Validaciones:

- ronda actual y en estado `BETTING`;
- monto entre 1.000 y 500.000;
- saldo suficiente;
- selección reconocida.

Response:

```json
{
  "betId": "rb-a1b2c3d4",
  "roundId": "r-a1b2c3d4",
  "selection": "Rojo",
  "amount": 5000.00,
  "multiplier": 2,
  "balance": 245000.00
}
```

Tipos de selección:

| Tipo | Valores | Multiplicador |
|---|---|---:|
| `NUMBER` | `0` a `36` | 36 |
| `COLOR` | `RED`, `BLACK` | 2 |
| `PARITY` | `EVEN`, `ODD` | 2 |
| `RANGE` | `LOW`, `HIGH` | 2 |
| `DOZEN` | `FIRST`, `SECOND`, `THIRD` | 3 |

### `GET /api/roulette/bets`

Acceso: autenticado.

Devuelve las jugadas del usuario con estados `PENDING`, `WON` o `LOST`.

## 6. Administración

Todos los endpoints requieren rol `ADMIN`.

### `GET /api/admin/users`

Devuelve usuarios ordenados por fecha descendente.

### `PATCH /api/admin/users/{id}/balance`

```json
{ "amount": 50000 }
```

El monto debe ser mayor o igual a 1.

### `PATCH /api/admin/users/{id}/role`

```json
{ "role": "ADMIN" }
```

Valores: `USER` o `ADMIN`.

### `PATCH /api/admin/users/{id}/active`

```json
{ "active": false }
```

Una cuenta desactivada no puede iniciar sesión ni mover saldo.

## 7. Dashboard

### `GET :8080/api/matches`

Acceso: público.

Devuelve la vista materializada para la carga inicial. Excluye partidos abiertos que superaron la gracia de cierre.

```json
[
  {
    "matchId": "m-a1b2c3d4",
    "status": "OPEN_FOR_BETS",
    "teams": ["Cúcuta", "Cali"],
    "winner": null,
    "homeScore": null,
    "awayScore": null,
    "bets": 3,
    "totalBet": 30000,
    "startsAt": "2026-11-18T20:00:00Z",
    "endsAt": "2026-11-18T20:01:00Z",
    "occurredAt": "2026-11-18T20:00:15Z"
  }
]
```

### `WS :8080/ws`

Cada mensaje es un `DashboardNotification` JSON completo. El cliente actualiza su mapa por `matchId` y vuelve a consultar REST cada 10 segundos como mecanismo de reconciliación.

## 8. Eventos Kafka

Topic: `match_events`.

Particiones: 3.

Retención: 604.800.000 ms, equivalente a siete días.

Key obligatoria: `matchId`.

### `MATCH_CREATED`

```json
{
  "eventId": "e-a1b2c3d4",
  "eventType": "MATCH_CREATED",
  "matchId": "m-a1b2c3d4",
  "teams": ["Cúcuta", "Cali"],
  "occurredAt": "2026-11-18T20:00:00Z",
  "endsAt": "2026-11-18T20:01:00Z"
}
```

### `BET_PLACED`

```json
{
  "eventId": "e-b1c2d3e4",
  "eventType": "BET_PLACED",
  "betId": "b-a1b2c3d4",
  "userId": "u-a1b2c3d4",
  "userEmail": "ana@example.com",
  "matchId": "m-a1b2c3d4",
  "betOn": "Cúcuta",
  "amount": 10000,
  "occurredAt": "2026-11-18T20:00:20Z"
}
```

### `MATCH_FINISHED`

```json
{
  "eventId": "e-c1d2e3f4",
  "eventType": "MATCH_FINISHED",
  "matchId": "m-a1b2c3d4",
  "winner": "Cúcuta",
  "homeScore": 2,
  "awayScore": 1,
  "occurredAt": "2026-11-18T20:01:00Z"
}
```

## 9. Comando diferido RabbitMQ

Exchange: `match.delayed`.

Tipo: `x-delayed-message` con routing interno `direct`.

Routing key: `match.simulate`.

Header: `x-delay` con duración del partido en milisegundos.

```json
{
  "matchId": "m-a1b2c3d4",
  "teams": ["Cúcuta", "Cali"]
}
```

## 10. Routing keys de notificación

| Routing key | Payload | Consumidor |
|---|---|---|
| `notify.dashboard.created` | `DashboardNotification` | dashboard-backend |
| `notify.dashboard.updated` | `DashboardNotification` | dashboard-backend |
| `notify.dashboard.finished` | `DashboardNotification` | dashboard-backend |
| `notify.email.win` | `EmailNotification` | email-worker |
| `notify.email.loss` | `EmailNotification` | email-worker |
| `notify.email.rejected` | `EmailNotification` | email-worker |
| `notify.email.recharge` | `EmailNotification` | email-worker |
| `notify.email.roulette-win` | `EmailNotification` | email-worker |
| `notify.email.roulette-loss` | `EmailNotification` | email-worker |

## 11. Compatibilidad y evolución

- Añadir campos opcionales al final de un record requiere coordinar productores y consumidores.
- Cambiar nombres o tipos es incompatible y debe versionarse.
- `eventId`, `eventType` y `matchId` no deben eliminarse.
- La key Kafka siempre debe coincidir con el `matchId` del payload.
- Nuevas routing keys pueden añadirse sin afectar las vinculaciones `notify.dashboard.*` y `notify.email.*`.
