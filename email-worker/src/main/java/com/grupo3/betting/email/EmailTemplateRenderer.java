package com.grupo3.betting.email;

import com.grupo3.betting.contracts.EmailNotification;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Component
class EmailTemplateRenderer {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy · hh:mm a", new Locale("es", "CO"))
            .withZone(ZoneId.of("America/Bogota"));

    String subject(EmailNotification notification) {
        return switch (notification.outcome()) {
            case "WIN" -> "🎉 ¡Ganaste tu apuesta en LiveBet!";
            case "LOSS" -> "Resultado de tu apuesta en LiveBet";
            case "ROULETTE_WIN" -> "🎉 ¡Ganaste en la ruleta LiveBet!";
            case "ROULETTE_LOSS" -> "Resultado de tu jugada en la ruleta";
            case "RECHARGE" -> "Recibo de tu recarga LiveBet";
            default -> "Tu apuesta fue reembolsada";
        };
    }

    String plainText(EmailNotification n) {
        if ("RECHARGE".equals(n.outcome())) return rechargePlainText(n);
        if (n.outcome().startsWith("ROULETTE_")) return roulettePlainText(n);
        return String.join("\n",
                headline(n),
                safe(n.homeTeam()) + " " + score(n.homeScore()) + " - " + score(n.awayScore()) + " " + safe(n.awayTeam()),
                "Tu selección: " + safe(n.selectedTeam()),
                "Monto apostado: " + money(n.stake()),
                payoutLabel(n) + ": " + money(n.payout()),
                "Apuesta: " + safe(n.betId()),
                "Partido: " + safe(n.matchId()),
                "Fecha: " + (n.occurredAt() == null ? "—" : DATE.format(n.occurredAt())),
                "Gracias por jugar en LiveBet.");
    }

    String html(EmailNotification n) {
        if ("RECHARGE".equals(n.outcome())) return rechargeHtml(n);
        if (n.outcome().startsWith("ROULETTE_")) return rouletteHtml(n);
        String accent = switch (n.outcome()) { case "WIN" -> "#34d399"; case "LOSS" -> "#ff7849"; default -> "#facc15"; };
        String badge = switch (n.outcome()) { case "WIN" -> "APUESTA GANADA"; case "LOSS" -> "APUESTA FINALIZADA"; default -> "APUESTA REEMBOLSADA"; };
        String date = n.occurredAt() == null ? "—" : DATE.format(n.occurredAt());
        return """
                <!doctype html><html lang="es"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"></head>
                <body style="margin:0;background:#f5efe2;font-family:Arial,Helvetica,sans-serif;color:#202126">
                  <div style="display:none;max-height:0;overflow:hidden">%s</div>
                  <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="background:#f5efe2;padding:28px 12px"><tr><td align="center">
                    <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="max-width:620px;background:#fffdf8;border:3px solid #202126;border-radius:20px;overflow:hidden;box-shadow:8px 8px 0 #202126">
                      <tr><td style="background:#202126;padding:24px 30px;color:#fff"><span style="font-size:30px;font-weight:900;letter-spacing:-1px">LIVE<span style="color:#ff7139">BET</span></span><span style="float:right;background:%s;color:#202126;padding:8px 12px;border-radius:999px;font-size:12px;font-weight:900">%s</span></td></tr>
                      <tr><td style="padding:34px 30px 18px"><h1 style="margin:0 0 10px;font-size:30px;line-height:1.1">%s</h1><p style="margin:0;color:#65666b;font-size:16px;line-height:1.5">%s</p></td></tr>
                      <tr><td style="padding:10px 30px"><table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="background:#fff3d7;border:2px solid #202126;border-radius:16px"><tr>
                        <td width="38%%" align="center" style="padding:22px 8px;font-size:18px;font-weight:800">%s</td>
                        <td width="24%%" align="center" style="padding:22px 8px;font-size:28px;font-weight:900">%s - %s</td>
                        <td width="38%%" align="center" style="padding:22px 8px;font-size:18px;font-weight:800">%s</td>
                      </tr></table></td></tr>
                      <tr><td style="padding:18px 30px 8px"><table role="presentation" width="100%%" cellspacing="0" cellpadding="0">
                        %s
                        %s
                        %s
                        %s
                      </table></td></tr>
                      <tr><td style="padding:20px 30px 32px"><div style="background:%s;border:2px solid #202126;border-radius:14px;padding:18px;text-align:center"><div style="font-size:13px;font-weight:800;text-transform:uppercase">%s</div><div style="font-size:34px;font-weight:900;margin-top:5px">%s</div></div></td></tr>
                      <tr><td style="background:#202126;color:#d7d2c8;padding:20px 30px;font-size:12px;line-height:1.6">Apuesta <strong style="color:#fff">%s</strong> · Partido <strong style="color:#fff">%s</strong><br>%s<br>Este es un mensaje automático de LiveBet.</td></tr>
                    </table>
                  </td></tr></table>
                </body></html>
                """.formatted(escape(headline(n)), accent, badge, escape(headline(n)), escape(description(n)),
                escape(n.homeTeam()), score(n.homeScore()), score(n.awayScore()), escape(n.awayTeam()),
                row("Tu selección", n.selectedTeam()), row("Ganador", n.winner()), row("Monto apostado", money(n.stake())),
                row("Estado", status(n)), accent, payoutLabel(n), money(n.payout()), escape(n.betId()),
                escape(n.matchId()), escape(date));
    }

