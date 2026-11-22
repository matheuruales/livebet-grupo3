#!/usr/bin/env python3
"""Genera la guía de estudio para la exposición de LiveBet."""
from pathlib import Path
from reportlab.lib import colors
from reportlab.lib.enums import TA_CENTER, TA_LEFT
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.lib.units import cm
from reportlab.platypus import (
    BaseDocTemplate, Frame, PageTemplate, Paragraph, Spacer, PageBreak,
    Table, TableStyle, KeepTogether, Preformatted, HRFlowable
)

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "output" / "pdf" / "Guia_Estudio_LiveBet_4_Integrantes.pdf"
OUT.parent.mkdir(parents=True, exist_ok=True)

INK = colors.HexColor("#1E2029")
ORANGE = colors.HexColor("#FF7832")
YELLOW = colors.HexColor("#FFE36C")
BLUE = colors.HexColor("#55C6EB")
GREEN = colors.HexColor("#7BD6A1")
CREAM = colors.HexColor("#FFF6DF")
MUTED = colors.HexColor("#5F606A")
LINE = colors.HexColor("#DDD4BE")

styles = getSampleStyleSheet()
styles.add(ParagraphStyle(name="TitleLive", parent=styles["Title"], fontName="Helvetica-Bold",
                          fontSize=32, leading=36, textColor=INK, alignment=TA_CENTER, spaceAfter=14))
styles.add(ParagraphStyle(name="SubTitleLive", parent=styles["Normal"], fontName="Helvetica-Bold",
                          fontSize=14, leading=19, textColor=MUTED, alignment=TA_CENTER))
styles.add(ParagraphStyle(name="H1Live", parent=styles["Heading1"], fontName="Helvetica-Bold",
                          fontSize=21, leading=25, textColor=INK, spaceBefore=4, spaceAfter=11))
styles.add(ParagraphStyle(name="H2Live", parent=styles["Heading2"], fontName="Helvetica-Bold",
                          fontSize=14, leading=18, textColor=INK, spaceBefore=9, spaceAfter=6))
styles.add(ParagraphStyle(name="BodyLive", parent=styles["BodyText"], fontName="Helvetica",
                          fontSize=9.7, leading=14, textColor=INK, spaceAfter=7))
styles.add(ParagraphStyle(name="SmallLive", parent=styles["BodyText"], fontName="Helvetica",
                          fontSize=8, leading=10.5, textColor=MUTED, spaceAfter=4))
styles.add(ParagraphStyle(name="Callout", parent=styles["BodyText"], fontName="Helvetica-Bold",
                          fontSize=9.3, leading=13.2, textColor=INK, spaceAfter=1))
styles.add(ParagraphStyle(name="CodeLive", parent=styles["Code"], fontName="Courier",
                          fontSize=7.4, leading=9.2, textColor=INK, backColor=colors.HexColor("#F4F1E8"),
                          borderColor=LINE, borderWidth=.5, borderPadding=7, spaceBefore=3, spaceAfter=8))
styles.add(ParagraphStyle(name="BulletLive", parent=styles["BodyText"], fontName="Helvetica",
                          fontSize=9.4, leading=13, textColor=INK, leftIndent=13, firstLineIndent=-9, spaceAfter=4))

def p(text, style="BodyLive"):
    return Paragraph(text, styles[style])

def bullets(items):
    return [p("• " + item, "BulletLive") for item in items]

def code(text):
    return Preformatted(text.strip("\n"), styles["CodeLive"])

def label(text, color=BLUE):
    t = Table([[p(text, "Callout")]], colWidths=[17.5*cm])
    t.setStyle(TableStyle([
        ("BACKGROUND", (0,0), (-1,-1), color),
        ("BOX", (0,0), (-1,-1), 1, INK),
        ("LEFTPADDING", (0,0), (-1,-1), 8), ("RIGHTPADDING", (0,0), (-1,-1), 8),
        ("TOPPADDING", (0,0), (-1,-1), 6), ("BOTTOMPADDING", (0,0), (-1,-1), 6),
    ]))
    return t

