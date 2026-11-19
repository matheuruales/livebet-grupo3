# Manual de usuario y administrador

## 1. Acceso

Abra el dashboard publicado por el equipo. La barra superior permite navegar entre:

- Partidos
- Ruleta en vivo
- Mis apuestas
- Recargar saldo
- Administración, solamente para rol ADMIN

El saldo visible corresponde a créditos de demostración y no representa dinero real.

## 2. Crear una cuenta

1. Seleccione **Crear cuenta**.
2. Escriba nombre, correo y contraseña.
3. La contraseña debe tener entre 8 y 72 caracteres.
4. Confirme el formulario.
5. La cuenta recibe 250.000 COP demo.

El correo no puede repetirse. La contraseña se almacena con BCrypt y nunca se muestra de nuevo.

## 3. Iniciar y cerrar sesión

1. Seleccione **Ingresar**.
2. Use el correo y contraseña registrados.
3. Al entrar se muestran nombre, avatar y saldo.
4. Use **Salir** para borrar la sesión guardada en el navegador.

El token expira después de 12 horas. Si la cuenta fue desactivada por un administrador no podrá iniciar sesión.

## 4. Apostar a un partido

1. Abra **Partidos**.
2. Seleccione un partido con etiqueta **ABIERTO · EN VIVO**.
3. Observe el tiempo restante.
4. Pulse **Voy con…** sobre el equipo deseado.
5. Escoja o escriba el monto.
6. Confirme la apuesta.

El saldo se descuenta inmediatamente. La apuesta queda inicialmente **EN PROCESO** porque el motor distribuido debe evaluarla respetando el orden de eventos.

### Resultado

- Si gana: estado **GANADA**, premio con cuota fija ×2 y saldo acreditado.
- Si pierde: estado **PERDIDA**, sin premio.
- Si llegó después del cierre: estado **REEMBOLSADA** y devolución del monto.

El correo llega tanto si gana como si pierde y muestra partido, marcador, selección, importe y premio.

## 5. Consultar historial

1. Abra **Mis apuestas**.
2. La lista combina partidos y ruleta.
3. Cada registro indica tipo, selección, contexto, fecha, monto y estado.
4. Use la paginación para consultar resultados anteriores.
5. Pulse **Actualizar** si desea forzar la consulta.

La vista se refresca automáticamente mientras permanece abierta.

## 6. Recargar saldo

La recarga es una simulación educativa.

1. Abra **Recargar saldo**.
2. Elija un monto rápido o escriba entre 5.000 y 2.000.000 COP.
3. Indique un titular ficticio.
4. Use cualquier número ficticio de exactamente 16 cifras.
5. Use una fecha `MM/AA` con mes entre 01 y 12.
6. Use un código ficticio de tres o cuatro cifras.
7. Confirme **Pagar y recargar**.

La plataforma muestra referencia, últimos cuatro dígitos y saldo nuevo. También envía un recibo al correo de la cuenta.

No utilice una tarjeta real. El número completo y el código de seguridad no se guardan.

## 7. Jugar ruleta

La ruleta es europea y utiliza los números 0–36. La ronda es la misma para todos los usuarios conectados.

### Estados

- **APUESTAS ABIERTAS:** se pueden seleccionar jugadas.
- **GIRANDO:** no se aceptan apuestas nuevas.
- **RESULTADO:** se presenta el número ganador.

### Jugadas

- Número individual: paga ×36.
- Rojo o negro: paga ×2.
- Par o impar: paga ×2; el cero no cuenta.
- 1–18 o 19–36: paga ×2; el cero no cuenta.
- Docena: paga ×3.

### Procedimiento

1. Abra **Ruleta en vivo**.
2. Seleccione ficha de monto.
3. Seleccione número o jugada exterior.
4. Revise el premio potencial.
5. Confirme antes de que el contador llegue a cero.
6. Espere el giro y resultado.

El saldo se debita al apostar. Después de la liquidación se actualiza y llega un correo de ganancia o pérdida.

## 8. Panel administrativo

Solo aparece con rol `ADMIN`.

### Consultar usuarios

El listado muestra nombre, correo, identificador, rol, estado y saldo.

### Agregar saldo

El botón correspondiente acredita 50.000 COP demo.

### Cambiar rol

Permite alternar entre USER y ADMIN. Debe conservarse al menos una cuenta administrativa conocida.

### Activar o desactivar

Una cuenta desactivada no puede iniciar sesión ni mover saldo. La información histórica permanece.

## 9. Indicadores y mensajes

| Mensaje | Significado |
|---|---|
| En tiempo real | WebSocket conectado |
| Reconectando | El navegador intenta restablecer WebSocket |
| Termina en | Tiempo de apuesta restante |
| Confirmando resultado | Gracia breve entre cierre y liquidación |
| En proceso | Evento pendiente de decisión |
| Ganada | Premio acreditado |
| Perdida | Resultado adverso |
| Reembolsada | Apuesta tardía devuelta |

Un partido no debe permanecer indefinidamente como **Confirmando resultado**. La página resincroniza cada 10 segundos.

## 10. Solución de problemas

### La página no carga

- confirme conexión a Internet;
- verifique que la URL incluya `:8080`;
- recargue el navegador.

### El saldo parece viejo

- abra otra sección y vuelva;
- pulse actualizar en historial;
- espere unos segundos después de la liquidación.

### No llega correo

- revise spam;
- confirme que la cuenta usa un correo real;
- espere los reintentos del sistema;
- solicite al administrador revisar el worker de correo.

### La apuesta no se registra

- confirme sesión activa;
- compruebe saldo;
- verifique que el partido o ronda sigan abiertos;
- revise el mensaje mostrado en el formulario.

## 11. Uso responsable

LiveBet es un proyecto académico. No procesa pagos reales, no acepta depósitos y no debe publicitarse como una casa de apuestas autorizada. Todos los montos, tarjetas y premios son ficticios.