    private String headline(EmailNotification n) {
        return switch (n.outcome()) { case "WIN" -> "¡Tu pronóstico fue correcto!"; case "LOSS" -> "El partido terminó"; default -> "Tu saldo está protegido"; };
    }

    private String description(EmailNotification n) {
        return switch (n.outcome()) {
            case "WIN" -> "Tu premio fue procesado y se sumará automáticamente a tu saldo.";
            case "LOSS" -> "Esta vez el resultado no coincidió con tu selección. La próxima puede ser la tuya.";
            default -> "La apuesta no alcanzó a entrar antes del cierre y el monto fue devuelto a tu saldo.";
        };
    }

    private String status(EmailNotification n) {
        return switch (n.outcome()) { case "WIN" -> "Ganada"; case "LOSS" -> "Perdida"; default -> "Reembolsada"; };
    }

    private String payoutLabel(EmailNotification n) { return "REJECTED".equals(n.outcome()) ? "Monto devuelto" : "Premio total"; }

    private String rechargePlainText(EmailNotification n) {
        return String.join("\n", "Recarga aprobada", "Monto: " + money(n.stake()),
                "Tarjeta terminada en: " + safe(n.cardLast4()), "Nuevo saldo: " + money(n.balance()),
                "Referencia: " + safe(n.reference()), "Fecha: " + DATE.format(n.occurredAt()),
                "Gracias por usar LiveBet.");
    }

    private String roulettePlainText(EmailNotification n) {
        return String.join("\n", "ROULETTE_WIN".equals(n.outcome()) ? "Jugada ganada" : "Jugada finalizada",
                "Tu selección: " + safe(n.selectedTeam()), "Resultado: " + safe(n.winner()),
                "Monto apostado: " + money(n.stake()), "Premio: " + money(n.payout()),
                "Jugada: " + safe(n.betId()), "Ronda: " + safe(n.matchId()),
                "Fecha: " + DATE.format(n.occurredAt()));
    }

    private String rechargeHtml(EmailNotification n) {
        return emailShell("RECARGA APROBADA", "#54c7ec", "Tu saldo ya está disponible",
                "Procesamos correctamente tu recarga en LiveBet.",
                row("Monto recargado", money(n.stake())) + row("Tarjeta", "•••• " + safe(n.cardLast4()))
                        + row("Referencia", n.reference()) + row("Fecha", DATE.format(n.occurredAt())),
                "NUEVO SALDO", money(n.balance()));
    }