def callout(title, text, color=YELLOW):
    cell = [p(title, "Callout"), p(text, "BodyLive")]
    t = Table([[cell]], colWidths=[17.5*cm])
    t.setStyle(TableStyle([
        ("BACKGROUND", (0,0), (-1,-1), color), ("BOX", (0,0), (-1,-1), 1.2, INK),
        ("LEFTPADDING", (0,0), (-1,-1), 9), ("RIGHTPADDING", (0,0), (-1,-1), 9),
        ("TOPPADDING", (0,0), (-1,-1), 7), ("BOTTOMPADDING", (0,0), (-1,-1), 6),
    ]))
    return t

def section_title(name, owner, color):
    return [label(f"{owner.upper()}  |  {name.upper()}", color), Spacer(1, 0.25*cm)]

def diagram(nodes):
    data = [[p(n, "Callout") for n in nodes]]
    widths = [17.5*cm/len(nodes)]*len(nodes)
    t = Table(data, colWidths=widths)
    ts = [("BACKGROUND", (i,0), (i,0), [BLUE,YELLOW,ORANGE,GREEN][i % 4]) for i in range(len(nodes))]
    ts += [("GRID", (0,0), (-1,-1), 1, INK), ("VALIGN", (0,0), (-1,-1), "MIDDLE"),
           ("ALIGN", (0,0), (-1,-1), "CENTER"), ("TOPPADDING", (0,0), (-1,-1), 10),
           ("BOTTOMPADDING", (0,0), (-1,-1), 10)]
    t.setStyle(TableStyle(ts))
    return t

def page_num(canvas, doc):
    canvas.saveState()
    canvas.setStrokeColor(INK)
    canvas.setLineWidth(.7)
    canvas.line(1.4*cm, 1.35*cm, 19.6*cm, 1.35*cm)
    canvas.setFillColor(MUTED)
    canvas.setFont("Helvetica", 8)
    canvas.drawString(1.45*cm, .88*cm, "LiveBet - guía de estudio para exposición")
    canvas.drawRightString(19.55*cm, .88*cm, f"Página {doc.page}")
    canvas.restoreState()

def add_page(story, title, kicker=None):
    if kicker:
        story.append(label(kicker, BLUE)); story.append(Spacer(1, .22*cm))
    story.append(p(title, "H1Live"))
    story.append(HRFlowable(width="100%", thickness=1, color=LINE, spaceAfter=8))

def file_path(path):
    return f"<font face='Courier' size='8'>{path}</font>"

story = []

# Cover
story += [Spacer(1, 2.1*cm), label("LIVEBET  |  EXPOSICIÓN COMPLETA", ORANGE), Spacer(1, .7*cm),
          p("Guía de estudio", "TitleLive"), p("Arquitectura distribuida, reglas de negocio, mensajería, Docker, AWS y despliegue", "SubTitleLive"),
          Spacer(1, 1.2*cm), diagram(["Usuario", "Servicios", "Mensajería", "AWS"]), Spacer(1, 1.1*cm),
          callout("Cómo usar esta guía", "Cada persona estudia su bloque, practica el guion hablado, abre las rutas de código indicadas y repasa las preguntas. La exposición completa queda equilibrada: todos muestran funcionalidad real, código y arquitectura.", GREEN),
          Spacer(1, 1.0*cm), p("Integrantes: Matheu, Carlos, Bayron e Integrante 4 (reemplazar por su nombre).", "SubTitleLive"), PageBreak()]

# General
add_page(story, "Mapa completo del proyecto", "VISIÓN GENERAL")
story += [p("LiveBet es una plataforma de apuestas deportivas y ruleta en vivo. La web permite registrarse, recargar saldo, apostar, ver resultados y recibir correos. Internamente está dividida en servicios para que las tareas lentas o repetibles no bloqueen al usuario."),
          diagram(["Dashboard", "Caddy / HTTPS", "betting-api", "PostgreSQL"]), Spacer(1, .25*cm),
          diagram(["Kafka", "RabbitMQ", "Results Engine", "Email Worker"]), Spacer(1, .2*cm),
          p("<b>Servicios principales:</b> betting-api, dashboard-backend, match-generator, match-simulator, results-engine, email-worker, PostgreSQL, Kafka, ZooKeeper, RabbitMQ y Caddy."),
          callout("Idea que todos deben repetir", "El usuario ve una sola aplicación, pero por dentro los servicios colaboran mediante APIs, base de datos y eventos. No todos tienen que esperar entre sí para responder una pantalla.", YELLOW),
          p("<b>Orden lógico:</b> usuario y reglas → apuesta consistente → eventos y resultados → infraestructura y despliegue.", "BodyLive"), PageBreak()]

