package com.grupo3.betting.api;

import java.math.BigDecimal;
import java.util.Set;

final class RouletteRules {
    private static final Set<Integer> RED = Set.of(1,3,5,7,9,12,14,16,18,19,21,23,25,27,30,32,34,36);
    private RouletteRules() {}

    record Selection(String type, String value, String label, int multiplier) {}

    static Selection validate(String rawType, String rawValue) {
        if (rawType == null || rawValue == null) throw new IllegalArgumentException("Selecciona una opción de la mesa");
        String type = rawType.trim().toUpperCase(), value = rawValue.trim().toUpperCase();
        return switch (type) {
            case "NUMBER" -> {
                int number;
                try { number = Integer.parseInt(value); } catch (NumberFormatException e) { throw new IllegalArgumentException("Número de ruleta inválido"); }
                if (number < 0 || number > 36) throw new IllegalArgumentException("El número debe estar entre 0 y 36");
                yield new Selection(type, String.valueOf(number), "Número " + number, 36);
            }
            case "COLOR" -> {
                if (!Set.of("RED", "BLACK").contains(value)) throw new IllegalArgumentException("Color inválido");
                yield new Selection(type, value, value.equals("RED") ? "Rojo" : "Negro", 2);
            }
            case "PARITY" -> {
                if (!Set.of("EVEN", "ODD").contains(value)) throw new IllegalArgumentException("Paridad inválida");
                yield new Selection(type, value, value.equals("EVEN") ? "Par" : "Impar", 2);
            }
            case "RANGE" -> {
                if (!Set.of("LOW", "HIGH").contains(value)) throw new IllegalArgumentException("Rango inválido");
                yield new Selection(type, value, value.equals("LOW") ? "1–18" : "19–36", 2);
            }
            case "DOZEN" -> {
                if (!Set.of("FIRST", "SECOND", "THIRD").contains(value)) throw new IllegalArgumentException("Docena inválida");
                String label = switch (value) { case "FIRST" -> "1.ª docena"; case "SECOND" -> "2.ª docena"; default -> "3.ª docena"; };
                yield new Selection(type, value, label, 3);
            }
            default -> throw new IllegalArgumentException("Tipo de apuesta inválido");
        };
    }

    static boolean wins(String type, String value, int result) {
        return switch (type) {
            case "NUMBER" -> result == Integer.parseInt(value);
            case "COLOR" -> result != 0 && (value.equals("RED") == RED.contains(result));
            case "PARITY" -> result != 0 && (value.equals("EVEN") == (result % 2 == 0));
            case "RANGE" -> result != 0 && (value.equals("LOW") ? result <= 18 : result >= 19);
            case "DOZEN" -> result != 0 && switch (value) {
                case "FIRST" -> result <= 12;
                case "SECOND" -> result >= 13 && result <= 24;
                case "THIRD" -> result >= 25;
                default -> false;
            };
            default -> false;
        };
    }

    static BigDecimal payout(BigDecimal amount, int multiplier) { return amount.multiply(BigDecimal.valueOf(multiplier)); }
    static boolean isRed(int number) { return RED.contains(number); }
}