    private String rouletteHtml(EmailNotification n) {
        boolean won = "ROULETTE_WIN".equals(n.outcome());
        String accent = won ? "#34d399" : "#ff7849";
        return emailShell(won ? "JUGADA GANADA" : "JUGADA FINALIZADA", accent,
                won ? "¡La ruleta estuvo de tu lado!" : "La ronda ha terminado",
                won ? "Tu premio ya fue agregado automáticamente a tu saldo." : "Esta vez tu selección no coincidió con el resultado.",
                row("Tu selección", n.selectedTeam()) + row("Número ganador", n.winner())
                        + row("Monto apostado", money(n.stake())) + row("Jugada", n.betId())
                        + row("Ronda", n.matchId()),
                won ? "PREMIO TOTAL" : "RESULTADO", won ? money(n.payout()) : "Sin premio");
    }

    private String emailShell(String badge, String accent, String title, String description,
                              String rows, String totalLabel, String totalValue) {
        return """
                <!doctype html><html lang="es"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"></head>
                <body style="margin:0;background:#f5efe2;font-family:Arial,Helvetica,sans-serif;color:#202126">
                  <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="background:#f5efe2;padding:28px 12px"><tr><td align="center">
                    <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="max-width:620px;background:#fffdf8;border:3px solid #202126;border-radius:20px;overflow:hidden;box-shadow:8px 8px 0 #202126">
                      <tr><td style="background:#202126;padding:24px 30px;color:#fff"><span style="font-size:30px;font-weight:900">LIVE<span style="color:#ff7139">BET</span></span><span style="float:right;background:%s;color:#202126;padding:8px 12px;border-radius:999px;font-size:12px;font-weight:900">%s</span></td></tr>
                      <tr><td style="padding:34px 30px 10px"><h1 style="margin:0 0 10px;font-size:30px">%s</h1><p style="margin:0;color:#65666b;font-size:16px;line-height:1.5">%s</p></td></tr>
                      <tr><td style="padding:18px 30px"><table role="presentation" width="100%%" cellspacing="0" cellpadding="0">%s</table></td></tr>
                      <tr><td style="padding:4px 30px 32px"><div style="background:%s;border:2px solid #202126;border-radius:14px;padding:18px;text-align:center"><div style="font-size:13px;font-weight:800">%s</div><div style="font-size:34px;font-weight:900;margin-top:5px">%s</div></div></td></tr>
                      <tr><td style="background:#202126;color:#d7d2c8;padding:20px 30px;font-size:12px">Este es un mensaje automático de LiveBet. Nunca te pediremos datos completos de tu tarjeta por correo.</td></tr>
                    </table>
                  </td></tr></table>
                </body></html>
                """.formatted(accent, escape(badge), escape(title), escape(description), rows,
                accent, escape(totalLabel), escape(totalValue));
    }
    private String score(Integer value) { return value == null ? "—" : value.toString(); }
    private String safe(String value) { return value == null || value.isBlank() ? "—" : value; }
    private String row(String label, String value) {
        return "<tr><td style=\"padding:10px 0;border-bottom:1px solid #ded9cf;color:#6b6b6f\">" + escape(label)
                + "</td><td align=\"right\" style=\"padding:10px 0;border-bottom:1px solid #ded9cf;font-weight:800\">"
                + escape(value) + "</td></tr>";
    }
    private String money(BigDecimal amount) {
        if (amount == null) return "$ 0";
        var symbols = new DecimalFormatSymbols(new Locale("es", "CO"));
        symbols.setGroupingSeparator('.');
        return "$ " + new DecimalFormat("#,##0", symbols).format(amount) + " COP";
    }
    private String escape(String value) {
        return safe(value).replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
}