add_page(story, "Reparto equilibrado y tiempos", "PLAN DE EXPOSICIÓN")
table_data = [[p("Persona", "Callout"), p("Tiempo", "Callout"), p("Responsabilidad", "Callout"), p("Código / evidencia", "Callout")],
              [p("Matheu"), p("5-6 min"), p("RabbitMQ, simulación, correos, AWS y CI/CD"), p("SimulationRabbitConfig, EmailWorker, pipeline, EC2")],
              [p("Carlos"), p("5-6 min"), p("Modelo rico, State, Strategy, validación y pruebas"), p("MatchAggregate, Money, State, tests")],
              [p("Bayron"), p("5-6 min"), p("Apuesta, PostgreSQL, Outbox, Kafka e idempotencia"), p("BetService, Outbox, eventos, auditoría")],
              [p("Integrante 4"), p("5-6 min"), p("Usuario, API, autenticación, Docker y Caddy"), p("Auth, Security, Compose, Caddyfile")]]
t = Table(table_data, colWidths=[2.6*cm, 2*cm, 6.1*cm, 6.8*cm], repeatRows=1)
t.setStyle(TableStyle([("BACKGROUND", (0,0), (-1,0), YELLOW), ("GRID", (0,0), (-1,-1), .6, INK),
                       ("VALIGN", (0,0), (-1,-1), "TOP"), ("LEFTPADDING", (0,0), (-1,-1), 6), ("RIGHTPADDING", (0,0), (-1,-1), 6),
                       ("TOPPADDING", (0,0), (-1,-1), 6), ("BOTTOMPADDING", (0,0), (-1,-1), 6),
                       ("BACKGROUND", (0,1), (-1,-1), CREAM)]))
story += [t, Spacer(1, .45*cm), callout("Regla de la presentación", "Cada integrante debe mostrar un diagrama, al menos una clase real y una decisión técnica. Así nadie se limita a leer diapositivas.", GREEN), PageBreak()]

# Matheu
story += section_title("RabbitMQ, simulación, correo, AWS y CI/CD", "Matheu", ORANGE)
story += [p("<b>Objetivo de Matheu:</b> explicar cómo LiveBet procesa trabajo asíncrono y cómo pasa de código a una aplicación pública, sin confundir Kafka con RabbitMQ."),
          p("<b>Frase inicial:</b> “Ahora explicaré las tareas que no deben bloquear al usuario: la simulación de partidos, las notificaciones por correo y el despliegue automático en nuestros dos ambientes.”", "Callout"),
          p("<b>Ruta de exposición:</b> RabbitMQ → simulación → liquidación → correo → Docker/AWS → pipeline."),
          callout("Qué debe dominar", "RabbitMQ entrega trabajos a consumidores; Kafka distribuye eventos entre servicios. RabbitMQ encaja bien para retrasos, colas y reintentos. Kafka encaja bien para comunicar hechos del negocio a varios servicios.", YELLOW),
          PageBreak()]

add_page(story, "1. RabbitMQ: colas, exchange y retrasos", "MATHEU")
story += [p("RabbitMQ no es una base de datos. Es un broker de mensajes: recibe un mensaje, lo enruta a una cola y un consumidor lo procesa. En LiveBet se usa para la simulación diferida y para notificaciones por correo."),
          diagram(["Productor", "Exchange", "Queue", "Consumidor"]),
          p("<b>Conceptos que debes decir:</b>"), *bullets([
              "<b>Exchange:</b> recibe el mensaje y decide a qué cola enviarlo según una routing key.",
              "<b>Queue:</b> guarda el mensaje hasta que un consumidor pueda procesarlo.",
              "<b>Binding:</b> conecta exchange, routing key y queue.",
              "<b>Dead Letter Queue:</b> recibe mensajes que no se pudieron procesar correctamente.",
              "<b>AMQP sobre TCP:</b> AMQP define el protocolo de mensajería; TCP transporta sus datos de forma confiable."
          ]),
          p("<b>Clase para mostrar:</b> " + file_path("match-simulator/.../SimulationRabbitConfig.java")),
          code('CustomExchange delayedExchange() {\n  return new CustomExchange("match.delayed", "x-delayed-message",\n      true, false, Map.of("x-delayed-type", "direct"));\n}\n\nQueue simulationQueue() {\n  return QueueBuilder.durable("match_simulation_queue")\n      .deadLetterExchange("match.dlx")\n      .deadLetterRoutingKey("match.failed").build();\n}'),
          callout("Cómo explicarlo en 20 segundos", "El simulador manda una orden a un exchange con retraso. RabbitMQ espera el tiempo indicado y luego entrega el mensaje a la cola del simulador. Si falla repetidamente, el mensaje termina en la cola de errores para no perderlo.", GREEN), PageBreak()]

