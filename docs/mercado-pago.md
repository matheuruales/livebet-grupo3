# Integración de Mercado Pago

LiveBet utiliza Checkout Pro mediante la Orders API. Los datos de la tarjeta se capturan exclusivamente en Mercado Pago; el frontend de LiveBet solo envía el monto de la recarga.

## Flujo

1. El usuario autenticado indica el monto.
2. `betting-api` crea `payment_recharges` con una referencia propia y una clave de idempotencia.
3. El backend crea la order en Mercado Pago con `X-Idempotency-Key` y devuelve `checkout_url`.
4. El navegador redirige al checkout oficial.
5. Mercado Pago envía un webhook HTTPS firmado con el evento `Order (Mercado Pago)`.
6. LiveBet valida la firma con el SDK oficial, consulta `/v1/orders/{id}` y comprueba referencia, moneda COP, monto y estado `processed/accredited`.
7. En una transacción con bloqueo de base de datos se acredita el saldo una sola vez y se envía el recibo por correo.

La URL de retorno nunca se usa como prueba del pago. Aunque una persona cambie sus parámetros, no puede aumentar el saldo sin una orden acreditada obtenida directamente de Mercado Pago.

## Variables

Copie `.env.example` a `.env` y complete:

```dotenv
MERCADOPAGO_ENABLED=true
MERCADOPAGO_ACCESS_TOKEN=APP_USR_...
MERCADOPAGO_WEBHOOK_SECRET=...
MERCADOPAGO_SUCCESS_URL=https://TU_DOMINIO/?payment=success
MERCADOPAGO_PENDING_URL=https://TU_DOMINIO/?payment=pending
MERCADOPAGO_FAILURE_URL=https://TU_DOMINIO/?payment=failure
SIMULATED_PAYMENTS_ENABLED=false
```

No agregue el Access Token ni la firma secreta a Git. `.env` ya está excluido del repositorio. Las URL públicas de producción deben usar HTTPS.

## Configuración en Mercado Pago

1. Cree o seleccione una aplicación en **Tus integraciones**.
2. Copie el Access Token privado al servidor.
3. En **Configuración de Webhooks**, configure tanto pruebas como producción con:

   `https://TU_API/api/recharges/mercadopago/webhook`

4. Active el evento **Order (Mercado Pago)**.
5. Copie la firma secreta generada a `MERCADOPAGO_WEBHOOK_SECRET`.
6. Primero pruebe con un usuario comprador de prueba distinto del usuario vendedor.

El despliegue académico que expone directamente `http://IP:8081` debe incorporar un dominio y TLS (por ejemplo, un proxy HTTPS o un balanceador con certificado) antes de recibir webhooks reales. Mercado Pago exige una URL HTTPS pública.

## Operación y seguridad

- Los montos aceptados son de 5.000 a 2.000.000 COP.
- Cada recarga tiene referencia propia y clave de idempotencia.
- `provider_order_id` e `idempotency_key` son únicos en PostgreSQL.
- El registro se bloquea durante la acreditación y el estado `APPROVED` es terminal para impedir dobles abonos.
- Un pago pendiente no cambia el saldo.
- Un monto o moneda diferente se rechaza.
- Las recargas simuladas están apagadas por defecto.
- Los recibos se publican a la cola de correo solamente después de la acreditación.

## Prueba técnica

Ejecute:

```bash
mvn clean test
docker compose config --quiet
```

Las pruebas `MercadoPagoRechargeServiceTest` comprueban creación sin crédito anticipado, idempotencia ante webhooks repetidos y rechazo de montos alterados.
