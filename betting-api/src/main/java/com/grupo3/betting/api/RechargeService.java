package com.grupo3.betting.api;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.YearMonth;

@Service
class RechargeService {
    private final UserAccountRepository users;
    private final RechargeRepository recharges;

    RechargeService(UserAccountRepository users, RechargeRepository recharges) {
        this.users = users;
        this.recharges = recharges;
    }

    @Transactional
    RechargeView recharge(String userId, RechargeRequest request) {
        String number = request.cardNumber().replace(" ", "");
        if (!validCard(number)) throw new IllegalArgumentException("La tarjeta ficticia fue rechazada. Revisa el número");
        if (expired(request.expiry())) throw new IllegalArgumentException("La tarjeta está vencida");
        var user = users.findByIdForUpdate(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        user.credit(request.amount());
        var transaction = recharges.save(new RechargeTransaction(userId, request.amount(),
                request.cardholder().trim(), number.substring(number.length() - 4)));
        return transaction.view(user.balance());
    }

    private boolean expired(String value) {
        String[] parts = value.split("/");
        return YearMonth.of(2000 + Integer.parseInt(parts[1]), Integer.parseInt(parts[0]))
                .isBefore(YearMonth.now());
    }

    static boolean validCard(String number) {
        if (!number.matches("[0-9]{13,19}")) return false;
        int sum = 0; boolean doubleDigit = false;
        for (int i = number.length() - 1; i >= 0; i--) {
            int digit = number.charAt(i) - '0';
            if (doubleDigit && (digit *= 2) > 9) digit -= 9;
            sum += digit; doubleDigit = !doubleDigit;
        }
        return sum % 10 == 0;
    }
}