add_page(story, "2. Simulación, resultados y correo", "MATHEU")
story += [diagram(["Match Generator", "RabbitMQ", "Match Simulator", "Results Engine", "Email Worker"]), Spacer(1, .25*cm),
          p("<b>Secuencia real:</b>"), *bullets([
              "El generador crea un partido y publica su información.",
              "RabbitMQ programa el momento en que debe ejecutarse la simulación.",
              "Match Simulator decide el resultado y publica el evento de partido terminado.",
              "Results Engine liquida las apuestas y calcula los premios.",
              "Se envía una notificación a RabbitMQ y el Email Worker construye el correo." ]),
          p("<b>Clases para abrir:</b> " + file_path("match-simulator/.../SimulationWorker.java") + " y " + file_path("email-worker/.../EmailNotificationWorker.java")),
          callout("Pregunta probable: ¿por qué el correo no se envía desde la API?", "Porque el correo depende de un proveedor externo y puede tardar o fallar. Mandarlo por cola permite responder rápido al usuario y reintentar sin perder la notificación.", YELLOW),
          p("<b>Conexión con la ruleta:</b> la ruleta también trabaja por rondas: abre apuestas, gira y liquida. La diferencia es que sus reglas están en `RouletteService` y `RouletteRules`.", "BodyLive"), PageBreak()]

add_page(story, "3. AWS, dos ambientes y GitHub Actions", "MATHEU")
env_table = [[p("", "Callout"), p("Desarrollo", "Callout"), p("Producción", "Callout")],
             [p("Rama", "Callout"), p("develop"), p("main")],
             [p("URL", "Callout"), p("https://livebet.13.58.103.66.nip.io"), p("https://livebet.beauty")],
             [p("EC2", "Callout"), p("livebet-development"), p("grupo3-livebet")],
             [p("Pagos", "Callout"), p("Simulados"), p("Mercado Pago")],
             [p("Contenedores", "Callout"), p("livebet-development-*") , p("grupo3-live-betting-*")]]
tt = Table(env_table, colWidths=[3.3*cm, 7.1*cm, 7.1*cm])
tt.setStyle(TableStyle([("BACKGROUND", (0,0), (-1,0), BLUE), ("BACKGROUND", (0,1), (0,-1), YELLOW),
                        ("GRID", (0,0), (-1,-1), .6, INK), ("VALIGN", (0,0), (-1,-1), "TOP"),
                        ("LEFTPADDING", (0,0), (-1,-1), 6), ("RIGHTPADDING", (0,0), (-1,-1), 6),
                        ("TOPPADDING", (0,0), (-1,-1), 6), ("BOTTOMPADDING", (0,0), (-1,-1), 6)]))
story += [tt, Spacer(1,.35*cm),
          p("<b>Pipeline:</b> el archivo " + file_path(".github/workflows/pipeline.yml") + " selecciona el runner por rama. Primero ejecuta pruebas, luego crea un paquete con checksum, despliega en la EC2 correcta y consulta `actuator/health` para confirmar que quedó disponible."),
          code('develop  → GitHub Environment: development → runner: livebet-development\nmain     → GitHub Environment: production  → runner: livebet-production\n\nPruebas → paquete inmutable → despliegue → health check'),
          callout("Cierre de Matheu", "RabbitMQ desacopla tareas lentas; los dos ambientes evitan probar cambios directamente en producción; y GitHub Actions asegura que el mismo proceso de pruebas y despliegue se aplique siempre.", GREEN), PageBreak()]

