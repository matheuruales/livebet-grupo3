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
            default -> "Tu apuesta fue reembolsada";
        };
    }

    String plainText(EmailNotification n) {
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
