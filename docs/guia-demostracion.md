# Guía de demostración de 15 minutos

## Preparación previa

1. Levantar el sistema al menos cinco minutos antes.
2. Abrir el dashboard, RabbitMQ Management y una terminal con los logs.
3. Configurar `MATCH_DURATION_MS=60000` y `GENERATION_INTERVAL_MS=45000`.
4. Tener listo un comando de apuesta, reemplazando partido y equipo.
5. Confirmar que PostgreSQL contiene al menos un resultado.

## Guion

### 0:00–1:00 — Problema

Luis presenta el objetivo: registrar apuestas mientras el partido permanece abierto, cerrar automáticamente y conservar una auditoría verificable.

### 1:00–3:30 — Arquitectura

Bayron explica que Kafka conserva los eventos y su orden por partido. RabbitMQ gestiona el temporizador y distribuye notificaciones. PostgreSQL facilita consultas históricas.

### 3:30–9:00 — Demostración

Carlos muestra el dashboard y selecciona un partido abierto. Ejecuta dos apuestas a equipos distintos. Enseña `202 Accepted` y el aumento en el contador del dashboard. Luego muestra el comando pendiente en RabbitMQ.

Al vencer el temporizador, el dashboard pasa el partido a resultados. En los logs aparecen el ganador, la liquidación y la instancia de `email-worker` que procesó cada notificación.

### 9:00–12:00 — Código y dominio

Matheu presenta `MatchAggregate`, los estados y `PayoutStrategy`. Ejecuta o muestra el resultado de las pruebas. Explica por qué una apuesta tardía produce `BettingClosedException`.

### 12:00–14:00 — Docker y auditoría

Luis muestra los Dockerfiles multietapa, el usuario no root y los límites. Bayron consulta la tabla `matches` en PostgreSQL.

### 14:00–15:00 — Cierre

El equipo resume la responsabilidad de cada tecnología y menciona la consistencia eventual como decisión explícita.

## Preguntas probables

**¿Por qué no usar solo Kafka?**  
Kafka conserva y reprocesa eventos. RabbitMQ aporta entrega diferida y routing por tópicos con colas competitivas de forma directa.

**¿Cómo se garantiza el orden?**  
Cada productor usa `match_id` como clave. Kafka asigna los eventos del partido a la misma partición.

**¿Qué sucede con una apuesta tardía?**  
El agregado la rechaza según su estado. El motor registra el evento como procesado y notifica al usuario.

**¿Cómo evitan duplicados?**  
Cada evento tiene `eventId` y PostgreSQL conserva los identificadores procesados.

**¿Cómo escalarían el motor?**  
Kafka Streams permitiría un state store por partición con changelog y restauración automática.

## Plan de respaldo

Si falla Internet, la demostración local no lo necesita una vez descargadas las imágenes. Si un broker tarda, mostrar la presentación y los registros de una ejecución anterior, y reiniciar únicamente el contenedor afectado.