# Carlos
story += section_title("Modelo rico, reglas, State, Strategy y pruebas", "Carlos", GREEN)
story += [p("<b>Objetivo de Carlos:</b> demostrar que LiveBet no es solo controladores y tablas: las reglas importantes viven en el dominio."),
          p("<b>Frase inicial:</b> “Después de ver cómo entra una apuesta al sistema, voy a mostrar cómo protegemos sus reglas. Usamos un modelo rico: las clases no solo guardan datos, también deciden qué operaciones son válidas.”", "Callout"),
          PageBreak()]

add_page(story, "4. MatchAggregate: el modelo rico", "CARLOS")
story += [p("`MatchAggregate` representa un partido y reúne las operaciones que cambian su ciclo de vida. Es rico porque tiene comportamiento, validaciones y encapsula su estado."),
          code('public void placeBet(Bet bet) {\n  if (!teams.stream().anyMatch(t -> t.equalsIgnoreCase(bet.team())))\n      throw new IllegalArgumentException("El equipo no participa en el partido");\n  if (bets.stream().anyMatch(b -> b.id().equals(bet.id()))) return;\n  state.placeBet(this, bet);\n}\n\npublic void finish(String winner) { ... }\npublic void settle(PayoutStrategy strategy) { ... }'),
          p("<b>Reglas para memorizar:</b>"), *bullets([
              "Un partido requiere exactamente dos equipos diferentes.",
              "No se acepta una apuesta por un equipo que no participa.",
              "Una apuesta repetida se ignora por su identificador.",
              "No se puede apostar cuando el partido ya cerró.",
              "No se puede liquidar antes de cerrar ni liquidar dos veces."
          ]),
          p("<b>Ruta:</b> " + file_path("domain/src/main/java/com/grupo3/betting/domain/match/MatchAggregate.java")),
          callout("Respuesta corta: ¿por qué es un modelo rico?", "Porque las reglas viven junto con el estado que protegen. Nadie puede modificar libremente equipos, apuestas o estado mediante setters públicos.", YELLOW), PageBreak()]

add_page(story, "5. State, Strategy y Value Objects", "CARLOS")
story += [diagram(["OPEN_FOR_BETS", "CLOSED", "SETTLED"]),
          p("<b>Patrón State:</b> cada estado permite una operación distinta. `OpenForBetsState` admite apostar y cerrar. `ClosedState` permite liquidar. `SettledState` no deja modificar el partido."),
          p("<b>Patrón Strategy:</b> `PayoutStrategy` define cómo calcular el premio. `FixedOddsPayoutStrategy` implementa una cuota fija. Si cambian las cuotas, se agrega otra estrategia sin cambiar `MatchAggregate`."),
          code('public Money payout(Bet bet, String winner) {\n  return bet.team().equalsIgnoreCase(winner)\n      ? bet.stake().multiply(multiplier)\n      : Money.ZERO;\n}'),
          p("<b>Value Objects:</b> `Money`, `MatchId`, `BetId` y `UserId` representan conceptos del dominio. `Money` impide valores negativos y normaliza los decimales."),
          p("<b>Rutas:</b> " + file_path("domain/.../match/OpenForBetsState.java") + ", " + file_path("domain/.../payout/FixedOddsPayoutStrategy.java") + " y " + file_path("domain/.../money/Money.java")),
          callout("Pregunta probable: ¿por qué BigDecimal?", "`double` puede producir errores de precisión decimal. Para dinero se usa `BigDecimal`, porque un centavo incorrecto es un error de negocio.", GREEN), PageBreak()]

add_page(story, "6. Pruebas del dominio", "CARLOS")
story += [p("Las reglas críticas se prueban sin levantar Spring, Docker, Kafka ni RabbitMQ. Esto hace que las pruebas sean rápidas y que validen la lógica de verdad."),
          p("<b>Pruebas que debes mencionar:</b>"), *bullets([
              "Un partido no acepta dos equipos iguales.",
              "No se puede apostar por un equipo ajeno al partido.",
              "Una apuesta no se acepta después del cierre.",
              "No se puede liquidar en un estado inválido.",
              "El premio se calcula correctamente con la estrategia."
          ]),
          p("<b>Ruta:</b> " + file_path("domain/src/test/java/com/grupo3/betting/domain/match/MatchAggregateTest.java")),
          callout("Mockito", "En pruebas de servicios se usa Mockito para reemplazar repositorios, gateways o publicadores. Así se comprueba una clase sin requerir PostgreSQL, Mercado Pago o un broker real.", YELLOW),
          p("<b>Cierre de Carlos:</b> “El dominio rico mantiene las reglas cerca de los datos. State controla las transiciones, Strategy evita acoplar el cálculo del premio y las pruebas garantizan que esas reglas no se rompan.”", "Callout"), PageBreak()]

# Bayron
story += section_title("Apuesta, PostgreSQL, Outbox, Kafka e idempotencia", "Bayron", BLUE)
story += [p("<b>Objetivo de Bayron:</b> explicar por qué el saldo y la apuesta necesitan consistencia, y cómo los eventos llegan de forma confiable al motor de resultados."),
          p("<b>Frase inicial:</b> “Una apuesta no puede dejar dinero descontado sin un registro. Por eso usamos una transacción en PostgreSQL y el patrón Transactional Outbox.”", "Callout"), PageBreak()]

add_page(story, "7. Crear una apuesta de forma consistente", "BAYRON")
story += [diagram(["Validar usuario", "Debitar saldo", "Guardar apuesta", "Guardar Outbox"]),
          p("`BetService.place` coordina el caso de uso. Primero encuentra el usuario, valida el identificador y el monto, descuenta saldo, crea la apuesta y encola el evento dentro de la misma transacción."),
          code('@Transactional PlaceBetResponse place(String userId, PlaceBetRequest request) {\n  var user = users.findById(userId).orElseThrow(...);\n  new MatchId(request.matchId());\n  new Money(request.amount());\n  user.debit(request.amount());\n  bets.save(new BetRequestEntity(...));\n  outbox.enqueue(event);\n  return new PlaceBetResponse(...);\n}'),
          p("<b>Por qué PostgreSQL:</b> el saldo requiere consistencia. La transacción agrupa los cambios: si cualquiera falla, se revierte todo. No queda una apuesta a medias."),
          p("<b>Rutas:</b> " + file_path("betting-api/.../bet/BetService.java") + " y " + file_path("betting-api/.../account/UserAccount.java")),
          callout("Respuesta corta", "PostgreSQL protege los datos transaccionales. Kafka no reemplaza la base de datos: Kafka comunica eventos; PostgreSQL conserva el estado del negocio.", YELLOW), PageBreak()]

add_page(story, "8. Transactional Outbox y Kafka", "BAYRON")
story += [p("Un problema típico es descontar el saldo y luego intentar publicar en Kafka. Si Kafka falla en ese instante, se pierde la comunicación. Outbox evita ese riesgo."),
          diagram(["Transacción PostgreSQL", "Tabla Outbox", "Dispatcher", "Kafka", "Results Engine"]),
          *bullets([
              "La transacción guarda la apuesta y una fila de `bet_event_outbox`.",
              "`BetOutboxDispatcher` busca eventos pendientes y los publica.",
              "Al publicarse, el evento queda marcado con fecha de publicación.",
              "Si el envío falla, se incrementan intentos y se conserva el error."
          ]),
          p("<b>Kafka:</b> es un registro distribuido de eventos. Los consumidores reciben hechos como `BetPlacedEvent` sin acoplarse directamente al módulo que creó la apuesta."),
          p("<b>Rutas:</b> " + file_path("betting-api/.../bet/BetOutboxEntity.java") + ", " + file_path("betting-api/.../bet/BetOutboxDispatcher.java") + " y " + file_path("shared-contracts/.../event/BetPlacedEvent.java")),
          callout("Frase clave", "Outbox no promete que Kafka nunca repita un mensaje. Lo que hace es evitar perder el evento cuando la transacción del dinero ya fue confirmada.", GREEN), PageBreak()]

add_page(story, "9. Idempotencia, resultados y Mercado Pago", "BAYRON")
story += [p("<b>Idempotencia:</b> una operación es idempotente si repetirla no cambia el resultado una segunda vez. Es clave cuando hay reintentos de mensajes, webhooks o redes inestables."),
          *bullets([
              "Cada evento de apuesta tiene `eventId`.",
              "Results Engine registra los eventos procesados en `ProcessedEventEntity`.",
              "Una apuesta ya liquidada no vuelve a acreditar un premio.",
              "Una recarga real guarda `idempotencyKey` y usa una restricción única.",
              "`PaymentRecharge.approve()` no acredita dos veces si ya está aprobada."
          ]),
          p("<b>Clases para mostrar:</b> " + file_path("results-engine/.../persistence/ProcessedEventEntity.java") + " y " + file_path("betting-api/.../recharge/PaymentRecharge.java")),
          callout("Pregunta probable: ¿tienen exactly-once?", "No debemos afirmar exactly-once global. Kafka puede repetir mensajes. Conseguimos un efecto único con identificadores, estados finales y validaciones idempotentes.", YELLOW),
          p("<b>Cierre de Bayron:</b> “La transacción protege saldo y apuesta; Outbox protege el evento; Kafka distribuye el hecho; e idempotencia evita duplicar efectos cuando existen reintentos.”", "Callout"), PageBreak()]

# Integrante 4
story += section_title("Usuario, API, autenticación, Docker y Caddy", "Integrante 4", YELLOW)
story += [p("<b>Objetivo del Integrante 4:</b> iniciar la historia desde la experiencia del usuario y mostrar cómo se protege la entrada al sistema y cómo se ejecuta localmente o en AWS."),
          p("<b>Frase inicial:</b> “LiveBet se ve como una web única, pero el usuario entra por una capa HTTPS que redirige cada solicitud al servicio correcto.”", "Callout"), PageBreak()]

add_page(story, "10. Usuario, API y autenticación", "INTEGRANTE 4")
story += [diagram(["Navegador", "Caddy HTTPS", "betting-api", "PostgreSQL"]),
          *bullets([
              "El usuario se registra o inicia sesión con correo y contraseña.",
              "La contraseña se almacena como hash, no como texto visible.",
              "Al autenticarse, el sistema entrega un token JWT.",
              "Las rutas protegidas validan el token mediante `BearerTokenFilter`.",
              "El controlador recibe solicitudes; el servicio aplica el caso de uso; el repositorio persiste datos."
          ]),
          p("<b>Rutas:</b> " + file_path("betting-api/.../auth/AuthController.java") + ", " + file_path("betting-api/.../auth/TokenService.java") + " y " + file_path("betting-api/.../config/SecurityConfig.java")),
          callout("Pregunta probable: ¿por qué una solicitud no entra a la base de datos directamente?", "Porque el backend debe autenticar, validar datos, aplicar reglas, controlar permisos y ejecutar la transacción. Exponer la base de datos sería inseguro y rompería la arquitectura.", GREEN), PageBreak()]

add_page(story, "11. Docker Compose, servicios y redes", "INTEGRANTE 4")
story += [p("Docker Compose declara todos los contenedores necesarios para LiveBet. Así el proyecto se ejecuta igual en desarrollo y producción, cambiando solo las variables del ambiente."),
          p("<b>Servicios que debes nombrar:</b> PostgreSQL, Kafka, ZooKeeper, RabbitMQ, betting-api, dashboard-backend, match-generator, match-simulator, results-engine, email-worker y edge-proxy/Caddy."),
          code('services:\n  postgres-db:      # datos transaccionales\n  kafka:            # eventos\n  rabbitmq:         # colas y retrasos\n  betting-api:      # API principal\n  results-engine:   # liquidación\n  edge-proxy:       # HTTPS / Caddy'),
          *bullets([
              "Los contenedores se comunican por la red interna usando sus nombres de servicio.",
              "Hacia Internet se exponen principalmente 80 y 443 por Caddy.",
              "La API puede estar disponible internamente por 8081, pero no es necesario abrirla al público.",
              "El mismo Compose recibe variables diferentes según dev o prod."
          ]),
          p("<b>Ruta:</b> " + file_path("docker-compose.yml")),
          callout("Diferencia dev y prod", "No existen dos archivos Compose. El pipeline entrega variables distintas: desarrollo usa `COMPOSE_PROJECT_NAME=livebet-development` y pagos simulados; producción usa el dominio real y Mercado Pago.", YELLOW), PageBreak()]

add_page(story, "12. Caddy, HTTPS y preguntas transversales", "INTEGRANTE 4")
story += [p("Caddy funciona como reverse proxy. Recibe el tráfico web, obtiene certificados TLS automáticamente y redirige cada ruta al contenedor correspondiente."),
          code('livebet.beauty {\n  @betting_api path /api/auth* /api/bets* /api/recharges*\n  reverse_proxy @betting_api betting-api:8081\n  reverse_proxy dashboard-backend:8080\n}'),
          p("<b>HTTP:</b> protocolo para intercambiar solicitudes y respuestas web. <b>HTTPS:</b> HTTP protegido con TLS. <b>WebSocket:</b> conexión persistente útil para actualizaciones en tiempo real."),
          p("<b>Rutas:</b> " + file_path("infrastructure/caddy/Caddyfile") + " y " + file_path("dashboard-backend/.../config/WebSocketConfig.java")),
          callout("Cierre del Integrante 4", "Docker empaqueta servicios; Caddy protege y enruta el tráfico; la API autentica y valida solicitudes. Todo esto permite que la experiencia del usuario sea simple aunque el sistema interno sea distribuido.", GREEN), PageBreak()]

# Review / questions
add_page(story, "Preguntas que cualquiera puede responder", "REPASO DEL EQUIPO")
questions = [
    ("¿Por qué microservicios?", "Para separar responsabilidades: apuestas, resultados, simulación y correo pueden evolucionar y fallar de forma más controlada."),
    ("¿Kafka vs RabbitMQ?", "Kafka distribuye eventos y conserva un registro; RabbitMQ maneja colas de trabajo, retrasos y reintentos de tareas."),
    ("¿Qué protege PostgreSQL?", "La consistencia de datos críticos como saldo, apuestas, recargas y auditoría."),
    ("¿Qué es Outbox?", "Una tabla donde se guarda el evento junto con la transacción; un dispatcher lo publica después."),
    ("¿Qué es idempotencia?", "Repetir una operación no duplica el efecto, por ejemplo, no paga dos veces una apuesta."),
    ("¿Qué es un modelo rico?", "Una clase con datos y comportamiento que protege sus propias reglas, como MatchAggregate."),
    ("¿Qué hace Docker?", "Empaqueta cada servicio con sus dependencias en contenedores reproducibles."),
    ("¿Cómo separan dev y prod?", "Dos EC2, dos runners, dos GitHub Environments y variables/secretos independientes por rama.")]
for q,a in questions:
    story += [p(q, "H2Live"), p(a, "BodyLive")]
story.append(PageBreak())

add_page(story, "Lista de verificación antes de exponer", "ENSAYO FINAL")
story += [p("Cada integrante debe poder cumplir esta lista sin depender de otro compañero."),
          *bullets([
              "Explicar su bloque sin leer la diapositiva.",
              "Abrir una clase real y señalar una regla o decisión técnica.",
              "Dibujar o explicar su flujo en menos de un minuto.",
              "Responder una pregunta difícil de su parte.",
              "Conectar su sección con la persona anterior y la siguiente.",
              "No decir que tienen exactly-once global; decir idempotencia y efecto único.",
              "No llamar a Kafka base de datos ni a RabbitMQ API REST.",
              "Decir que desarrollo y producción son EC2 separadas y no comparten datos ni secretos."
          ]),
          callout("Última frase para todo el grupo", "LiveBet protege las reglas con un dominio rico, mantiene el dinero consistente con PostgreSQL y Outbox, distribuye eventos con Kafka, desacopla tareas con RabbitMQ y se despliega de forma repetible con Docker, AWS y GitHub Actions.", ORANGE),
          Spacer(1, .5*cm), p("Documento generado a partir de la estructura y las clases actuales del proyecto LiveBet.", "SmallLive")]

doc = BaseDocTemplate(str(OUT), pagesize=A4, rightMargin=1.4*cm, leftMargin=1.4*cm, topMargin=1.35*cm, bottomMargin=1.7*cm)
frame = Frame(doc.leftMargin, doc.bottomMargin, doc.width, doc.height, id="normal")
doc.addPageTemplates([PageTemplate(id="LiveBet", frames=[frame], onPage=page_num)])
doc.build(story)
print(OUT)
